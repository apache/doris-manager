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

import org.apache.doris.stack.control.ModelControlResponse;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.ModelControlRequestEntity;

// TODO:To be optimized
public interface RequestHandler<REQUEST> {

    /**
     * Processing control requests
     * @param user
     * @param request
     * @return
     * @throws Exception
     */
    ModelControlResponse handleRequest(CoreUserEntity user, REQUEST request) throws Exception;

    /**
     * get request data entity
     * @param request
     * @return
     */
    ModelControlRequestEntity getRequestData(REQUEST request, String userName);

    /**
     * Verify the validity of the request
     * @param request
     */
    void verifyRequest(REQUEST request) throws Exception;

    /**
     * Update request information
     * @param request
     * @param requestEntity
     */
    void updateRequest(REQUEST request, ModelControlRequestEntity requestEntity);

    /**
     * Complete request
     * @param requestEntity
     */
    void completedRequest(boolean isCompleted, ModelControlRequestEntity requestEntity);

    /**
     * Handle the detailed event content of the request
     * @param user
     * @param request
     * @return
     * @throws Exception
     */
    ModelControlResponse handleRequestEvent(CoreUserEntity user, REQUEST request, boolean modelInit) throws Exception;

    /**
     * Get the processing result of the request
     * @param request
     * @return
     */
    ModelControlResponse getResponse(REQUEST request, boolean isCompleted);

    /**
     * If it is a request to create a model, you need to create a model entity
     * @param request
     * @return
     */
    long initRequestModel(REQUEST request, String creator) throws Exception;
}
