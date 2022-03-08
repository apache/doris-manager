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

package org.apache.doris.stack.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.PropertyService;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "Get global properties API")
@RestController
@RequestMapping()
@Slf4j
public class PropertyController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private PropertyService propertyService;

    @ApiOperation(value = "Get all configuration information")
    @GetMapping(value = "/api/session/properties", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getProperties(HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        log.debug("Get all properties.");
        CoreUserEntity user = authService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(propertyService.properties(user));
    }

    @ApiOperation(value = "Get initialization configuration information")
    @GetMapping(value = "/api/session/initProperties", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getInitProperties(HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        log.debug("Get all properties.");
        return ResponseEntityBuilder.ok(propertyService.properties());
    }
}
