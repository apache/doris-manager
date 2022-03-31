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
import org.apache.doris.manager.common.util.ConfigDefault;
import org.apache.doris.manager.common.util.ServerAndAgentConstant;
import org.apache.doris.stack.component.ClusterUserComponent;
import org.apache.doris.stack.control.ModelControlRequestType;
import org.apache.doris.stack.control.ModelControlResponse;
import org.apache.doris.stack.control.request.DorisClusterRequest;
import org.apache.doris.stack.control.request.content.DorisClusterCreationRequest;
import org.apache.doris.stack.control.request.content.DorisClusterTakeOverRequest;
import org.apache.doris.stack.control.request.handler.DorisClusterCreationRequestHandler;
import org.apache.doris.stack.control.request.handler.DorisClusterRestartRequestHandler;
import org.apache.doris.stack.control.request.handler.DorisClusterStartRequestHandler;
import org.apache.doris.stack.control.request.handler.DorisClusterStopRequestHandler;
import org.apache.doris.stack.control.request.handler.DorisClusterTakeOverRequestHandler;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.ClusterModuleRepository;
import org.apache.doris.stack.dao.ClusterModuleServiceRepository;
import org.apache.doris.stack.dao.HeartBeatEventRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.driver.JdbcSampleClient;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.apache.doris.stack.entity.ClusterModuleEntity;
import org.apache.doris.stack.entity.ClusterModuleServiceEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.model.request.control.DeployConfigItem;
import org.apache.doris.stack.model.request.control.DorisClusterCreationReq;
import org.apache.doris.stack.model.request.control.DorisClusterModuleDeployConfig;
import org.apache.doris.stack.model.request.control.DorisClusterTakeOverReq;
import org.apache.doris.stack.model.response.control.ClusterInstanceInfo;
import org.apache.doris.stack.model.response.control.ClusterModuleInfo;
import org.apache.doris.stack.model.response.control.ResourceNodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DorisClusterService {

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Autowired
    private HeartBeatEventRepository heartBeatEventRepository;

    @Autowired
    private ResourceNodeRepository nodeRepository;

    @Autowired
    private ClusterModuleRepository moduleRepository;

    @Autowired
    private ClusterInstanceRepository instanceRepository;

    @Autowired
    private ClusterModuleServiceRepository serviceRepository;

    @Autowired
    private DorisClusterCreationRequestHandler creationRequestHandler;

    @Autowired
    private DorisClusterTakeOverRequestHandler takeOverRequestHandler;

    @Autowired
    private DorisClusterStopRequestHandler stopRequestHandler;

    @Autowired
    private DorisClusterStartRequestHandler startRequestHandler;

    @Autowired
    private DorisClusterRestartRequestHandler restartRequestHandler;

    @Autowired
    private JdbcSampleClient jdbcSampleClient;

    @Autowired
    private ClusterUserComponent userComponent;

    public ModelControlResponse creation(CoreUserEntity user, DorisClusterCreationReq creationReq) throws Exception {
        log.info("Rquest info is {}", JSON.toJSON(creationReq));

        DorisClusterCreationRequest request = new DorisClusterCreationRequest();
        request.setType(ModelControlRequestType.CREATION);
        request.setReqInfo(creationReq);
        request.setClusterId(creationReq.getClusterId());
        request.setRequestId(creationReq.getRequestId());
        request.setEventType(creationReq.getEventType());

        ModelControlResponse response = creationRequestHandler.handleRequest(user, request);
        return response;
    }

    public ModelControlResponse takeOver(CoreUserEntity user, DorisClusterTakeOverReq takeOverReq) throws Exception {
        log.info("Rquest info is {}", JSON.toJSON(takeOverReq));
        DorisClusterTakeOverRequest request = new DorisClusterTakeOverRequest();
        request.setType(ModelControlRequestType.TAKE_OVER);
        request.setReqInfo(takeOverReq);
        request.setClusterId(takeOverReq.getClusterId());
        request.setRequestId(takeOverReq.getRequestId());
        request.setEventType(takeOverReq.getEventType());

        ModelControlResponse response = takeOverRequestHandler.handleRequest(user, request);
        return response;
    }

    public ModelControlResponse stopCluster(CoreUserEntity user, long clusterId) throws Exception {
        userComponent.checkUserSpuerAdminOrClusterAdmin(user, clusterId);
        DorisClusterRequest request = new DorisClusterRequest();
        request.setType(ModelControlRequestType.STOP);
        request.setClusterId(clusterId);
        request.setRequestId(0);
        ModelControlResponse response = stopRequestHandler.handleRequest(user, request);
        return response;
    }

    public ModelControlResponse startCluster(CoreUserEntity user, long clusterId) throws Exception {
        userComponent.checkUserSpuerAdminOrClusterAdmin(user, clusterId);
        DorisClusterRequest request = new DorisClusterRequest();
        request.setType(ModelControlRequestType.START);
        request.setClusterId(clusterId);
        request.setRequestId(0);
        ModelControlResponse response = startRequestHandler.handleRequest(user, request);
        return response;
    }

    public ModelControlResponse restartCluster(CoreUserEntity user, long clusterId) throws Exception {
        userComponent.checkUserSpuerAdminOrClusterAdmin(user, clusterId);
        DorisClusterRequest request = new DorisClusterRequest();
        request.setType(ModelControlRequestType.RESTART);
        request.setClusterId(clusterId);
        request.setRequestId(0);
        ModelControlResponse response = restartRequestHandler.handleRequest(user, request);
        return response;
    }

    /**
     * TODO:Subsequent improvement
     * @return
     */
    public List<ClusterModuleInfo> getClusterModules(CoreUserEntity user, long clusterId) throws Exception {
        userComponent.checkUserSpuerAdminOrClusterAdmin(user, clusterId);

        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        ClusterInfoEntity cluster = clusterInfoRepository.findById(clusterId).get();

        List<ClusterModuleInfo> moduleInfos = new ArrayList<>();
        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            ClusterModuleInfo moduleInfo = new ClusterModuleInfo();
            moduleInfo.setModuleId(moduleEntity.getId());
            moduleInfo.setName(moduleEntity.getModuleName());

            // If it has been configured, the configuration information is used.
            // If it has not been configured, the default configuration information is used
            String configInfo = moduleEntity.getConfig();
            if (configInfo == null || configInfo.isEmpty()) {
                moduleInfo.setConfig(getModuleDefaultConfig(moduleEntity.getModuleName(), cluster.getInstallInfo()));
            } else {
                DorisClusterModuleDeployConfig config = JSON.parseObject(configInfo, DorisClusterModuleDeployConfig.class);
                moduleInfo.setConfig(config.getConfigs());
            }
            moduleInfos.add(moduleInfo);

        }
        return moduleInfos;
    }

    /**
     * TODO:Subsequent improvement
     * @return
     */
    public List<ClusterInstanceInfo> getClusterInstances(CoreUserEntity user, long clusterId) throws Exception {
        userComponent.checkUserSpuerAdminOrClusterAdmin(user, clusterId);

        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        List<ClusterInstanceInfo> instanceInfos = new ArrayList<>();

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            List<ClusterInstanceEntity> instanceEntities = instanceRepository.getByModuleId(moduleEntity.getId());
            for (ClusterInstanceEntity instanceEntity : instanceEntities) {
                ClusterInstanceInfo instanceInfo = new ClusterInstanceInfo();
                instanceInfo.setModuleName(moduleEntity.getModuleName());
                instanceInfo.setInstanceId(instanceEntity.getId());
                instanceInfo.setNodeHost(instanceEntity.getAddress());

                long eventId = instanceEntity.getCurrentEventId();
                if (eventId > 0) {
                    HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();

                    instanceInfo.setOperateStatus(eventEntity.getStatus());
                    instanceInfo.setOperateResult(eventEntity.getOperateResult());
                    instanceInfo.setOperateStage(eventEntity.getStage());
                }

                instanceInfos.add(instanceInfo);
            }
        }
        return instanceInfos;
    }

    /**
     * TODO:Subsequent improvement
     * @return
     */
    public List<ResourceNodeInfo> getClusterResourceNodes(CoreUserEntity user, long clusterId) throws Exception {
        userComponent.checkUserSpuerAdminOrClusterAdmin(user, clusterId);

        ClusterInfoEntity clusterInfo = clusterInfoRepository.findById(clusterId).get();
        long resourceClusterId = clusterInfo.getResourceClusterId();

        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);

        List<ResourceNodeInfo> nodeInfos = new ArrayList<>();
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            ResourceNodeInfo nodeInfo = new ResourceNodeInfo();
            nodeInfo.setNodeId(nodeEntity.getId());
            nodeInfo.setHost(nodeEntity.getHost());

            long eventId = nodeEntity.getCurrentEventId();
            if (eventId > 0) {
                HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();

                nodeInfo.setOperateStatus(eventEntity.getStatus());
                nodeInfo.setOperateStage(eventEntity.getStage());
                nodeInfo.setOperateResult(eventEntity.getOperateResult());
            }

            nodeInfos.add(nodeInfo);
        }
        return nodeInfos;
    }

    public boolean checkJdbcServiceReady(CoreUserEntity user, long clusterId) throws Exception {
        userComponent.checkUserSpuerAdminOrClusterAdmin(user, clusterId);
        ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById(clusterId).get();
        if (clusterInfoEntity.getAddress() != null && !clusterInfoEntity.getAddress().isEmpty()) {
            log.info("The cluster {} is already access user space.", clusterId);
            return true;
        }

        List<ClusterModuleServiceEntity> jdbcService =
                serviceRepository.getByClusterIdAndName(clusterId, ServerAndAgentConstant.FE_JDBC_SERVICE);
        if (jdbcService.isEmpty()) {
            log.warn("The cluster {} no have jdbc service", clusterId);
            return false;
        }

        ClusterModuleServiceEntity serviceEntity = jdbcService.get(0);
        List<String> accessInfo = JSON.parseArray(serviceEntity.getAddressInfo(), String.class);
        int feJdbcPort = serviceEntity.getPort();

        return jdbcSampleClient.testConnetion(accessInfo.get(0), feJdbcPort, ServerAndAgentConstant.USER_ROOT, "");
    }

    private List<DeployConfigItem> getModuleDefaultConfig(String moduleName, String installInfo) {
        // get default config
        // TODO: Later, it is implemented in module template
        List<DeployConfigItem> configItems = new ArrayList<>();
        if (moduleName.equals(ServerAndAgentConstant.BE_NAME)) {
            for (String key : ConfigDefault.BE_CONFIG_DEDAULT.keySet()) {
                DeployConfigItem configItem = new DeployConfigItem();
                String value = ConfigDefault.BE_CONFIG_DEDAULT.get(key);
                if (key.equals(ConfigDefault.BE_LOG_CONFIG_NAME) || key.equals(ConfigDefault.BE_DATA_CONFIG_NAME)) {
                    value = installInfo + "/" + ServerAndAgentConstant.BE_NAME + value;
                }
                configItem.setKey(key);
                configItem.setValue(value);

                configItems.add(configItem);
            }
        } else if (moduleName.equals(ServerAndAgentConstant.FE_NAME)) {
            for (String key : ConfigDefault.FE_CONFIG_DEDAULT.keySet()) {
                DeployConfigItem configItem = new DeployConfigItem();
                String value = ConfigDefault.FE_CONFIG_DEDAULT.get(key);
                if (key.equals(ConfigDefault.FE_LOG_CONFIG_NAME) || key.equals(ConfigDefault.FE_META_CONFIG_NAME)) {
                    value = installInfo + "/" + ServerAndAgentConstant.FE_NAME + value;
                }
                configItem.setKey(key);
                configItem.setValue(value);

                configItems.add(configItem);
            }
        } else {
            for (String key : ConfigDefault.BROKER_CONFIG_DEDAULT.keySet()) {
                DeployConfigItem configItem = new DeployConfigItem();
                String value = ConfigDefault.BROKER_CONFIG_DEDAULT.get(key);
                configItem.setKey(key);
                configItem.setValue(value);

                configItems.add(configItem);
            }
        }
        return configItems;
    }

}
