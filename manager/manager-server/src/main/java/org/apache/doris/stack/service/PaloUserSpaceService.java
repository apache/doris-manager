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

package org.apache.doris.stack.service;

import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.component.DorisManagerUserSpaceComponent;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.model.request.space.ClusterCreateReq;
import org.apache.doris.stack.model.request.space.NewUserSpaceCreateReq;
import org.apache.doris.stack.model.response.space.NewUserSpaceInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description：doris Manager Create user Doris cluster space when servicing
 */
@Service
@Slf4j
public class PaloUserSpaceService extends BaseService {

    @Autowired
    private DorisManagerUserSpaceComponent userSpaceComponent;

    @Transactional(rollbackFor = Exception.class)
    public long create(NewUserSpaceCreateReq createReq, CoreUserEntity user) throws Exception {
        return userSpaceComponent.create(createReq, user.getFirstName());
    }

    public ClusterInfoEntity validateCluster(ClusterCreateReq createReq) throws Exception {
        return userSpaceComponent.validateCluster(createReq);
    }

    /**
     * Change cluster space information
     * 1. Only modify the space name and other information;
     * 2. Add new cluster connection information;
     * TODO: Currently, modifying cluster information is not supported
     * @param user
     * @param spaceId
     * @param updateReq
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public NewUserSpaceInfo update(CoreUserEntity user, int spaceId, NewUserSpaceCreateReq updateReq) throws Exception {
        return userSpaceComponent.update(user, spaceId, updateReq);
    }

    public boolean nameCheck(String name) throws Exception {
        return userSpaceComponent.nameCheck(name);
    }

    /**
     * Get the list of spaces for which the user has permission
     * @param userEntity
     * @return
     */
    public List<NewUserSpaceInfo> getAllSpaceByUser(CoreUserEntity userEntity) {
        return userSpaceComponent.getAllSpaceByUser(userEntity);
    }

    public NewUserSpaceInfo getById(CoreUserEntity user, int spaceId) throws Exception {
        return userSpaceComponent.getById(user, spaceId);
    }

    /**
     * Delete a space's information
     * 1. Information of space itself
     * 2. Space permissions and user group information
     * 3. User  of space
     *
     * @param spaceId
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(int spaceId) throws Exception {
        userSpaceComponent.deleteSpace(spaceId);
    }
}
