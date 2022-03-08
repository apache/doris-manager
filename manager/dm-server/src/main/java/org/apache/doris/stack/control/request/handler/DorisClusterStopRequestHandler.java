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
import org.apache.doris.stack.control.request.DorisClusterRequest;
import org.apache.doris.stack.control.request.DorisClusterRequestHandler;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DorisClusterStopRequestHandler extends DorisClusterRequestHandler {
    @Autowired
    private DorisClusterManager dorisClusterManager;

    @Override
    public ModelControlResponse handleRequestEvent(CoreUserEntity user, DorisClusterRequest request, boolean modelInit) throws Exception {
        dorisClusterManager.stopClusterOperation(request.getClusterId(), request.getRequestId());
        return getResponse(request, false);
    }
}
