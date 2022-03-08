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
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResult;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResultType;
import org.apache.doris.manager.common.heartbeat.stage.AgentInstallEventStage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentEventHandler {
    public HeartBeatEventResult handleAgentHeartBeat(HeartBeatEventInfo eventInfo) {
        // The agent side only needs to handle the last
        // step of agent installation and restart and report the heartbeat
        return register(eventInfo);
    }

    // This method doesn't need to do anything, just register
    public HeartBeatEventResult register(HeartBeatEventInfo eventInfo) {
        HeartBeatEventResult result = new HeartBeatEventResult(eventInfo);

        result.setEventStage(eventInfo.getEventStage());
        result.setCompleted(true);
        result.setResultType(HeartBeatEventResultType.SUCCESS);
        result.setResultInfo(AgentInstallEventStage.AGENT_REGISTER.getMessage());

        return result;
    }
}
