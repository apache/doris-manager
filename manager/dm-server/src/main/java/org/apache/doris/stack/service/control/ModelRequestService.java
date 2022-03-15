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
import org.apache.doris.stack.control.ModelControlRequestType;
import org.apache.doris.stack.control.ModelControlStatus;
import org.apache.doris.stack.dao.HeartBeatEventRepository;
import org.apache.doris.stack.dao.ModelControlRequestRepository;
import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.apache.doris.stack.entity.ModelControlRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ModelRequestService {

    @Autowired
    private ModelControlRequestRepository requestRepository;

    @Autowired
    private HeartBeatEventRepository eventRepository;

    /**
     * Finally, you need to wait for the request of the result returned by the agent,
     * regularly check whether it is completed
     * It does not include the requests to create a new cluster and take over the cluster,
     * because those two requests are controlled by the front-end page.
     * The last event can be reached only after one event is completed.
     * The last event has nothing to be completed by the agent, so it does not need to be checked
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void completeRequest() {
        log.debug("Modify the request status that can only be completed through the agent heartbeat asynchronous event");
        List<ModelControlRequestEntity> notCompletedRequests = new ArrayList<>();
        // get start model not completed request
        notCompletedRequests.addAll(requestRepository.getByRequestTypeAndCompleted(ModelControlRequestType.START, false));

        // get stop model not completed request
        notCompletedRequests.addAll(requestRepository.getByRequestTypeAndCompleted(ModelControlRequestType.STOP, false));

        // get restart model not completed request
        notCompletedRequests.addAll(requestRepository.getByRequestTypeAndCompleted(ModelControlRequestType.RESTART, false));

        for (ModelControlRequestEntity requestEntity : notCompletedRequests) {
            List<HeartBeatEventEntity> notCompletedEvents = eventRepository.getByRequestIdAndCompleted(requestEntity.getId(), false);
            // All events completed
            if (notCompletedEvents.isEmpty()) {
                Set<String> completedStatus = eventRepository.getStatusByRequestId(requestEntity.getId());
                // TODO:ModelControlStatus.CANCEL is not currently supported
                if (completedStatus.contains(ModelControlStatus.FAIL.name())) {
                    requestEntity.setStatus(ModelControlStatus.FAIL);
                } else {
                    requestEntity.setStatus(ModelControlStatus.SUCCESS);
                }
                requestEntity.setCompleted(true);
                requestRepository.save(requestEntity);
                log.info("The request {} has been completed", requestEntity.getId());
            } else {
                log.info("The request {} not completed", requestEntity.getId());
                continue;
            }
        }
    }

    public Object getRequestInfo(long requestId) throws Exception {
        if (requestId < 1) {
            log.error("request id error.");
            throw new Exception("request id error.");
        }
        ModelControlRequestEntity requestEntity = requestRepository.findById(requestId).get();
        if (requestEntity.getRequestInfo() == null) {
            return new Object();
        }
        return JSON.parse(requestEntity.getRequestInfo());
    }
}
