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

package org.apache.doris.stack.control.request;

import com.alibaba.fastjson.JSON;
import org.apache.doris.stack.component.ModelControlRequestComponent;
import org.apache.doris.stack.control.ModelControlLevel;
import org.apache.doris.stack.control.ModelControlResponse;
import org.apache.doris.stack.control.ModelControlStatus;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.ModelControlRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DorisClusterRequestHandler implements RequestHandler<DorisClusterRequest> {

    @Autowired
    protected ModelControlRequestComponent requestComponent;

    @Override
    public ModelControlResponse handleRequest(CoreUserEntity user, DorisClusterRequest request) throws Exception {
        verifyRequest(request);
        boolean modelInit = false;
        if (request.getClusterId() < 1L) {
            modelInit = true;
            long clusterId = initRequestModel(request, user.getFirstName());
            request.setClusterId(clusterId);
        }

        ModelControlRequestEntity requestEntity = getRequestData(request, user.getFirstName());
        updateRequest(request, requestEntity);

        ModelControlResponse response = handleRequestEvent(user, request, modelInit);
        completedRequest(response.isCompleted(), requestEntity);
        return response;
    }

    @Override
    public ModelControlRequestEntity getRequestData(DorisClusterRequest request, String userName) {
        long requestId = request.getRequestId();
        if (requestId < 1L) {
            ModelControlRequestEntity requestEntity =
                    requestComponent.requestCreate(ModelControlLevel.DORIS_CLUSTER, request.getClusterId(),
                            request.getType(), userName);
            request.setRequestId(requestEntity.getId());
            return requestEntity;
        } else {
            return requestComponent.requestGet(requestId);
        }
    }

    @Override
    public void verifyRequest(DorisClusterRequest request) throws Exception {
        requestComponent.requestIdVerification(ModelControlLevel.DORIS_CLUSTER, request.getClusterId(),
                request.getRequestId());
    }

    @Override
    public void updateRequest(DorisClusterRequest request, ModelControlRequestEntity requestEntity) {
        requestComponent.updateRequestCurrentEventAndModel(requestEntity, request.getEventType(),
                request.getClusterId(), JSON.toJSONString(request));
    }

    @Override
    public void completedRequest(boolean isCompleted, ModelControlRequestEntity requestEntity) {
        if (isCompleted) {
            requestComponent.requestCompleted(requestEntity, ModelControlStatus.SUCCESS);
        }
    }

    @Override
    public ModelControlResponse getResponse(DorisClusterRequest dorisClusterRequest, boolean isCompleted) {
        ModelControlResponse response = new ModelControlResponse();
        response.setClusterId(dorisClusterRequest.getClusterId());
        response.setRequestId(dorisClusterRequest.getRequestId());
        response.setCurrentEventType(dorisClusterRequest.getEventType() + 1);
        response.setLevel(ModelControlLevel.DORIS_CLUSTER);
        response.setCompleted(isCompleted);
        response.setRequestType(dorisClusterRequest.getType());
        return response;
    }

    @Override
    public long initRequestModel(DorisClusterRequest request, String creator) throws Exception {
        return request.getClusterId();
    }
}
