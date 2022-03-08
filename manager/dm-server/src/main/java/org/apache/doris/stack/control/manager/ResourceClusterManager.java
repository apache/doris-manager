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
import org.apache.doris.manager.common.heartbeat.config.AgentInstallEventConfigInfo;
import org.apache.doris.stack.dao.ResourceClusterRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.entity.ResourceClusterEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.model.request.control.PMResourceClusterAccessInfo;
import org.apache.doris.stack.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
        ResourceClusterEntity clusterEntity = new ResourceClusterEntity(String.valueOf(userId),
                JSON.toJSONString(authInfo));

        ResourceClusterEntity newClusterEntity = resourceClusterRepository.save(clusterEntity);
        long resourceClusterId = newClusterEntity.getId();

        for (String host : hosts) {
            nodeAndAgentManager.initOperation(resourceClusterId, host);
        }
        return resourceClusterId;
    }

    public void updateOperation(long resourceClusterId, int userId,
                                PMResourceClusterAccessInfo authInfo,
                                List<String> hosts) {
        ResourceClusterEntity clusterEntity = resourceClusterRepository.findById(resourceClusterId).get();

        clusterEntity.setAccessInfo(JSON.toJSONString(authInfo));
        clusterEntity.setUserId(String.valueOf(userId));
        resourceClusterRepository.save(clusterEntity);

        List<String> existHosts = nodeRepository.getHostsByResourceClusterId(resourceClusterId);

        List<String> reduceList = ListUtil.getReduceList(hosts, existHosts);
        for (String host : reduceList) {
            nodeAndAgentManager.deleteOperation(resourceClusterId, host);
        }

        List<String> addList = ListUtil.getAddList(hosts, existHosts);
        for (String host : addList) {
            nodeAndAgentManager.initOperation(resourceClusterId, host);
        }
    }

    public void configOperation(long resourceClusterId, String packageInfo, String installInfo) {
        // TODO:The path can be set separately for each machine later
        ResourceClusterEntity resourceClusterEntity = resourceClusterRepository.findById(resourceClusterId).get();
        resourceClusterEntity.setRegistryInfo(packageInfo);

        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);
        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            nodeEntity.setAgentInstallDir(installInfo);
            nodeRepository.save(nodeEntity);
        }
    }

    public void startOperation(long resourceClusterId, long requestId) {
        ResourceClusterEntity clusterEntity = resourceClusterRepository.findById(resourceClusterId).get();
        PMResourceClusterAccessInfo accessInfo = JSON.parseObject(clusterEntity.getAccessInfo(),
                PMResourceClusterAccessInfo.class);
        // TODO:The path can be set separately for each machine later
        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);
        AgentInstallEventConfigInfo configInfo = new AgentInstallEventConfigInfo();
        configInfo.setSshUser(accessInfo.getSshUser());
        configInfo.setSshPort(accessInfo.getSshPort());
        configInfo.setSshKey(accessInfo.getSshKey());

        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            nodeAndAgentManager.installAgentOperation(nodeEntity, configInfo, requestId);
        }
    }

    public void checkNodesAgentOperation(long resourceClusterId) throws Exception {
        List<ResourceNodeEntity> nodeEntities = nodeRepository.getByResourceClusterId(resourceClusterId);

        for (ResourceNodeEntity nodeEntity : nodeEntities) {
            if (!nodeAndAgentManager.checkAgentOperation(nodeEntity)) {
                throw new Exception("The node agent has not been successfully installed. "
                        + "The next step cannot be carried out temporarily");
            }
        }

    }
}
