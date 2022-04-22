// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.manager.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.agent.exceptions.InstanceNotInstallException;
import org.apache.doris.manager.agent.exceptions.InstanceNotRunningException;
import org.apache.doris.manager.agent.exceptions.InstanceServiceException;
import org.apache.doris.manager.agent.service.heartbeat.DorisInstanceOperator;
import org.apache.doris.manager.agent.service.heartbeat.HeartbeatEventHandler;
import org.apache.doris.manager.agent.util.Request;
import org.apache.doris.manager.common.heartbeat.HeartBeatContext;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventInfo;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResult;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResultType;
import org.apache.doris.manager.common.heartbeat.HeartBeatResult;
import org.apache.doris.manager.common.heartbeat.InstanceInfo;
import org.apache.doris.manager.common.heartbeat.InstanceStateResult;
import org.apache.doris.stack.control.ModelControlState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class HeartBeatService {
    @Autowired
    private HeartbeatEventHandler heartbeatEventHandler;

    @Autowired
    private DorisInstanceOperator instanceOpera;

    @Autowired
    private Environment environment;

    private ConcurrentHashMap<Long, HeartBeatEventInfo> events = new ConcurrentHashMap<>();  // event id
    private ConcurrentHashMap<Long, InstanceInfo>  instanceInfos = new ConcurrentHashMap<>(); // instance id

    // cache event result in case of http post result failure
    // some event is not reentrant
    private ConcurrentHashMap<Long, HeartBeatEventResult> cacheResults = new ConcurrentHashMap<>();

    // When the agent starts, it needs to complete the registration before it can handle other heartbeats
    private String agentNodeId = "";

    private String serverEndpoint = "";

    private String heartBeatUrl = "";

    @Scheduled(cron = "0/${agent.heartbeat.interval:5} * * * * ?")
    public void handleHeartBeatContextLoop() {
        if (agentNodeId.isEmpty() || serverEndpoint.isEmpty()) {
            agentNodeId = environment.getProperty("agent.node.id");
            serverEndpoint = environment.getProperty("manager.server.endpoint");
            heartBeatUrl = "http://" + serverEndpoint + "/api/control/node/" + agentNodeId + "/agent/context";
        }

        log.info("agent node is " + agentNodeId);
        log.info("heartBeatUrl is " + heartBeatUrl);

        HeartBeatContext ctx = Request.getHeartBeatContext(heartBeatUrl);

        // duplicate task
        HeartBeatContext newCtx = addAndFilterContextTask(ctx);

        Thread contextTask = new Thread(() -> {
            HeartBeatResult res = executeContextTask(newCtx);

            cacheResults.clear();
            try {
                String dealRes = Request.sendHeartBeatContextResult(heartBeatUrl, res);
                log.info("server return context deal result: {}", dealRes);
            } catch (IOException e) {
                log.warn("send heartbeat context result error: {}", e.getMessage());
                res.getEventResults().forEach((eventRes) -> {
                    if (eventRes.getResultType() != HeartBeatEventResultType.FAIL) {
                        log.info("cache event {} result, event type {}, stage {}", eventRes.getEventId(),
                                eventRes.getEventType(), eventRes.getEventStage());
                        cacheResults.put(eventRes.getEventId(), eventRes);
                    }
                });
            }

            // clear completed tasks
            newCtx.getEvents().forEach((e) -> {
                log.info("remove finished [event {}] task", e.getEventId());
                events.remove(e.getEventId());
            });
            newCtx.getInstanceInfos().forEach((ins) -> {
                instanceInfos.remove(ins.getInstanceId());
            });
        });

        contextTask.start();
    }

    private HeartBeatResult executeContextTask(HeartBeatContext ctx) {
        List<HeartBeatEventResult> eventResults = new ArrayList<>();
        List<InstanceStateResult> insStateResults = new ArrayList<>();

        //TODO find from cache before

        for (HeartBeatEventInfo eventInfo : ctx.getEvents()) {
            log.info("handle event {}: resource:{} type:{} stage:{}", eventInfo.getEventId(),
                    eventInfo.getResourceType(), eventInfo.getEventStage(), eventInfo.getEventStage());

            // get result from cache if it has been executed
            long eventId = eventInfo.getEventId();
            if (cacheResults.containsKey(eventId)) {
                HeartBeatEventResult cr = cacheResults.get(eventId);
                log.info("result is in cache, event {}, stage {}, result type {}", cr.getEventId(),
                        cr.getEventStage(), cr.getResultType());
                if (cr.getResultType() == HeartBeatEventResultType.PROCESSING
                        && eventInfo.getEventStage() < cr.getEventStage()) {
                    log.info("return result from result cache");
                    eventResults.add(cr);
                    continue;
                } else if (cr.getResultType() == HeartBeatEventResultType.SUCCESS
                        && cr.getEventStage() == cr.getEventStage()) {
                    log.info("return result form result cache");
                    eventResults.add(cr);
                    continue;
                }
            }

            HeartBeatEventResult result = heartbeatEventHandler.handHeartBeatEvent(eventInfo);
            if (result != null) {
                eventResults.add(result);
            }
        }

        for (InstanceInfo instanceInfo : ctx.getInstanceInfos()) {
            log.info("check module {} instance {} state", instanceInfo.getModuleName(), instanceInfo.getInstanceId());
            InstanceStateResult stateResult = new InstanceStateResult(instanceInfo);
            try {
                instanceOpera.checkInstanceProcessState(instanceInfo.getModuleName(), instanceInfo.getInstallDir(),
                        instanceInfo.getHttpPort());

                stateResult.setState(ModelControlState.RUNNING);
            } catch (InstanceNotInstallException e) {
                log.error("{} instance check exception {}", instanceInfo.getModuleName(), e.getMessage());
                // maybe instance has noe be installed
                stateResult.setState(ModelControlState.INIT);
                stateResult.setErrMsg(e.getMessage());
            } catch (InstanceNotRunningException | InstanceServiceException e) {
                log.error("{} instance check exception {}", instanceInfo.getModuleName(), e.getMessage());
                stateResult.setState(ModelControlState.STOPPED);
                stateResult.setErrMsg(e.getMessage());
            }

            insStateResults.add(stateResult);
        }

        HeartBeatResult res = new HeartBeatResult();
        res.setEventResults(eventResults);
        res.setStateResults(insStateResults);

        return res;
    }

    private HeartBeatContext addAndFilterContextTask(HeartBeatContext ctx) {
        HeartBeatContext filterCtx = new HeartBeatContext();
        List<HeartBeatEventInfo> newEvents = new ArrayList<>();
        List<InstanceInfo> newInsInfos = new ArrayList<>();

        if (ctx.getEvents() != null) {
            for (HeartBeatEventInfo eventInfo : ctx.getEvents()) {
                if (events.containsKey(eventInfo.getEventId())) {
                    log.warn("heartbeat event {} is running", eventInfo.getEventId());
                    continue;
                }
                log.info("add event {}", eventInfo.getEventId());
                events.put(eventInfo.getEventId(), eventInfo);
                newEvents.add(eventInfo);
            }
        }

        if (ctx.getInstanceInfos() != null) {
            for (InstanceInfo ins : ctx.getInstanceInfos()) {
                if (instanceInfos.containsKey(ins.getInstanceId())) {
                    log.warn("module {} instance {} check task is running", ins.getModuleName(), ins.getInstanceId());
                    continue;
                }
                log.info("add {} instance {} check task", ins.getModuleName(), ins.getInstanceId());
                instanceInfos.put(ins.getInstanceId(), ins);
                newInsInfos.add(ins);
            }
        }

        filterCtx.setEvents(newEvents);
        filterCtx.setInstanceInfos(newInsInfos);
        return filterCtx;
    }
}
