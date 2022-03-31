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
import org.apache.doris.manager.common.heartbeat.config.InstanceDeployCheckEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceInstallEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceRestartEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceStartEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceStopEventConfigInfo;
import org.apache.doris.manager.common.util.ConfigDefault;
import org.apache.doris.manager.common.util.ServerAndAgentConstant;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.ClusterModuleRepository;
import org.apache.doris.stack.dao.ClusterModuleServiceRepository;
import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.apache.doris.stack.entity.ClusterModuleEntity;
import org.apache.doris.stack.entity.ClusterModuleServiceEntity;
import org.apache.doris.stack.model.request.control.DeployConfigItem;
import org.apache.doris.stack.model.request.control.DorisClusterModuleDeployConfig;
import org.apache.doris.stack.model.request.control.DorisClusterModuleResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DorisClusterModuleManager {
    @Autowired
    private ClusterModuleRepository clusterModuleRepository;

    @Autowired
    private ClusterInstanceRepository instanceRepository;

    @Autowired
    private ClusterModuleServiceRepository serviceRepository;

    @Autowired
    private DorisClusterInstanceManager instanceManager;

    public long initOperation(long clusterId, DorisClusterModuleResourceConfig resourceConfig) {
        log.info("create module for cluster {}", clusterId);
        ClusterModuleEntity moduleEntity = new ClusterModuleEntity(clusterId, resourceConfig.getModuleName());

        ClusterModuleEntity newModuleEntity = clusterModuleRepository.save(moduleEntity);

        for (long nodeId : resourceConfig.getNodeIds()) {
            instanceManager.initOperation(clusterId, newModuleEntity, nodeId);
        }
        return newModuleEntity.getId();
    }

    public void configOperation(long clusterId, DorisClusterModuleDeployConfig deployConfig) {
        String moduleName = deployConfig.getModuleName();
        log.info("config module name {} for cluster {}", moduleName, clusterId);
        List<ClusterModuleEntity> moduleEntities = clusterModuleRepository.getByClusterIdAndModuleName(clusterId, moduleName);

        // Step fallback operation
        // If it has been configured before, you need to delete the service information
        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            serviceRepository.deleteByModuleId(moduleEntity.getId());
        }

        ClusterModuleEntity moduleEntity = moduleEntities.get(0);
        moduleEntity.setConfig(JSON.toJSONString(deployConfig));

        // add service for module
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(moduleEntity.getId());

        List<String> accessInfo = new ArrayList<>();
        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            accessInfo.add(instanceEntity.getAddress());
        }
        Map<String, Integer> serviceNamePorts = new HashMap<>();

        int editLogPort = 0;
        List<String> followerIp = new ArrayList<>();
        List<String> observerIps = new ArrayList<>();

        if (moduleName.equals(ServerAndAgentConstant.FE_NAME)) {
            // for fe service,jdbc and http
            Map<String, Integer> editServiceNamePort = new HashMap<>();
            for (DeployConfigItem configItem : deployConfig.getConfigs()) {
                if (configItem.getKey().equals(ConfigDefault.FE_HTTP_PORT_CONFIG_NAME)) {
                    serviceNamePorts.put(ServerAndAgentConstant.FE_HTTP_SERVICE, Integer.valueOf(configItem.getValue()));
                }

                if (configItem.getKey().equals(ConfigDefault.FE_QUERY_PORT_CONFIG_NAME)) {
                    serviceNamePorts.put(ServerAndAgentConstant.FE_JDBC_SERVICE, Integer.valueOf(configItem.getValue()));
                }

                if (configItem.getKey().equals(ConfigDefault.FE_EDIT_LOG_PORT)) {
                    editLogPort = Integer.valueOf(configItem.getValue());
                    editServiceNamePort.put(ServerAndAgentConstant.FE_EDIT_SERVICE, editLogPort);
                }
            }
            // Set follower or observer
            // Followers are stored in the extra information of instance
            int index = 0;
            String followerEndpoint = "";
            for (ClusterInstanceEntity instanceEntity : instanceEntities) {
                if (index < 1) {
                    followerEndpoint = instanceEntity.getAddress() + ":" + editLogPort;
                    followerIp.add(instanceEntity.getAddress());
                } else {
                    instanceEntity.setExtraInfo(followerEndpoint);
                    instanceRepository.save(instanceEntity);
                    observerIps.add(instanceEntity.getAddress());
                }
                index++;
            }
            serviceCreateOperation(moduleEntity, serviceNamePorts, followerIp);
            // TODO:Modify it when the Fe capacity is expanded
            serviceCreateOperation(moduleEntity, editServiceNamePort, observerIps);
        } else if (moduleName.equals(ServerAndAgentConstant.BE_NAME)) {
            // for be service, heartbeat
            for (DeployConfigItem configItem : deployConfig.getConfigs()) {
                if (configItem.getKey().equals(ConfigDefault.BE_HEARTBEAT_PORT_CONFIG_NAME)) {
                    serviceNamePorts.put(ServerAndAgentConstant.BE_HEARTBEAT_SERVICE, Integer.valueOf(configItem.getValue()));
                }
            }
            serviceCreateOperation(moduleEntity, serviceNamePorts, accessInfo);
        } else {
            // for broker service, rpc
            for (DeployConfigItem configItem : deployConfig.getConfigs()) {
                if (configItem.getKey().equals(ConfigDefault.BROKER_PORT_CONFIG_NAME)) {
                    serviceNamePorts.put(ServerAndAgentConstant.BROKER_PRC_SERVICE, Integer.valueOf(configItem.getValue()));
                }
            }
            serviceCreateOperation(moduleEntity, serviceNamePorts, accessInfo);
        }

        clusterModuleRepository.save(moduleEntity);
    }

    private void serviceCreateOperation(ClusterModuleEntity module, Map<String, Integer> serviceNamePorts,
                                       List<String> accessInfo) {
        log.info("create module {} service", module.getId());
        for (String name : serviceNamePorts.keySet()) {
            int port = serviceNamePorts.get(name);
            ClusterModuleServiceEntity serviceEntity = new ClusterModuleServiceEntity(name, module.getClusterId(),
                    module.getId(), port, JSON.toJSONString(accessInfo));
            serviceRepository.save(serviceEntity);
        }
    }

    public void deployOperation(ClusterModuleEntity module, long requestId) {
        // TODO:Step fallback operation
        log.info("deploy module {}", module.getId());
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(module.getId());
        DorisClusterModuleDeployConfig deployConfig = JSON.parseObject(module.getConfig(),
                DorisClusterModuleDeployConfig.class);

        InstanceInstallEventConfigInfo configInfo = new InstanceInstallEventConfigInfo();
        configInfo.setModuleName(module.getModuleName());
        configInfo.setPackageDir(deployConfig.getPackageDir());

        List<DeployConfigItem> configItems = deployConfig.getConfigs();
        for (DeployConfigItem configItem : configItems) {
            configInfo.addParm(configItem.getKey(), configItem.getValue());
        }

        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            configInfo.setInstallInfo(instanceEntity.getInstallInfo());
            configInfo.setFollowerEndpoint(instanceEntity.getExtraInfo());
            instanceManager.deployOperation(instanceEntity, configInfo, requestId);
        }
    }

    public void checkDeployOperation(ClusterModuleEntity module, long requestId) {
        // TODO:Step fallback operation
        log.info("check module {} deploy for request {}", module.getId(), requestId);
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(module.getId());

        InstanceDeployCheckEventConfigInfo configInfo = new InstanceDeployCheckEventConfigInfo();
        configInfo.setModuleName(module.getModuleName());

        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            configInfo.setInstallInfo(instanceEntity.getInstallInfo());
            instanceManager.checkDeployOperation(instanceEntity, configInfo, requestId);
        }
    }

    public void checkInstancesOperation(ClusterModuleEntity module) throws Exception {
        log.info("check module {} instances", module.getId());
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(module.getId());

        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            if (!instanceManager.checkInstanceOperation(instanceEntity)) {
                throw new Exception("Instance has not been started successfully and cannot proceed to the next step");
            }
        }
    }

    public void stopOperation(ClusterModuleEntity module, long requestId) {
        log.info("stop module {} for request {}", module.getId(), requestId);
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(module.getId());

        InstanceStopEventConfigInfo configInfo = new InstanceStopEventConfigInfo();
        configInfo.setModuleName(module.getModuleName());

        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            configInfo.setInstallInfo(instanceEntity.getInstallInfo());
            instanceManager.stopOperation(instanceEntity, configInfo, requestId);
        }
    }

    public void startOperation(ClusterModuleEntity module, long requestId) {
        log.info("start module {} for request {}", module.getId(), requestId);
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(module.getId());

        InstanceStartEventConfigInfo configInfo = new InstanceStartEventConfigInfo();
        configInfo.setModuleName(module.getModuleName());

        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            configInfo.setInstallInfo(instanceEntity.getInstallInfo());
            instanceManager.startOperation(instanceEntity, configInfo, requestId);
        }
    }

    public void restartOperation(ClusterModuleEntity module, long requestId) {
        log.info("restart module {} for request {}", module.getId(), requestId);
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(module.getId());

        InstanceRestartEventConfigInfo configInfo = new InstanceRestartEventConfigInfo();
        configInfo.setModuleName(module.getModuleName());

        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            configInfo.setInstallInfo(instanceEntity.getInstallInfo());
            instanceManager.restartOperation(instanceEntity, configInfo, requestId);
        }
    }

    public void deleteOperation(ClusterModuleEntity module) {
        log.info("delete module {}", module.getId());
        List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(module.getId());

        // delete all instances
        for (ClusterInstanceEntity instanceEntity : instanceEntities) {
            instanceManager.deleteOperation(instanceEntity);
        }

        // delete service
        serviceRepository.deleteByModuleId(module.getId());

        // delete module
        clusterModuleRepository.deleteById(module.getId());
    }

}
