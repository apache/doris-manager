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

package org.apache.doris.stack.control.request.handler;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.common.util.ServerAndAgentConstant;
import org.apache.doris.stack.control.ModelControlResponse;
import org.apache.doris.stack.control.manager.DorisClusterManager;
import org.apache.doris.stack.control.manager.ResourceClusterManager;
import org.apache.doris.stack.control.request.DorisClusterRequest;
import org.apache.doris.stack.control.request.DorisClusterRequestHandler;
import org.apache.doris.stack.control.request.content.DorisClusterTakeOverRequest;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.driver.JdbcSampleClient;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.model.request.control.DorisClusterModuleResourceConfig;
import org.apache.doris.stack.util.CredsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Subsequent improvement of request content judgment and exception handling
@Slf4j
@Component
public class DorisClusterTakeOverRequestHandler extends DorisClusterRequestHandler {

    @Autowired
    private DorisClusterManager dorisClusterManager;

    @Autowired
    private ResourceClusterManager resourceClusterManager;

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Autowired
    private ResourceNodeRepository nodeRepository;

    @Autowired
    private JdbcSampleClient jdbcClient;

    @Override
    public ModelControlResponse handleRequestEvent(CoreUserEntity user, DorisClusterRequest takeOverRequest,
                                                   boolean modelInit) throws Exception {
        DorisClusterTakeOverRequest request = (DorisClusterTakeOverRequest) takeOverRequest;
        switch (request.getEventType()) {
            case 1: // CREATE_CLUSTER_SPACE
                return handleCreateClusterSpaceEvent(user, request, modelInit);
            case 2: // ACCESS_DORIS_CLUSTER
                return handleAccessDorisClusterEvent(user, request);
            case 3: // CREATE_AND_START_RESOURCE_CLUSTER
                return handleCreateAndStartResourceClusterEvent(user, request);
            case 4: // CHECK_CLUSTER_DEPLOY
                return handleCheckClusterDeployEvent(user, request);
            case 5: // COMPLETED_TAKEOVER
                return handleCompletedEvent(user, request);
            default:
                log.error("Event type error.");
                throw new Exception("Event type error.");
        }
    }

    @Override
    public long initRequestModel(DorisClusterRequest request, String creator) throws Exception {
        log.info("init doris cluster, create cluster user space");
        DorisClusterTakeOverRequest takeOverRequest = (DorisClusterTakeOverRequest) request;
        return dorisClusterManager.initOperation(takeOverRequest.getReqInfo().getSpaceInfo(), creator);
    }

    // CREATE_CLUSTER_SPACE
    private ModelControlResponse handleCreateClusterSpaceEvent(CoreUserEntity user,
                                                               DorisClusterTakeOverRequest request,
                                                               boolean isInit) throws Exception {
        long clusterId = request.getClusterId();
        log.info("handle take over cluster {} CREATE_CLUSTER_SPACE request {} event", clusterId, request.getRequestId());
        if (!isInit) {
            log.info("The cluster user space already exist, update info.");
            dorisClusterManager.updateClusterOperation(user, clusterId,
                    request.getReqInfo().getSpaceInfo());
        }

        return getResponse(request, false);
    }

    // ACCESS_DORIS_CLUSTER
    private ModelControlResponse handleAccessDorisClusterEvent(CoreUserEntity user,
                                                               DorisClusterTakeOverRequest request) throws Exception {
        long clusterId = request.getClusterId();
        log.info("handle take over cluster {} ACCESS_DORIS_CLUSTER request {} event", clusterId, request.getRequestId());

        dorisClusterManager.clusterAccessOperation(clusterId, request.getReqInfo().getClusterAccessInfo());

        return getResponse(request, false);
    }

