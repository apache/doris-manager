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

package org.apache.doris.stack.controller.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.control.DorisClusterInstanceService;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "Doris cluster Instance management and control API")
@RestController
@RequestMapping("/api/control/instance/")
@Slf4j
public class DorisClusterInstanceController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DorisClusterInstanceService instanceService;

    @ApiOperation(value = "The user operates(INSTANCE_INSTALL/INSTANCE_TAKE_OVER) the agent on the node")
    @PostMapping(value = "{instanceId}/agent/operate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object operateAgent(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable(value = "instanceId") long instanceId,
                               @RequestParam(value = "operate_type", required = false,
                                       defaultValue = "INSTANCE_INSTALL") String operateType) throws Exception {
        log.debug("Super user create palo user space.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        instanceService.operateInstance(user, instanceId, operateType);
        return ResponseEntityBuilder.ok();
    }

}
