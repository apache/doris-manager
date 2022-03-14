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
import org.apache.doris.stack.service.control.DorisClusterModuleService;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "Doris cluster Module management and control API")
@RestController
@RequestMapping("/api/control/module/")
@Slf4j
public class DorisClusterModuleController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DorisClusterModuleService moduleService;

    @ApiOperation(value = "Super user get cluster resource nodes list")
    @GetMapping(value = "{moduleId}/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getModelInstanceList(@PathVariable(value = "moduleId") long moduleId,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {
        log.debug("Super user create palo user space.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);

        return ResponseEntityBuilder.ok(moduleService.getClusterMoudleInstanceList(user, moduleId));
    }
}
