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

import lombok.extern.slf4j.Slf4j;
import org.apache.doris.stack.control.ModelControlResponse;
import org.apache.doris.stack.control.manager.DorisClusterManager;
import org.apache.doris.stack.control.manager.ResourceClusterManager;
import org.apache.doris.stack.control.request.DorisClusterRequest;
import org.apache.doris.stack.control.request.DorisClusterRequestHandler;
import org.apache.doris.stack.control.request.content.DorisClusterCreationRequest;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.model.request.space.ClusterCreateReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: Subsequent improvement of request content judgment and exception handling
@Slf4j
@Component
public class DorisClusterCreationRequestHandler extends DorisClusterRequestHandler {

    @Autowired
    private DorisClusterManager dorisClusterManager;

    @Autowired
    private ResourceClusterManager resourceClusterManager;

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Override
    public ModelControlResponse handleRequestEvent(CoreUserEntity user, DorisClusterRequest creationRequest, boolean modelInit) throws Exception {
        // TODO:How to define enumeration constants
        DorisClusterCreationRequest request = (DorisClusterCreationRequest) creationRequest;
        switch (request.getEventType()) {
            case 1: // CREATE_CLUSTER_SPACE
                return handleCreateClusterSpaceEvent(user, request, modelInit);
            case 2: // CREATE_RESOURCE_CLUSTER
                return handleCreateResourceClusterEvent(user, request);
            case 3: // CONFIG_AND_START_RESOURCE_CLUSTER
                return handleConfigAndStartResourceClusterEvent(user, request);
            case 4: // RESOURCE_CLUSTER_STARTED
                return handleResourceClusterStartedEvent(user, request);
            case 5: // SCHEDULE_DORIS_CLUSTER
                return handleScheduleDorisClusterEvent(user, request);
            case 6: // CONFIG_AND_DEPLOY_DORIS_CLUSTER
                return handleConfigAndDeployDorisClusterEvent(user, request);
            case 7: // DORIS_CLUSTER_DEPLOYED
                return handleDorisClusterDeployedEvent(user, request);
            case 8: // ACCESS_DORIS_CLUSTER
                return handleAccessDorisClusterEvent(user, request);
            default:
                log.error("Event type error.");
                throw new Exception("Event type error.");
        }
    }

    @Override
    public long initRequestModel(DorisClusterRequest request, String creator) throws Exception {
        DorisClusterCreationRequest creationRequest = (DorisClusterCreationRequest) request;
        return dorisClusterManager.initOperation(creationRequest.getReqInfo().getSpaceInfo(), creator);
    }

    // CREATE_CLUSTER_SPACE
    private ModelControlResponse handleCreateClusterSpaceEvent(CoreUserEntity user,
                                                               DorisClusterCreationRequest request,
                                                               boolean isInit) throws Exception {
        long clusterId = request.getClusterId();
        if (!isInit) {
            dorisClusterManager.updateClusterOperation(user, clusterId,
                    request.getReqInfo().getSpaceInfo());
        }

        return getResponse(request, false);
    }

    // CREATE_RESOURCE_CLUSTER
    private ModelControlResponse handleCreateResourceClusterEvent(CoreUserEntity user,
                                                                  DorisClusterCreationRequest request) {
        long clusterId = request.getClusterId();
        ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById(clusterId).get();
        dorisClusterManager.createClusterResourceOperation(user, clusterInfoEntity, request.getReqInfo().getAuthInfo(),
                request.getReqInfo().getHosts());

        return getResponse(request, false);
    }

    // CONFIG_AND_START_RESOURCE_CLUSTER
    private ModelControlResponse handleConfigAndStartResourceClusterEvent(CoreUserEntity user,
                                                                          DorisClusterCreationRequest request)
            throws Exception {
        long clusterId = request.getClusterId();
        ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById(clusterId).get();
        dorisClusterManager.configClusterResourceOperation(clusterInfoEntity, request.getReqInfo().getPackageInfo(),
                request.getReqInfo().getInstallInfo(), request.getReqInfo().getAgentPort());
        dorisClusterManager.startClusterResourceOperation(clusterInfoEntity, request.getRequestId());

        return getResponse(request, false);
    }

    // RESOURCE_CLUSTER_STARTED
    private ModelControlResponse handleResourceClusterStartedEvent(CoreUserEntity user,
                                                                   DorisClusterCreationRequest request) throws Exception {
        long clusterId = request.getClusterId();
        ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById(clusterId).get();
        resourceClusterManager.checkNodesAgentOperation(clusterInfoEntity.getResourceClusterId());
        return getResponse(request, false);
    }

    // SCHEDULE_DORIS_CLUSTER
    private ModelControlResponse handleScheduleDorisClusterEvent(CoreUserEntity user,
                                                                 DorisClusterCreationRequest request) throws Exception {
        long clusterId = request.getClusterId();

        dorisClusterManager.scheduleClusterOperation(clusterId, request.getReqInfo().getNodeConfig());

        return getResponse(request, false);
    }

    // CONFIG_AND_DEPLOY_DORIS_CLUSTER
    private ModelControlResponse handleConfigAndDeployDorisClusterEvent(CoreUserEntity user,
                                                                        DorisClusterCreationRequest request) {
        long clusterId = request.getClusterId();

        ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById(clusterId).get();
        dorisClusterManager.configClusterOperation(clusterInfoEntity, request.getReqInfo().getDeployConfigs());
        dorisClusterManager.deployClusterOperation(clusterId, request.getRequestId());

        return getResponse(request, false);
    }

    // DORIS_CLUSTER_DEPLOYED
    private ModelControlResponse handleDorisClusterDeployedEvent(CoreUserEntity user,
                                                                 DorisClusterCreationRequest request) throws Exception {
        long clusterId = request.getClusterId();
        dorisClusterManager.checkClusterInstancesOperation(clusterId);
        return getResponse(request, false);
    }

    // ACCESS_DORIS_CLUSTER
    private ModelControlResponse handleAccessDorisClusterEvent(CoreUserEntity user,
                                                               DorisClusterCreationRequest request) throws Exception {
        long clusterId = request.getClusterId();

        String newPassword = request.getReqInfo().getClusterPassword();
        ClusterCreateReq clusterAccessInfo = dorisClusterManager.deployClusterAfterOperation(clusterId, newPassword);

        dorisClusterManager.clusterAccessOperation(clusterId, clusterAccessInfo);

        return getResponse(request, true);
    }

}
