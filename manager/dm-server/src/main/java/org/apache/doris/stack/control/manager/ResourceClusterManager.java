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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.doris.manager.common.heartbeat.config.AgentInstallEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.AgentUnInstallEventConfigInfo;
import org.apache.doris.stack.dao.ResourceClusterRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.entity.ResourceClusterEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.model.request.control.PMResourceClusterAccessInfo;
import org.apache.doris.stack.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class ResourceClusterManager {
    @Autowired
    private ResourceClusterRepository resourceClusterRepository;

    @Autowired
    private ResourceNodeRepository nodeRepository;

    @Autowired
    private ResourceNodeAndAgentManager nodeAndAgentManager;

    public long initOperation(int userId, PMResourceClusterAccessInfo authInfo, List<String> hosts) {
        log.info("create resource cluster");
        ResourceClusterEntity clusterEntity = new ResourceClusterEntity(String.valueOf(userId),
                JSON.toJSONString(authInfo));

        ResourceClusterEntity newClusterEntity = resourceClusterRepository.save(clusterEntity);
        long resourceClusterId = newClusterEntity.getId();

        log.debug("create resource cluster nodes is {}", hosts);
        for (String host : hosts) {
            nodeAndAgentManager.initOperation(resourceClusterId, host);
        }
        return resourceClusterId;
    }

    public void updateOperation(long resourceClusterId, int userId,
                                PMResourceClusterAccessInfo authInfo,
                                List<String> hosts) {
        log.info("update resource cluster {} info", resourceClusterId);
        ResourceClusterEntity clusterEntity = resourceClusterRepository.findById(resourceClusterId).get();

        clusterEntity.setAccessInfo(JSON.toJSONString(authInfo));
        clusterEntity.setUserId(String.valueOf(userId));
        resourceClusterRepository.save(clusterEntity);

        List<String> existHosts = nodeRepository.getHostsByResourceClusterId(resourceClusterId);
        log.debug("resource cluster {} exist nodes", existHosts);

        List<String> reduceList = ListUtil.getReduceList(hosts, existHosts);
        log.debug("resource cluster {} reduce nodes", reduceList);
        for (String host : reduceList) {
            // node agent maybe not installed yet
            // only delete cluster node db info
            nodeAndAgentManager.deleteOperation(resourceClusterId, host);
        }

        List<String> addList = ListUtil.getAddList(hosts, existHosts);
        log.debug("resource cluster {} add nodes", addList);
        for (String host : addList) {
            nodeAndAgentManager.initOperation(resourceClusterId, host);
        }
    }

    public void configOperation(long resourceClusterId, String packageInfo, String installInfo, int agentPort) {
        // TODO:The path can be set separately for each machine later
        log.info("config resource cluster {}", resourceClusterId);
        ResourceClusterEntity resourceClusterEntity = resourceClusterRepository.findById(resourceClusterId).get();
        resourceClusterEntity.setRegistryInfo(packageInfo);
        resourceClusterRepository.save(resourceClusterEntity);

        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            nodeEntity.setAgentInstallDir(installInfo);
            nodeEntity.setAgentPort(agentPort);
            nodeRepository.save(nodeEntity);
        }
    }

    public void startOperation(long resourceClusterId, long requestId) throws Exception {

        log.info("start resource cluster {} all nodes agent", resourceClusterId);
        ResourceClusterEntity clusterEntity = resourceClusterRepository.findById(resourceClusterId).get();
        PMResourceClusterAccessInfo accessInfo = JSON.parseObject(clusterEntity.getAccessInfo(),
                PMResourceClusterAccessInfo.class);
        // TODO:The path can be set separately for each machine later
        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);
        AgentInstallEventConfigInfo configInfo = new AgentInstallEventConfigInfo();
        configInfo.setSshUser(accessInfo.getSshUser());
        configInfo.setSshPort(accessInfo.getSshPort());
        configInfo.setSshKey(accessInfo.getSshKey());

        log.debug("check agent port for resource cluster {} all nodes", resourceClusterId);

        // before install and start agent, to check whether port is available or not，
        // it can not guarantee the port must not be used when starting the agent，
        // but it may expose this problem early if the port has been used.
        List<Pair<ResourceNodeEntity, CompletableFuture<Boolean>>> nodeFutures = new ArrayList<>();
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            CompletableFuture<Boolean> portCheckFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    nodeAndAgentManager.checkSshConnect(nodeEntity, configInfo);
                    return nodeAndAgentManager.isAvailableAgentPort(nodeEntity, configInfo);
                } catch (Exception e) {
                    log.error("check node {} exception: {}", nodeEntity.getHost(), e.getMessage());
                    throw new CompletionException(e);
                }
            });
            nodeFutures.add(Pair.of(nodeEntity, portCheckFuture));
        }

        boolean checkFailed = false;
        StringBuilder exStrBuilder = new StringBuilder();
        for (Pair<ResourceNodeEntity, CompletableFuture<Boolean>> nodeFuture: nodeFutures) {
            ResourceNodeEntity nodeEntity = nodeFuture.getLeft();
            CompletableFuture<Boolean> future = nodeFuture.getRight();
            try {
                boolean isAvailablePort = future.get();
                if (!isAvailablePort) {
                    checkFailed = true;
                    log.error("node {}:{} port already in use", nodeEntity.getHost(), nodeEntity.getAgentPort());
                    throw new Exception(String.format("node %s:%d port already in use",
                            nodeEntity.getHost(), nodeEntity.getAgentPort()));
                }
            } catch (Exception e) {
                checkFailed = true;
                log.error("node {}:{} check exception {}", nodeEntity.getHost(), nodeEntity.getAgentPort(), e);
                exStrBuilder.append(String.format("%s:%d, %s",
                        nodeEntity.getHost(), nodeEntity.getAgentPort(), e));
                exStrBuilder.append("\n");
            }
        }

        if (checkFailed) {
            log.error("check node exception list: {}\n", exStrBuilder);
            throw new Exception(exStrBuilder.toString());
        }

        log.debug("install agent for resource cluster {} all nodes", resourceClusterId);
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            log.info("start to install agent to {} node {}", nodeEntity.getId(), nodeEntity.getHost());
            nodeAndAgentManager.installAgentOperation(nodeEntity, configInfo, requestId);
        }
    }

    public void checkNodesAgentOperation(long resourceClusterId) throws Exception {
        log.info("check resource cluster {} all nodes agent", resourceClusterId);
        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);

        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            if (!nodeAndAgentManager.checkAgentOperation(nodeEntity)) {
                throw new Exception("The node agent has not been successfully installed. "
                        + "The next step cannot be carried out temporarily");
            }
        }
    }

    public void deleteOperation(long resourceClusterId) throws Exception {
        log.info("to delete resource cluster {} info", resourceClusterId);

        Optional<ResourceClusterEntity> resourceClusterOpt = resourceClusterRepository.findById(resourceClusterId);
        if (!resourceClusterOpt.isPresent()) {
            log.error("resource cluster {} does not exist", resourceClusterId);
            throw new Exception("resource cluster" + resourceClusterId + "does not exist");
        }

        ResourceClusterEntity clusterEntity =  resourceClusterOpt.get();

        List<ResourceNodeEntity> nodeList = nodeRepository.getByResourceClusterId(resourceClusterId);
        for (ResourceNodeEntity node : nodeList) {
            log.info("delete node {}, host: {}", node.getId(), node.getHost());
            nodeAndAgentManager.deleteOperation(node.getId());
        }

        log.info("delete resource cluster {}", resourceClusterId);
        resourceClusterRepository.delete(clusterEntity);
    }

    public void deleteAgentsOperation(long resourceClusterId) throws  Exception {
        log.info("delete resource cluster {} all nodes agent", resourceClusterId);

        Optional<ResourceClusterEntity> resourceClusterOpt = resourceClusterRepository.findById(resourceClusterId);
        if (!resourceClusterOpt.isPresent()) {
            throw new Exception("resource cluster " + resourceClusterId + " does not exist");
        }

        ResourceClusterEntity clusterEntity =  resourceClusterOpt.get();
        PMResourceClusterAccessInfo accessInfo = JSON.parseObject(clusterEntity.getAccessInfo(),
                PMResourceClusterAccessInfo.class);

        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);

        List<ResourceNodeEntity> agentInstalledNodes = new ArrayList<>();
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            if (!nodeAndAgentManager.checkAgentOperation(nodeEntity)) {
                log.warn("the agent has not been installed on {} node {}", nodeEntity.getId(), nodeEntity.getHost());
            } else {
                agentInstalledNodes.add(nodeEntity);
            }
        }

        // we check something before uninstall agent
        // to guarantee uninstall operation must be executed
        List<Pair<ResourceNodeEntity, CompletableFuture<Void>>> nodeFutures = new ArrayList<>();
        for (ResourceNodeEntity nodeEntity : agentInstalledNodes) {
            CompletableFuture<Void> portCheckFuture = CompletableFuture.runAsync(() -> {
                AgentInstallEventConfigInfo installConfig = new AgentInstallEventConfigInfo();
                installConfig.setSshUser(accessInfo.getSshUser());
                installConfig.setSshPort(accessInfo.getSshPort());
                installConfig.setSshKey(accessInfo.getSshKey());

                try {
                    log.info("check ssh connect and stop script before uninstall agent on node {}", nodeEntity.getId());
                    nodeAndAgentManager.checkSshConnect(nodeEntity, installConfig);
                    nodeAndAgentManager.checkStopScriptExist(nodeEntity, installConfig);
                } catch (Exception e) {
                    log.error("check node {} exception: {}", nodeEntity.getHost(), e.getMessage());
                    throw new CompletionException(e);
                }
            });
            nodeFutures.add(Pair.of(nodeEntity, portCheckFuture));
        }

        for (Pair<ResourceNodeEntity, CompletableFuture<Void>> nodeFuture: nodeFutures) {
            ResourceNodeEntity nodeEntity = nodeFuture.getLeft();
            CompletableFuture<Void> future = nodeFuture.getRight();
            try {
                future.get();
            } catch (Exception e) {
                log.error("check {} node {} stop script exception {}", nodeEntity.getId(), nodeEntity.getHost(), e);
                throw new Exception("check node stop script failed" + e);
            }
        }

        // async delete agent
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            AgentUnInstallEventConfigInfo uninstallConfig = new AgentUnInstallEventConfigInfo(
                    accessInfo.getSshUser(), accessInfo.getSshPort(), accessInfo.getSshKey(),
                    nodeEntity.getHost(), nodeEntity.getAgentInstallDir(),
                    nodeEntity.getId(), nodeEntity.getAgentPort());

            log.info("to stop agent of {} node {}", nodeEntity.getId(), nodeEntity.getHost());
            nodeAndAgentManager.deleteAgentOperation(nodeEntity, uninstallConfig);
        }
    }
}
