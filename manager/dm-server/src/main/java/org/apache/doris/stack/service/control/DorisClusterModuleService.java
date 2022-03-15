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

import lombok.extern.slf4j.Slf4j;
import org.apache.doris.stack.component.ClusterUserComponent;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.ClusterModuleRepository;
import org.apache.doris.stack.dao.HeartBeatEventRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.apache.doris.stack.entity.ClusterModuleEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.model.response.control.ClusterInstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DorisClusterModuleService {
    @Autowired
    private ResourceNodeRepository nodeRepository;

    @Autowired
    private ClusterInstanceRepository instanceRepository;

    @Autowired
    private HeartBeatEventRepository heartBeatEventRepository;

    @Autowired
    private ClusterModuleRepository moduleRepository;

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Autowired
    private ClusterUserComponent userComponent;

    /**
     * TODO:Subsequent improvement
     * @return
     */
    public List<ClusterInstanceInfo> getClusterMoudleInstanceList(CoreUserEntity user, long moduleId) throws Exception {
        ClusterModuleEntity moduleEntity = moduleRepository.findById(moduleId).get();

        userComponent.checkUserSpuerAdminOrClusterAdmin(user, moduleEntity.getClusterId());

        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(moduleId);

        List<ClusterInstanceInfo> instanceInfos = new ArrayList<>();
        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            ClusterInstanceInfo instanceInfo = new ClusterInstanceInfo();
            instanceInfo.setModuleName(moduleEntity.getModuleName());
            instanceInfo.setInstanceId(instanceEntity.getId());

            long eventId = instanceEntity.getCurrentEventId();
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();

            instanceInfo.setOperateStatus(eventEntity.getStatus());
            instanceInfo.setOperateResult(eventEntity.getOperateResult());
            instanceInfo.setOperateStage(eventEntity.getStage());

            ResourceNodeEntity nodeEntity = nodeRepository.findById(instanceEntity.getNodeId()).get();
            instanceInfo.setNodeHost(nodeEntity.getHost());

            instanceInfos.add(instanceInfo);
        }
        return instanceInfos;
    }

}
