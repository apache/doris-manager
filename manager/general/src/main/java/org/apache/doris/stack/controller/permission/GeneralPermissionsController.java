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

package org.apache.doris.stack.controller.permission;

import org.apache.doris.stack.controller.BaseController;
import org.apache.doris.stack.model.request.permission.BatchPermissionMembershipReq;
import org.apache.doris.stack.model.request.permission.PermissionGroupAddReq;
import org.apache.doris.stack.model.request.permission.PermissionMembershipReq;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.apache.doris.stack.service.permission.GeneralPermissionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "Permission manage API")
@RestController
@RequestMapping(value = "/api/permissions/")
@Slf4j
public class GeneralPermissionsController extends BaseController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private GeneralPermissionsService permissionsService;

    @ApiOperation(value = "admin get all roles")
    @GetMapping(value = "/group", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getPermissionsGroup(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        log.debug("get all permission group.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(permissionsService.getAllPermissionGroup(user));
    }

    @ApiOperation(value = "admin get role detail by id")
    @GetMapping(value = "/group/{" + GROUP_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getPermissionsGroupById(@PathVariable(value = GROUP_KEY) int groupId,
                                          HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {
        log.debug("get permission group by id.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(permissionsService.getPermissionGroupById(user, groupId));
    }

    @ApiOperation(value = "add a new role")
    @PostMapping(value = "/group", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object addPermissionsGroup(@RequestBody PermissionGroupAddReq addReq,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        log.debug("add permission group.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(permissionsService.addPermissionGroup(user, addReq));
    }

    @ApiOperation(value = "admin update role info by id")
    @PutMapping(value = "/group/{" + GROUP_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object updatePermissionsGroupById(@PathVariable(value = GROUP_KEY) int groupId,
                                             @RequestBody PermissionGroupAddReq updateReq,
                                             HttpServletRequest request,
                                             HttpServletResponse response) throws Exception {
        log.debug("update permission group by id.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(permissionsService.updatePermissionGroup(user, groupId, updateReq));
    }

    @ApiOperation(value = "admin delete role by id")
    @DeleteMapping(value = "/group/{" + GROUP_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object deletePermissionsGroupById(@PathVariable(value = GROUP_KEY) int groupId,
                                             HttpServletRequest request,
                                             HttpServletResponse response) throws Exception {
        log.debug("delete permission group by id.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        permissionsService.deletePermissionsGroupById(user, groupId);
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "admin get all user role relationships")
    @GetMapping(value = "/membership", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getAllMemberships(HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        log.debug("Get all permission memberships.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(permissionsService.getAllMemberships(user));
    }

    @ApiOperation(value = "admin add a new user to role")
    @PostMapping(value = "/membership", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object addMemberships(@RequestBody PermissionMembershipReq membershipReq,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        log.debug("add permission membership.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(permissionsService.addMembership(user, membershipReq));
    }

    @ApiOperation(value = "admin add multiple users to role")
    @PostMapping(value = "/memberships", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object batchAddMemberships(@RequestBody BatchPermissionMembershipReq req,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        log.debug("add permission memberships.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(permissionsService.batchAddMembership(user, req));
    }

    @ApiOperation(value = "admin delete a user from role")
    @DeleteMapping(value = "/membership/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object deleteMemberships(@PathVariable(value = "id") int id,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        log.debug("delete permission membership.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);

        permissionsService.deleteMembership(user, id);
        return ResponseEntityBuilder.ok();
    }
}
