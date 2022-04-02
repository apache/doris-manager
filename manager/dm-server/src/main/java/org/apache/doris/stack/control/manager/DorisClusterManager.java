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
import org.apache.doris.manager.common.util.ServerAndAgentConstant;
import org.apache.doris.stack.component.DorisManagerUserSpaceComponent;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.ClusterInstanceRepository;
import org.apache.doris.stack.dao.ClusterModuleRepository;
import org.apache.doris.stack.dao.ClusterModuleServiceRepository;
import org.apache.doris.stack.dao.ResourceClusterRepository;
import org.apache.doris.stack.driver.JdbcSampleClient;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.ClusterModuleEntity;
import org.apache.doris.stack.entity.ClusterModuleServiceEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.ResourceClusterEntity;
import org.apache.doris.stack.model.request.control.DorisClusterModuleDeployConfig;
import org.apache.doris.stack.model.request.control.DorisClusterModuleResourceConfig;
import org.apache.doris.stack.model.request.control.PMResourceClusterAccessInfo;
import org.apache.doris.stack.model.request.space.ClusterCreateReq;
import org.apache.doris.stack.model.request.space.ClusterType;
import org.apache.doris.stack.model.request.space.NewUserSpaceCreateReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class DorisClusterManager {

    @Autowired
    private DorisManagerUserSpaceComponent userSpaceComponent;

    @Autowired
    private ClusterInfoRepository clusterRepository;

    @Autowired
    private ClusterInstanceRepository instanceRepository;

    @Autowired
    private ClusterModuleRepository moduleRepository;

    @Autowired
    private ClusterModuleServiceRepository serviceRepository;

    @Autowired
    private ResourceClusterRepository resourceClusterRepository;

    @Autowired
    private ResourceClusterManager resourceClusterManager;

    @Autowired
    private DorisClusterModuleManager clusterModuleManager;

    @Autowired
    private JdbcSampleClient jdbcClient;

    // Ensure the data atomicity of creating user space, so add transactions
    @Transactional
    public long initOperation(NewUserSpaceCreateReq spaceInfo, String creator) throws Exception {
        return userSpaceComponent.create(spaceInfo, creator);
    }

    // Ensure the atomicity of data in user space, so add transactions
    @Transactional
    public void updateClusterOperation(CoreUserEntity user, long clusterId,
                                       NewUserSpaceCreateReq spaceInfo) throws Exception {
        userSpaceComponent.update(user, clusterId, spaceInfo);
    }

    public void createClusterResourceOperation(CoreUserEntity user, ClusterInfoEntity clusterInfoEntity,
                                               PMResourceClusterAccessInfo authInfo,
                                               List<String> hosts) {
        log.info("Create cluster {} resource cluster operation.", clusterInfoEntity.getId());
        long resourceClusterId = clusterInfoEntity.getResourceClusterId();
        if (resourceClusterId < 1L) {
            log.debug("Cluster {} resource cluster not exist, add a new one.", clusterInfoEntity.getId());
            resourceClusterId = resourceClusterManager.initOperation(user.getId(), authInfo, hosts);
            clusterInfoEntity.setResourceClusterId(resourceClusterId);
            clusterRepository.save(clusterInfoEntity);
        } else {
            log.debug("Cluster {} resource cluster {} already exist, update it.",
                    clusterInfoEntity.getId(), resourceClusterId);
            resourceClusterManager.updateOperation(resourceClusterId, user.getId(), authInfo, hosts);
        }
    }

    public void configClusterResourceOperation(ClusterInfoEntity clusterInfoEntity, String packageInfo,
                                               String installInfo, int agentPort) {
        log.info("Config cluster {} resource info operation.", clusterInfoEntity.getId());
        clusterInfoEntity.setInstallInfo(installInfo);
        clusterRepository.save(clusterInfoEntity);

        resourceClusterManager.configOperation(clusterInfoEntity.getResourceClusterId(), packageInfo,
                installInfo, agentPort);
    }

    public void startClusterResourceOperation(ClusterInfoEntity clusterInfoEntity, long requestId) throws Exception {
        log.info("Start cluster {} resource cluster operation.", clusterInfoEntity.getId());
        resourceClusterManager.startOperation(clusterInfoEntity.getResourceClusterId(), requestId);
    }

    public void scheduleClusterOperation(long clusterId, List<DorisClusterModuleResourceConfig> resourceConfigs) throws Exception {
        // Step fallback operation
        // If you have done scheduling and allocation before, you need to delete the created data.
        // If not, do nothing directly
        log.info("Schedule cluster {} operation.", clusterId);
        deleteClusterOperation(clusterId);

        // Add broker node installation information, which is available for each node by default
        DorisClusterModuleResourceConfig brokerConfig = new DorisClusterModuleResourceConfig();
        brokerConfig.setModuleName(ServerAndAgentConstant.BROKER_NAME);

        Set<Long> brokerNodes = new HashSet<>();
        for (DorisClusterModuleResourceConfig resourceConfig : resourceConfigs) {
            if (resourceConfig.getModuleName().equals(ServerAndAgentConstant.FE_NAME)) {
                int nodeCount = resourceConfig.getNodeIds().size();
                if (nodeCount % 2 == 0) {
                    log.error("The number {} of Fe cannot be even", nodeCount);
                    throw new Exception("The number of Fe cannot be even");
                }
            }
            brokerNodes.addAll(resourceConfig.getNodeIds());
            clusterModuleManager.initOperation(clusterId, resourceConfig);
        }
        brokerConfig.setNodeIds(brokerNodes);
        clusterModuleManager.initOperation(clusterId, brokerConfig);
    }

    public void configClusterOperation(ClusterInfoEntity clusterInfoEntity, List<DorisClusterModuleDeployConfig> deployConfigs) {
        log.info("Config cluster {} operation.", clusterInfoEntity.getId());

        ResourceClusterEntity resourceClusterEntity =
                resourceClusterRepository.findById(clusterInfoEntity.getResourceClusterId()).get();

        for (DorisClusterModuleDeployConfig deployConfig : deployConfigs) {
            deployConfig.setPackageDir(resourceClusterEntity.getRegistryInfo());
            clusterModuleManager.configOperation(clusterInfoEntity.getId(), deployConfig);
        }
    }

    public void deployClusterOperation(long clusterId, long requestId) {
        // TODO:Step fallback operation
        log.info("Deploy cluster {} operation.", clusterId);

        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            clusterModuleManager.deployOperation(moduleEntity, requestId);
        }
    }

    /**
     * Operations after creating a new cluster include adding be nodes and broker nodes, and changing passwords
     * @param clusterId
     * @param newPassword
     * @return
     * @throws Exception
     */
    public ClusterCreateReq deployClusterAfterOperation(long clusterId, String newPassword) throws Exception {
        log.info("Deploy cluster {} after operation.", clusterId);
        List<ClusterModuleServiceEntity> serviceEntities = serviceRepository.getByClusterId(clusterId);

        int feJdbcPort = 0;
        int feHttpPort = 0;
        int feEditPort = 0;
        int beHeartPort = 0;
        int brokerRpcPort = 0;
        List<String> feAccessInfo = new ArrayList<>();
        List<String> beAccessInfo = new ArrayList<>();
        List<String> brokerAccessInfo = new ArrayList<>();
        List<String> feObserverInfo = new ArrayList<>();
        for (ClusterModuleServiceEntity serviceEntity : serviceEntities) {
            if (serviceEntity.getName().equals(ServerAndAgentConstant.FE_JDBC_SERVICE)) {
                feJdbcPort = serviceEntity.getPort();
                feAccessInfo = JSON.parseArray(serviceEntity.getAddressInfo(), String.class);
            }
            if (serviceEntity.getName().equals(ServerAndAgentConstant.FE_EDIT_SERVICE)) {
                feEditPort = serviceEntity.getPort();
                feObserverInfo = JSON.parseArray(serviceEntity.getAddressInfo(), String.class);
            }
            if (serviceEntity.getName().equals(ServerAndAgentConstant.FE_HTTP_SERVICE)) {
                feHttpPort = serviceEntity.getPort();
            }
            if (serviceEntity.getName().equals(ServerAndAgentConstant.BE_HEARTBEAT_SERVICE)) {
                beHeartPort = serviceEntity.getPort();
                beAccessInfo = JSON.parseArray(serviceEntity.getAddressInfo(), String.class);
            }
            if (serviceEntity.getName().equals(ServerAndAgentConstant.BROKER_PRC_SERVICE)) {
                brokerRpcPort = serviceEntity.getPort();
                brokerAccessInfo = JSON.parseArray(serviceEntity.getAddressInfo(), String.class);
            }
        }

        log.debug("Get doris jdbc connection");
        // get doris jdbc connection
        String feHost = feAccessInfo.get(0);
        Statement stmt = jdbcClient.getStatement(feHost, feJdbcPort, ServerAndAgentConstant.USER_ROOT, "");
        // add fe Observer
        log.debug("Add fe Observers {}", feObserverInfo);
        List<String> feObserverHostsPorts = new ArrayList<>();
        for (String feObserverHost : feObserverInfo) {
            String feObserverHostsPort = feObserverHost + ":" + feEditPort;
            feObserverHostsPorts.add(feObserverHostsPort);
        }
        jdbcClient.addFeObserver(feObserverHostsPorts, stmt);

        // add be
        log.debug("Add be {}", beAccessInfo);
        List<String> beHostsPorts = new ArrayList<>();
        for (String beHost : beAccessInfo) {
            String beHostsPort = beHost + ":" + beHeartPort;
            beHostsPorts.add(beHostsPort);
        }
        jdbcClient.addBe(beHostsPorts, stmt);

        // add broker
        log.debug("Add broker {}", brokerAccessInfo);
        List<String> brokerHostsPorts = new ArrayList<>();
        for (String brokerHost : brokerAccessInfo) {
            String brokerHostsPort = brokerHost + ":" + brokerRpcPort;
            brokerHostsPorts.add(brokerHostsPort);
        }
        jdbcClient.addBrokerName(brokerHostsPorts, stmt);

        // update password
        log.debug("Update doris root and admin user default password.");
        jdbcClient.updateUserPassword(ServerAndAgentConstant.USER_ADMIN, newPassword, stmt);
        jdbcClient.updateUserPassword(ServerAndAgentConstant.USER_ROOT, newPassword, stmt);

        // close jdbc connection
        jdbcClient.closeStatement(stmt);

        // get cluster fe access info
        ClusterCreateReq clusterAccessInfo = new ClusterCreateReq();
        clusterAccessInfo.setPasswd(newPassword);
        clusterAccessInfo.setAddress(feHost);
        clusterAccessInfo.setUser(ServerAndAgentConstant.USER_ADMIN);
        clusterAccessInfo.setQueryPort(feJdbcPort);
        clusterAccessInfo.setHttpPort(feHttpPort);
        clusterAccessInfo.setType(ClusterType.Doris);
        return clusterAccessInfo;
    }

    public void clusterAccessOperation(long clusterId, ClusterCreateReq clusterAccessInfo) throws Exception {
        log.info("Access cluster {} operation.", clusterId);
        ClusterInfoEntity clusterInfo = clusterRepository.findById(clusterId).get();
        userSpaceComponent.clusterAccess(clusterAccessInfo, clusterInfo);
    }

    public void checkClusterDeployOperation(long clusterId, long requestId) throws Exception {
        log.info("Check cluster {} deploy operation.", clusterId);
        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            clusterModuleManager.checkDeployOperation(moduleEntity, requestId);
        }
    }

    public void checkClusterInstancesOperation(long clusterId) throws Exception {
        log.info("Check cluster {} instances operation.", clusterId);
        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            clusterModuleManager.checkInstancesOperation(moduleEntity);
        }
    }

    public void stopClusterOperation(long clusterId, long requestId) throws Exception {
        log.info("Stop cluster {} instances operation.", clusterId);
        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            clusterModuleManager.stopOperation(moduleEntity, requestId);
        }
    }

    public void startClusterOperation(long clusterId, long requestId) throws Exception {
        log.info("Start cluster {} instances operation.", clusterId);
        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            clusterModuleManager.startOperation(moduleEntity, requestId);
        }
    }

    public void reStartClusterOperation(long clusterId, long requestId) throws Exception {
        log.info("Restart cluster {} instances operation.", clusterId);
        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            clusterModuleManager.restartOperation(moduleEntity, requestId);
        }
    }

    public void deleteClusterOperation(ClusterInfoEntity clusterInfo)throws Exception {
        long clusterId = clusterInfo.getId();
        log.info("Delete cluster {} instances operation.", clusterId);
        deleteClusterOperation(clusterId);
    }

    private void deleteClusterOperation(long clusterId)throws Exception {
        List<ClusterModuleEntity> moduleEntities = moduleRepository.getByClusterId(clusterId);

        for (ClusterModuleEntity moduleEntity : moduleEntities) {
            clusterModuleManager.deleteOperation(moduleEntity);
        }
    }

}
