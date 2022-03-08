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

package org.apache.doris.stack.connector;

import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.exception.PaloRequestException;
import org.apache.doris.stack.model.palo.DorisNodes;
import org.apache.doris.stack.model.palo.PaloResponseEntity;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

// Get a list of fe and be nodes of the current palo cluster by fe http api.
@Component
@Slf4j
public class DorisNodesClient extends PaloClient {
    private static final String NODE_LIST_PATH = "/rest/v2/manager/node/node_list";

    protected HttpClientPoolManager poolManager;

    @Autowired
    public DorisNodesClient(HttpClientPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    public DorisNodes getNodes(ClusterInfoEntity entity) throws Exception {
        String url = getHostUrl(entity.getAddress(), entity.getHttpPort()) + NODE_LIST_PATH;
        return getDorisNodes(entity, url);
    }

    private DorisNodes getDorisNodes(ClusterInfoEntity entity, String url) throws Exception {
        log.debug("Send get doris node list request, url is {}.", url);
        Map<String, String> headers = Maps.newHashMap();
        setHeaders(headers);
        setAuthHeaders(headers, entity.getUser(), entity.getPasswd());

        PaloResponseEntity response = poolManager.doGet(url, headers);
        if (response.getCode() != REQUEST_SUCCESS_CODE) {
            throw new PaloRequestException("Get doris node list error.");
        }

        return JSON.parseObject(response.getData(), DorisNodes.class);
    }
}
