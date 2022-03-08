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
import org.apache.doris.manager.common.heartbeat.HeartBeatEventType;
import org.apache.doris.manager.common.heartbeat.config.InstanceInstallEventConfigInfo;
import org.apache.doris.stack.control.manager.DorisClusterInstanceManager;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.ClusterModuleRepository;
import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.apache.doris.stack.entity.ClusterModuleEntity;
import org.apache.doris.stack.model.request.control.DeployConfigItem;
import org.apache.doris.stack.model.request.control.DorisClusterModuleDeployConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DorisClusterInstanceService {
    @Autowired
    private ClusterModuleRepository moduleRepository;

    @Autowired
    private ClusterInstanceRepository instanceRepository;

    @Autowired
    private DorisClusterInstanceManager instanceManager;

    public void operateInstance(long instanceId, String operateType) throws Exception {

        HeartBeatEventType eventType = HeartBeatEventType.valueOf(operateType);

        // TODO:Currently, only agent install is implemented

        ClusterInstanceEntity instanceEntity = instanceRepository.findById(instanceId).get();
        ClusterModuleEntity module = moduleRepository.findById(instanceEntity.getModuleId()).get();

        if (eventType == HeartBeatEventType.INSTANCE_INSTALL) {
            DorisClusterModuleDeployConfig deployConfig = JSON.parseObject(module.getConfig(),
                    DorisClusterModuleDeployConfig.class);

            InstanceInstallEventConfigInfo configInfo = new InstanceInstallEventConfigInfo();
            configInfo.setModuleName(module.getModuleName());
            List<DeployConfigItem> configItems = deployConfig.getConfigs();
            for (DeployConfigItem configItem : configItems) {
                configInfo.addParm(configItem.getKey(), configItem.getValue());
            }
            configInfo.setPackageDir(deployConfig.getPackageDir());
            configInfo.setInstallInfo(instanceEntity.getInstallInfo());

            // TODO:Do not put the request ID temporarily
            instanceManager.deployOperation(instanceEntity, configInfo, 0L);
        } else if (eventType == HeartBeatEventType.INSTANCE_DEPLOY_CHECK) {
            // TODO:
            return;
        } else {
            throw new Exception("The instance operate type not support");
        }

    }

}