    // CREATE_AND_START_RESOURCE_CLUSTER
    private ModelControlResponse handleCreateAndStartResourceClusterEvent(CoreUserEntity user,
                                                                          DorisClusterTakeOverRequest request) throws Exception {
        long clusterId = request.getClusterId();
        log.info("handle take over cluster {} CREATE_AND_START_RESOURCE_CLUSTER request {} event",
                clusterId, request.getRequestId());

        ClusterInfoEntity clusterInfo = clusterInfoRepository.findById(clusterId).get();

        // TODO:get cluster nodes ip info
        List<String> nodeIps = new ArrayList<>();

        Statement stmt = jdbcClient.getStatement(clusterInfo.getAddress(), clusterInfo.getQueryPort(),
                clusterInfo.getUser(), CredsUtil.aesDecrypt(clusterInfo.getPasswd()));
        Set<String> feNodeIps = jdbcClient.getFeOrBeIps(stmt, "'/frontends';");
        log.debug("The node list IP of Doris cluster Fe is {}", feNodeIps);

        Set<String> beNodeIps = jdbcClient.getFeOrBeIps(stmt, "'/backends';");
        log.debug("The node list IP of Doris cluster Be is {}", beNodeIps);
        jdbcClient.closeStatement(stmt);

        Set<String> allNodeDistinct = new HashSet<>();
        allNodeDistinct.addAll(feNodeIps);
        allNodeDistinct.addAll(beNodeIps);

        log.debug("The node list distinct IP of Doris cluster is {}", allNodeDistinct);

        nodeIps.addAll(allNodeDistinct);
        log.debug("The node list IP of Doris cluster is {}", nodeIps);

        dorisClusterManager.createClusterResourceOperation(user, clusterInfo, request.getReqInfo().getAuthInfo(), nodeIps);
        dorisClusterManager.configClusterResourceOperation(clusterInfo, "",
                request.getReqInfo().getInstallInfo(), request.getReqInfo().getAgentPort());

        // TODO  sshInfo  and  iplist  can check agent port
        List<ResourceNodeEntity> nodeEntities =
                nodeRepository.getByResourceClusterId(clusterInfo.getResourceClusterId());
        Set<Long> feNodeIds = new HashSet<>();
        Set<Long> beNodeIds = new HashSet<>();
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            if (feNodeIps.contains(nodeEntity.getHost())) {
                feNodeIds.add(nodeEntity.getId());
            }

            if (beNodeIps.contains(nodeEntity.getHost())) {
                beNodeIds.add(nodeEntity.getId());
            }
        }
        log.debug("The node list ID of Doris cluster fe is {}", feNodeIds);
        log.debug("The node list ID of Doris cluster be is {}", beNodeIds);

        DorisClusterModuleResourceConfig feConfig = new DorisClusterModuleResourceConfig();
        feConfig.setModuleName(ServerAndAgentConstant.FE_NAME);
        feConfig.setNodeIds(feNodeIds);

        DorisClusterModuleResourceConfig beConfig = new DorisClusterModuleResourceConfig();
        beConfig.setModuleName(ServerAndAgentConstant.BE_NAME);
        beConfig.setNodeIds(beNodeIds);
        List<DorisClusterModuleResourceConfig> resourceConfigs = Lists.newArrayList(feConfig, beConfig);

        dorisClusterManager.scheduleClusterOperation(clusterId, resourceConfigs);

        dorisClusterManager.startClusterResourceOperation(clusterInfo, request.getRequestId());

        return getResponse(request, false);
    }

    // CHECK_CLUSTER_DEPLOY
    private ModelControlResponse handleCheckClusterDeployEvent(CoreUserEntity user,
                                                               DorisClusterTakeOverRequest request) throws Exception {
        long clusterId = request.getClusterId();
        log.info("handle take over cluster {} CHECK_CLUSTER_DEPLOY request {} event", clusterId, request.getRequestId());

        // Check whether the cluster node agent is installed successfully
        ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById(clusterId).get();
        resourceClusterManager.checkNodesAgentOperation(clusterInfoEntity.getResourceClusterId());

        dorisClusterManager.checkClusterDeployOperation(clusterId, request.getRequestId());

        return getResponse(request, false);
    }

    // COMPLETED_TAKEOVER
    private ModelControlResponse handleCompletedEvent(CoreUserEntity user,
                                                      DorisClusterTakeOverRequest request) throws Exception {
        long clusterId = request.getClusterId();
        log.info("handle take over cluster {} COMPLETED_TAKEOVER request {} event", clusterId, request.getRequestId());
        dorisClusterManager.checkClusterInstancesOperation(clusterId);
        return getResponse(request, true);
    }
}
