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

package org.apache.doris.manager.agent.service.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventInfo;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResourceType;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HeartbeatEventHandler {

    @Autowired
    private InstanceEventHandler instanceEventHandler;

    @Autowired
    private AgentEventHandler agentEventHandler;

    public HeartBeatEventResult handHeartBeatEvent(HeartBeatEventInfo eventInfo) {
        if (eventInfo.getResourceType().equals(HeartBeatEventResourceType.AGENT)) {
            return agentEventHandler.handleAgentHeartBeat(eventInfo);
        } else {
            return instanceEventHandler.handleInstanceHeartBeat(eventInfo);
        }
    }
}
