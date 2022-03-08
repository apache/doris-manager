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
import org.apache.doris.manager.agent.service.heartbeat.HeartbeatEventHandler;
import org.apache.doris.manager.agent.util.Request;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventInfo;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HeartBeatService {
    @Autowired
    private HeartbeatEventHandler heartbeatEventHandler;

    @Autowired
    private Environment environment;

    // When the agent starts, it needs to complete the registration before it can handle other heartbeats
    private String agentNodeId = "";

    private String serverEndpoint = "";

    private String heartBeatUrl = "";

    // TODO：Reserved for subsequent active reporting of instance status
//    private Set<InstanceInfo> instances = new HashSet<>();

    // TODO: To be improved
    // TODO: Currently, the heartbeat implemented here is only responsible for obtaining the events to be executed
    //  from the server, and does not report the instance status controlled by the current agent
    // TODO:Execute once when the agent process starts?
    // Send heartbeat every 5 seconds, get heartbeat event list
//    @PostConstruct
    @Scheduled(cron = "0/5 * * * * ?")
    public void heartBeat() {
        System.out.println("aaaaa1");
        if (agentNodeId.isEmpty() || serverEndpoint.isEmpty()) {
            System.out.println("aaaaa2");
            agentNodeId = environment.getProperty("agent.node.id");
            serverEndpoint = environment.getProperty("manager.server.endpoint");
            heartBeatUrl = "http://" + serverEndpoint + "/api/control/node/" + agentNodeId + "/agent/heartbeat";
        }
        // TODO :Process according to the returned heartbeat event results
        // TODO：If there is an event and it is processed, the result is sent
        log.info("agent node is " + agentNodeId);
        log.info("heartBeatUrl is " + heartBeatUrl);

        List<HeartBeatEventInfo> eventInfos = Request.getHeartBeatEventInfo(heartBeatUrl);

        List<HeartBeatEventResult> results = new ArrayList<>();
        for (HeartBeatEventInfo eventInfo : eventInfos) {
            HeartBeatEventResult result = heartbeatEventHandler.handHeartBeatEvent(eventInfo);
            if (result != null) {
                results.add(result);
            }
        }

        Request.sendHeartBeatEventResult(heartBeatUrl, results);
    }

}
