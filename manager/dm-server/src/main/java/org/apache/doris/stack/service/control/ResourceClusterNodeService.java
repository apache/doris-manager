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

package org.apache.doris.stack.service.control;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.common.heartbeat.HeartBeatContext;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventInfo;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResourceType;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResult;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventType;
import org.apache.doris.manager.common.heartbeat.HeartBeatResult;
import org.apache.doris.manager.common.heartbeat.InstanceInfo;
import org.apache.doris.manager.common.heartbeat.InstanceStateResult;
import org.apache.doris.manager.common.heartbeat.config.AgentInstallEventConfigInfo;
import org.apache.doris.manager.common.util.ServerAndAgentConstant;
import org.apache.doris.stack.control.ModelControlState;
import org.apache.doris.stack.control.manager.ResourceNodeAndAgentManager;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.ClusterModuleRepository;
import org.apache.doris.stack.dao.ClusterModuleServiceRepository;
import org.apache.doris.stack.dao.HeartBeatEventRepository;
import org.apache.doris.stack.dao.ResourceClusterRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.apache.doris.stack.entity.ClusterModuleEntity;
import org.apache.doris.stack.entity.ClusterModuleServiceEntity;
import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.apache.doris.stack.entity.ResourceClusterEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.model.request.control.PMResourceClusterAccessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ResourceClusterNodeService {

    @Autowired
    private ResourceNodeRepository nodeRepository;

    @Autowired
    private ResourceClusterRepository resourceClusterRepository;

    @Autowired
    private ClusterInstanceRepository instanceRepository;

    @Autowired
    private HeartBeatEventRepository heartBeatEventRepository;

    @Autowired
    private ClusterModuleRepository clusterModuleRepository;

    @Autowired
    private ResourceNodeAndAgentManager nodeAndAgentManager;

    @Autowired
    private ClusterModuleServiceRepository serviceRepository;

    // Send uncompleted heartbeat events that need to be handled by agent
    public List<HeartBeatEventInfo> getHeartbeat(long agentNodeId) {
        log.info("Get agent {} uncompleted heartbeat events", agentNodeId);
        List<HeartBeatEventInfo> eventInfos = new ArrayList<>();

        ResourceNodeEntity nodeEntity = nodeRepository.findById(agentNodeId).get();

        HeartBeatEventEntity agentEvent = heartBeatEventRepository.findById(nodeEntity.getCurrentEventId()).get();
        addHeartbeatByEntity(agentEvent, agentNodeId, 0L, eventInfos);

        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByNodeId(agentNodeId);
        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            if (instanceEntity.getCurrentEventId() < 1L) {
                continue;
            }
            HeartBeatEventEntity instanceEvent =
                    heartBeatEventRepository.findById(instanceEntity.getCurrentEventId()).get();
            addHeartbeatByEntity(instanceEvent, agentNodeId, instanceEntity.getId(), eventInfos);
        }
        nodeEntity.setLastHeartBeatTimestamp(new Timestamp(System.currentTimeMillis()));
        nodeRepository.save(nodeEntity);
        return eventInfos;
    }

    public List<InstanceInfo> getInstanceInfo(long agentNodeId) {
        log.info("get node {} instance info", agentNodeId);
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByNodeId(agentNodeId);

        List<InstanceInfo> instanceInfos = new ArrayList<>();

        for (ClusterInstanceEntity ins : instanceEntities) {
            Optional<ClusterModuleEntity> moduleEntityOpt = clusterModuleRepository.findById(ins.getModuleId());
            if (!moduleEntityOpt.isPresent()) {
                log.error("this instance module {} is not find, ignore it", ins.getModuleId());
                continue;
            }

            ClusterModuleEntity moduleEntity = moduleEntityOpt.get();

            log.info("to get module {} instance {} info", moduleEntity.getModuleName(), ins.getId());

            int httpPort = 0;
            String httpServerName = "";
            if (moduleEntity.getModuleName().equals(ServerAndAgentConstant.FE_NAME)) {
                httpServerName = ServerAndAgentConstant.FE_HTTP_SERVICE;
            } else if (moduleEntity.getModuleName().equals(ServerAndAgentConstant.BE_NAME)) {
                httpServerName = ServerAndAgentConstant.BE_HTTP_SERVICE;
            }

            List<ClusterModuleServiceEntity> httpServices = serviceRepository.getByClusterIdAndName(
                    moduleEntity.getClusterId(), httpServerName);

            for (ClusterModuleServiceEntity service : httpServices) {
                List<String> addrList = JSON.parseArray(service.getAddressInfo(), String.class);
                if (addrList.contains(ins.getAddress())) {
                    httpPort = service.getPort();
                }
            }

            log.info("module {} instance {} http port is {}", moduleEntity.getModuleName(), ins.getId(), httpPort);

            InstanceInfo instanceInfo = new InstanceInfo(ins.getNodeId(), ins.getModuleId(), ins.getId(),
                    moduleEntity.getModuleName(), ins.getInstallInfo(), httpPort);
            log.info("get instance {} info: {}", ins.getId(), instanceInfo);
            instanceInfos.add(instanceInfo);
        }

        return instanceInfos;
    }

    public HeartBeatContext getHeartBeatContext(long agentNodeId) {
        log.info("start to get heartbeat context");
        HeartBeatContext ctx = new HeartBeatContext();
        ctx.setEvents(getHeartbeat(agentNodeId));
        ctx.setInstanceInfos(getInstanceInfo(agentNodeId));
        return ctx;
    }

    // Handle the result of the heartbeat event of the agent
    public void dealHeartbeatResult(List<HeartBeatEventResult> eventResults) {
        for (HeartBeatEventResult eventResult : eventResults) {
            long eventId = eventResult.getEventId();
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();
            // The event has been cancelled
            if (eventEntity.isCompleted()) {
                continue;
            }
            eventEntity.setStatus(eventResult.getResultType().name());
            eventEntity.setCompleted(eventResult.isCompleted());
            eventEntity.setOperateResult(JSON.toJSONString(eventResult.getResultInfo()));
            eventEntity.setStage(eventResult.getEventStage());
            heartBeatEventRepository.save(eventEntity);
        }
    }

    public void dealHeartbeatContext(HeartBeatResult ctx) {
        log.info("deal heart beat context {}", ctx);

        if (ctx.getEventResults() == null) {
            log.warn("no events to deal");
        } else {
            dealHeartbeatResult(ctx.getEventResults());
        }

        if (ctx.getStateResults() == null) {
            log.warn("no instance state result to deal");
        } else {
            for (InstanceStateResult stateResult : ctx.getStateResults()) {
                dealInstanceState(stateResult);
            }
        }
    }

    public void dealInstanceState(InstanceStateResult stateResult) {
        log.info("update module {} instance {}  state is {}",  stateResult.getModuleName(), stateResult.getInstanceId(),
                stateResult.getState());

        Optional<ClusterInstanceEntity> instanceEntityOpt = instanceRepository.findById(stateResult.getInstanceId());
        if (!instanceEntityOpt.isPresent()) {
            log.error("instance {} does not exists", stateResult.getInstanceId());
        }

        ClusterInstanceEntity instanceEntity = instanceEntityOpt.get();
        instanceEntity.setCurrentState(stateResult.getState().getValue());
        instanceRepository.save(instanceEntity);
    }

    public void operateAgent(long nodeId, String operateType) throws Exception {

        HeartBeatEventType eventType = HeartBeatEventType.valueOf(operateType);

        // TODO:Currently, only agent install is implemented

        ResourceNodeEntity nodeEntity = nodeRepository.findById(nodeId).get();

        ResourceClusterEntity clusterEntity = resourceClusterRepository.findById(nodeEntity.getResourceClusterId()).get();
        PMResourceClusterAccessInfo accessInfo = JSON.parseObject(clusterEntity.getAccessInfo(),
                PMResourceClusterAccessInfo.class);

        if (eventType == HeartBeatEventType.AGENT_INSTALL) {
            AgentInstallEventConfigInfo configInfo = new AgentInstallEventConfigInfo();
            configInfo.setSshUser(accessInfo.getSshUser());
            configInfo.setSshPort(accessInfo.getSshPort());
            configInfo.setSshKey(accessInfo.getSshKey());
            // TODO:Do not put the request ID temporarily
            nodeAndAgentManager.installAgentOperation(nodeEntity, configInfo, 0L);
        } else {
            throw new Exception("The agent operate type not support");
        }
    }

    public void operateAgentCancel(long nodeId, String operateType) throws Exception {
        // TODO:Not implemented
    }

    private void addHeartbeatByEntity(HeartBeatEventEntity eventEntity, long agentNodeId,
                                      long instanceId, List<HeartBeatEventInfo> eventInfos) {
        if (!eventEntity.isCompleted()) {
            HeartBeatEventInfo eventInfo = new HeartBeatEventInfo();
            eventInfo.setAgentNodeId(agentNodeId);
            eventInfo.setInstanceId(instanceId);
            if (instanceId > 0) {
                eventInfo.setResourceType(HeartBeatEventResourceType.INSTANCE);
            } else {
                eventInfo.setResourceType(HeartBeatEventResourceType.AGENT);
            }
            eventInfo.setEventId(eventEntity.getId());
            eventInfo.setEventType(HeartBeatEventType.valueOf(eventEntity.getType()));
            eventInfo.setConfigInfo(JSON.parseObject(eventEntity.getConfigInfo()));
            eventInfo.setEventStage(eventEntity.getStage());
            eventInfos.add(eventInfo);
        } else {
            return;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateInstancesState(ResourceNodeEntity node, int state) {
        log.info("update instances of node {} {} to state {}", node.getId(), node.getHost(), state);
        List<ClusterInstanceEntity> instanceEntities =  instanceRepository.getByNodeId(node.getId());
        if (instanceEntities.isEmpty()) {
            log.warn("node {} does hava any instances", node.getId());
            return;
        }

        for (ClusterInstanceEntity ins : instanceEntities) {
            log.info("update instance {} of module {} to {}", ins.getId(), ins.getModuleId(), state);
            ins.setCurrentState(state);
            instanceRepository.save(ins);
        }
    }

    @Scheduled(cron = "0/60 * * * * ?")
    public void agentNodeStateCheck() {
        log.info("start to check agent nodes state");
        List<ResourceNodeEntity> nodes = nodeRepository.findAll();
        if (nodes.isEmpty()) {
            log.info("no any agent nodes");
            return;
        }

        for (ResourceNodeEntity node : nodes) {
            Timestamp lastTime = node.getLastHeartBeatTimestamp();
            if (lastTime == null) {
                log.warn("not receive heartbeat yet form node {} {}", node.getId(), node.getHost());
                if (node.getCurrentState() != ModelControlState.INIT.getValue()) {
                    node.setCurrentState(ModelControlState.INIT.getValue());
                    nodeRepository.save(node);
                }
                continue;
            }

            log.info("node {} {} last heartbeat time {}", node.getId(), node.getHost(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastTime));
            if (System.currentTimeMillis() - lastTime.getTime() > 60 * 1000) {
                log.warn("node {} heartbeat timeout", node.getId());
                log.warn("update node {} form {} to {}", node.getId(), node.getCurrentState(),
                        ModelControlState.UNKNOWN.getValue());
                if (node.getCurrentState() != ModelControlState.UNKNOWN.getValue()) {
                    node.setCurrentState(ModelControlState.UNKNOWN.getValue());
                    log.info("update all instance of node {} to UNKNOWN", node.getId());
                    updateInstancesState(node, ModelControlState.UNKNOWN.getValue());
                }
            } else {
                log.info("node {} heartbeat state normal", node.getId());
                log.warn("update node {} form {} to {}", node.getId(), node.getCurrentState(),
                        ModelControlState.RUNNING.getValue());
                if (node.getCurrentState() != ModelControlState.RUNNING.getValue()) {
                    node.setCurrentState(ModelControlState.RUNNING.getValue());
                }
            }
            nodeRepository.save(node);
        }
    }
}
