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
import org.apache.doris.manager.common.heartbeat.HeartBeatEventInfo;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResourceType;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResult;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventType;
import org.apache.doris.manager.common.heartbeat.config.AgentInstallEventConfigInfo;
import org.apache.doris.stack.control.manager.ResourceNodeAndAgentManager;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.HeartBeatEventRepository;
import org.apache.doris.stack.dao.ResourceClusterRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.apache.doris.stack.entity.ResourceClusterEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.model.request.control.PMResourceClusterAccessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
    private ResourceNodeAndAgentManager nodeAndAgentManager;

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
}
