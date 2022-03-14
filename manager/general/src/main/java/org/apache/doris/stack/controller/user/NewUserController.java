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

package org.apache.doris.stack.controller.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.stack.controller.BaseController;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.model.request.user.AdminUpdateReq;
import org.apache.doris.stack.model.request.user.NewUserAddReq;
import org.apache.doris.stack.model.request.user.PasswordUpdateReq;
import org.apache.doris.stack.model.request.user.UserUpdateReq;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.apache.doris.stack.service.user.NewUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "V2 Version User Management API")
@RestController
@RequestMapping(value = "/api/v2/user/")
@Slf4j
public class NewUserController extends BaseController {
    @Autowired
    private NewUserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * The space administrator obtains the list of all users in the space.
     * If includedeactivated is true, it means to obtain all users (active and inactive),
     * If includedeactivated is false, the active user is obtained
     *
     * @param includeDeactivated
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "The user obtains the list of all users (platform level or space level API, platform level "
            + "obtains the list of all users, and views all users in the space)"
            + "(include_deactivated indicates whether to include deactivated users, the default is false, "
            + "which means not included. q indicates the search field, the default is empty, "
            + "which means to obtain all users)")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getUser(@RequestParam(value = "include_deactivated", defaultValue = "false") boolean includeDeactivated,
                          @RequestParam(value = "q", required = false, defaultValue = "") String q,
                          HttpServletRequest request,
                          HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        log.debug("Admin user {} get all user list.", user.getId());
        return ResponseEntityBuilder.ok(userService.getAllUser(user, includeDeactivated, q));
    }

    @ApiOperation(value = "Get user details according to user ID (all users, platform level or space level API). "
            + "Users can view their own information, or admin users can view other users, or space administrator role "
            + "users can view other users")
    @GetMapping(value = "{" + USER_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getUserById(@PathVariable(value = USER_KEY) int userId,
                              HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(userService.getUserById(userId, user));
    }

    @ApiOperation(value = "Get the current user information after logging in (all users, "
            + "platform level or space level API)")
    @GetMapping(value = "current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getCurrentUser(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(userService.getCurrentUser(user));
    }

    @ApiOperation(value = "User modifies the space ID being used (all users, platform level API)")
    @PostMapping(value = "current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object updateUserCurrentCluster(@RequestParam(value = "cluster_id") long clusterId,
                                           HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(userService.updateUserCurrentCluster(user, clusterId));
    }

    @ApiOperation(value = "Add a new user (admin user, platform level API)")
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object addUser(@RequestBody NewUserAddReq userAddReq,
                          HttpServletRequest request,
                          HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(userService.addUser(userAddReq, user));
    }

    @ApiOperation(value = "Change the user name or mailbox information (all users, all users can modify their "
            + "own information, admin user can modify the information of other users, platform level API)")
    @PutMapping(value = "{" + USER_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object updateUser(@RequestBody UserUpdateReq userUpdateReq,
                             @PathVariable(value = USER_KEY) int userId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(userService.updateUser(userUpdateReq, user, userId));
    }

    @ApiOperation(value = "Reactivate a deactivated user (admin user, platform level API)")
    @PutMapping(value = "{" + USER_KEY + "}" + "/reactivate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object reactivateUser(@PathVariable(value = USER_KEY) int userId,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(userService.reactivateUser(userId, user));
    }

    @ApiOperation(value = "Deactivate a user (admin user, platform level API)")
    @DeleteMapping(value = "{" + USER_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object stopUser(@PathVariable(value = USER_KEY) int userId,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(userService.stopUser(userId, user));
    }

    @ApiOperation(value = "Modify user password (all users can modify their own passwords, and admin users can "
            + "also directly reset the passwords of other users, platform level or space level APIs)")
    @PutMapping(value = "{" + USER_KEY + "}" + "/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object updatePassword(@PathVariable(value = USER_KEY) int userId,
                                 @RequestBody PasswordUpdateReq updateReq,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(userService.updatePassword(updateReq, userId, user));
    }

    @ApiOperation(value = "Modify the user's admin attribute (admin user, but you can't modify your "
            + "own admin attribute, platform level API)")
    @PutMapping(value = "{" + USER_KEY + "}" + "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object updateUserAdmin(@PathVariable(value = USER_KEY) int userId,
                                 @RequestBody AdminUpdateReq updateReq,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(userService.updateUserAdmin(updateReq, userId, user));
    }

    @ApiOperation(value = "Resend the invitation email (admin user, but can't send email to himself, "
            + "platform level API)")
    @PostMapping(value = "{" + USER_KEY + "}" +  "/send_invite", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object sendInvite(@PathVariable(value = USER_KEY) int userId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(userService.sendInvite(user, userId));
    }

    @ApiOperation(value = "Delete a user (admin user, but cannot delete itself, platform level API)")
    @DeleteMapping(value = "delete/{" + USER_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object deleteUser(@PathVariable(value = USER_KEY) int userId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        authenticationService.checkUserIsAdmin(user);
        userService.deleteUser(user, userId);
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Get all users in the space (all users with the role of space administrator, space level API) "
            + "(q represents the search field, which is empty by default, which means get all)")
    @GetMapping(value = "space", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getUserSpace(@RequestParam(value = "q", required = false, defaultValue = "") String q,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        log.debug("User {} get space list.", user.getId());
        return ResponseEntityBuilder.ok(userService.getSpaceUserList(user, q));
    }

    @ApiOperation(value = "Add a new user into the space (all users with the role of "
            + "space administrator, space level API)")
    @PostMapping(value = "/add/{" + USER_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object addUserToSpace(@PathVariable(value = USER_KEY) int userId,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        userService.addUserToSpace(user, userId);
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Delete a user in the space (all users with the role of "
            + "space administrator, space level API)")
    @DeleteMapping(value = "/move/{" + USER_KEY + "}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object moveUser(@PathVariable(value = USER_KEY) int userId,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        userService.moveUser(userId, user);
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "update user qbnewb")
    @PutMapping(value = "{" + USER_KEY + "}" + "/qbnewb", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object setQpnewb(@PathVariable(value = USER_KEY) int userId,
                            HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(userService.setQbnewb(user, userId));
    }
}
