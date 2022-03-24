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

package org.apache.doris.stack.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.doris.stack.control.ModelControlLevel;
import org.apache.doris.stack.control.ModelControlRequestType;
import org.apache.doris.stack.control.ModelControlStatus;
import org.apache.doris.stack.dao.ModelControlRequestRepository;
import org.apache.doris.stack.entity.ModelControlRequestEntity;
import org.apache.doris.stack.exceptions.ModelRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
@Slf4j
public class ModelControlRequestComponent {
    @Autowired
    private ModelControlRequestRepository requestRepository;

    public ModelControlRequestEntity requestCreate(ModelControlLevel level, long modelId,
                                                   ModelControlRequestType requestType, String userName) {
        ModelControlRequestEntity newEntity = new ModelControlRequestEntity(level, modelId, requestType, userName);
        return requestRepository.save(newEntity);
    }

    public ModelControlRequestEntity requestGet(long requestId) {
        return requestRepository.findById(requestId).get();
    }

    public ModelControlRequestEntity updateRequestCurrentEventAndModel(ModelControlRequestEntity requestEntity,
                                                                       int eventType, long modelId, String requestInfo) {
        requestEntity.setCurrentEventType(eventType);
        requestEntity.setModelId(modelId);
        requestEntity.setRequestInfo(requestInfo);
        return requestRepository.save(requestEntity);
    }

    public void requestIdVerification(ModelControlLevel level, long modelId, long requestId) throws Exception {
        if (modelId < 1) {
            return;
        }
        List<Long> modelNotCompletedRequest =
                requestRepository.getIdByModelLevelAndIdAndCompleted(level, modelId, false);
        if (modelNotCompletedRequest.isEmpty()
                || (modelNotCompletedRequest.size() == 1 && modelNotCompletedRequest.contains(requestId))) {
            log.info("request verification success");
            return;
        } else {
            log.error("request verification fail");
            throw new ModelRequestException();
        }
    }

    public void requestCompleted(ModelControlRequestEntity requestEntity, ModelControlStatus status) {
        requestEntity.setCompleted(true);
        requestEntity.setStatus(status);
        requestEntity.setCompletedTimestamp(new Timestamp(System.currentTimeMillis()));
        requestRepository.save(requestEntity);
    }
}

