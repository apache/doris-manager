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

package org.apache.doris.stack.control.manager;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResultType;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventType;
import org.apache.doris.manager.common.heartbeat.config.InstanceDeployCheckEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceInstallEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceRestartEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceStartEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceStopEventConfigInfo;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.HeartBeatEventRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.apache.doris.stack.entity.ClusterModuleEntity;
import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DorisClusterInstanceManager {

    @Autowired
    private ClusterInstanceRepository clusterInstanceRepository;

    @Autowired
    private ResourceNodeRepository nodeRepository;

    @Autowired
    private HeartBeatEventRepository heartBeatEventRepository;

    public long initOperation(long clusterId, ClusterModuleEntity moudle, long nodeId) {
        // TODO:Judge whether node can deploy this instance
        log.info("create a new instance for cluster {} moudle {} on node {}", clusterId, moudle.getModuleName(), nodeId);
        ResourceNodeEntity nodeEntity = nodeRepository.findById(nodeId).get();

        ClusterInstanceEntity instanceEntity = new ClusterInstanceEntity(clusterId, moudle.getId(), nodeId,
                nodeEntity.getAgentInstallDir(), nodeEntity.getHost());
        ClusterInstanceEntity newInstanceEntity = clusterInstanceRepository.save(instanceEntity);
        return newInstanceEntity.getId();
    }

    public void deployOperation(ClusterInstanceEntity instance, InstanceInstallEventConfigInfo configInfo,
                                long requestId) {
        // TODO:Judge whether instance can be deploy
        // TODO: Step fallback operation
        log.info("deploy instance {}", instance.getId());
        long eventId = instance.getCurrentEventId();
        if (eventId < 1L) {
            // First install instance operation
            log.debug("first deploy instance {}", instance.getId());
            HeartBeatEventEntity eventEntity = new HeartBeatEventEntity(HeartBeatEventType.INSTANCE_INSTALL.name(),
                    HeartBeatEventResultType.INIT.name(), JSON.toJSONString(configInfo), requestId);

            HeartBeatEventEntity newEventEntity = heartBeatEventRepository.save(eventEntity);

            instance.setCurrentEventId(newEventEntity.getId());
            clusterInstanceRepository.save(instance);
        } else {
            log.debug("deploy instance {} heart beat event {} exist", instance.getId(), eventId);
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();
            // TODO:exception
            if (!eventEntity.getType().equals(HeartBeatEventType.INSTANCE_INSTALL.name())) {
                return;
            }

            // If the agent has been successfully installed, it cannot be installed again
            if (eventEntity.isCompleted() && eventEntity.getStatus().equals(HeartBeatEventResultType.SUCCESS.name())) {
                return;
            }

            eventEntity.setStatus(HeartBeatEventResultType.INIT.name());
            eventEntity.setConfigInfo(JSON.toJSONString(configInfo));

            heartBeatEventRepository.save(eventEntity);
        }
    }

    public void checkDeployOperation(ClusterInstanceEntity instance, InstanceDeployCheckEventConfigInfo configInfo,
                                     long requestId) {
        log.info("check instance {} deploy", instance.getId());
        long eventId = instance.getCurrentEventId();
        if (eventId < 1L) {
            // First check instance install operation
            log.debug("first check instance {} deploy", instance.getId());
            HeartBeatEventEntity eventEntity = new HeartBeatEventEntity(HeartBeatEventType.INSTANCE_DEPLOY_CHECK.name(),
                    HeartBeatEventResultType.INIT.name(), JSON.toJSONString(configInfo), requestId);

            HeartBeatEventEntity newEventEntity = heartBeatEventRepository.save(eventEntity);

            instance.setCurrentEventId(newEventEntity.getId());
            clusterInstanceRepository.save(instance);
        } else {
            log.debug("check instance {} deploy heart beat event {} exist", instance.getId(), eventId);
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();
            // TODO:exception
            if (!eventEntity.getType().equals(HeartBeatEventType.INSTANCE_DEPLOY_CHECK.name())) {
                return;
            }

            // If the agent has been successfully installed, it cannot be installed again
            if (eventEntity.isCompleted() && eventEntity.getStatus().equals(HeartBeatEventResultType.SUCCESS.name())) {
                return;
            }

            eventEntity.setStatus(HeartBeatEventResultType.INIT.name());
            eventEntity.setConfigInfo(JSON.toJSONString(configInfo));

            heartBeatEventRepository.save(eventEntity);
        }
    }

    // Check whether instance is installed successfully
    public boolean checkInstanceOperation(ClusterInstanceEntity instance) {
        log.info("check instance {} has been deployed", instance.getId());
        long eventId = instance.getCurrentEventId();
        if (eventId < 1L) {
            return false;
        } else {
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();
            // TODO:exception
            if (!eventEntity.getType().equals(HeartBeatEventType.INSTANCE_DEPLOY_CHECK.name())) {
                return true;
            }

            // If the agent has been successfully installed, it cannot be installed again
            if (eventEntity.isCompleted() && eventEntity.getStatus().equals(HeartBeatEventResultType.SUCCESS.name())) {
                return true;
            }
            return false;
        }
    }

    public void startOperation(ClusterInstanceEntity instance, InstanceStartEventConfigInfo configInfo,
                               long requestId) {
        log.info("start instance {} for request {}", instance.getId(), requestId);
        saveInstanceNewHeartBeat(instance, JSON.toJSONString(configInfo),
                HeartBeatEventType.INSTANCE_START, requestId);
    }

    public void stopOperation(ClusterInstanceEntity instance, InstanceStopEventConfigInfo configInfo,
                               long requestId) {
        log.info("stop instance {} for request {}", instance.getId(), requestId);
        saveInstanceNewHeartBeat(instance, JSON.toJSONString(configInfo),
                HeartBeatEventType.INSTANCE_STOP, requestId);
    }

    public void restartOperation(ClusterInstanceEntity instance, InstanceRestartEventConfigInfo configInfo,
                                 long requestId) {
        log.info("restart instance {} for request {}", instance.getId(), requestId);
        saveInstanceNewHeartBeat(instance, JSON.toJSONString(configInfo),
                HeartBeatEventType.INSTANCE_RESTART, requestId);
    }

    public void deleteOperation(ClusterInstanceEntity instance) {
        log.info("delete instance {}", instance.getId());
        clusterInstanceRepository.delete(instance);
    }

    private void saveInstanceNewHeartBeat(ClusterInstanceEntity instance, String config, HeartBeatEventType eventType,
                                         long requestId) {
        HeartBeatEventEntity eventEntity = new HeartBeatEventEntity(eventType.name(),
                HeartBeatEventResultType.INIT.name(), config, requestId);

        HeartBeatEventEntity newEventEntity = heartBeatEventRepository.save(eventEntity);

        instance.setCurrentEventId(newEventEntity.getId());
        clusterInstanceRepository.save(instance);
    }

}
