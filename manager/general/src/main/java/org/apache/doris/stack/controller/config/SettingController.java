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

package org.apache.doris.stack.controller.config;

import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.model.request.config.ConfigUpdateReq;
import org.apache.doris.stack.model.request.config.InitStudioReq;
import org.apache.doris.stack.model.request.config.LdapAuthTypeReq;
import org.apache.doris.stack.model.request.user.NewUserAddReq;
import org.apache.doris.stack.model.response.config.SettingItem;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.config.SettingService;
import org.apache.doris.stack.service.user.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

@Api(tags = "configuration information API")
@RestController
@RequestMapping(value = "/api/setting/")
@Slf4j
public class SettingController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SettingService settingService;

    @ApiOperation(value = "Initialize stack service authentication type(super administrator access)")
    @PostMapping(value = "init/studio", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object initStudio(@RequestBody InitStudioReq initStudioReq, HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        log.debug("init studio.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        authenticationService.checkUserIsAdmin(user);
        settingService.initStudio(initStudioReq);
        log.debug("init studio successful.");
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Initialize ldap setting")
    @PostMapping(value = "init/ldapStudio", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object initLdapStudio(@RequestBody InitStudioReq initStudioReq, HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        log.debug("init ldap studio.");
        settingService.initLdapStudio(initStudioReq);
        log.debug("init ldap studio successful.");
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "sync ldap manually")
    @GetMapping(value = "syncLdapUser", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object syncLdapUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("sync ldap manually");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // 检查是否为Admin用户账号
        authenticationService.checkUserIsAdmin(user);
        settingService.syncLdapUser();
        log.debug("init ldap studio successful.");
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Initialize stack service authentication type")
    @PostMapping(value = "init/authType", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object initStudioAuthType(@RequestBody LdapAuthTypeReq authType, HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        log.debug("init studio auth type.");
        settingService.initStudioAuthType(authType);
        log.debug("init studio auth type successful.");
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Get all configuration information of the current space")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getAllConfig(HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        log.debug("get config info.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(settingService.getAllConfig(user));
    }

    @ApiOperation(value = "system init last step, config the admin user")
    @PostMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object addAdminUser(@RequestBody NewUserAddReq userAddReq,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        String sessionId = settingService.addAdminUser(userAddReq);
        authenticationService.setResponseCookie(response, sessionId);
        return ResponseEntityBuilder.ok(sessionId);
    }

    @ApiOperation(value = "Get all global configuration information of stack(super administrator access)")
    @GetMapping(value = "global", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getAllGlobalConfig(HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        log.debug("get config info.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(settingService.getAllPublicConfig());
    }

    @ApiOperation(value = "Get the information of a global configuration item by name(super administrator access)")
    @GetMapping(value = "global/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getGlobalConfigByKey(@PathVariable(value = "key") String key,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        log.debug("get config info.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(settingService.getConfigByKey(key));
    }

    @ApiOperation(value = "Get the information of a configuration item by name(super administrator/space administrator access)")
    @GetMapping(value = "{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getConfigByKey(@PathVariable(value = "key") String key,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        log.debug("get config info.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        if (user.isSuperuser()) {
            // Platform level configuration
            return ResponseEntityBuilder.ok(settingService.getConfigByKey(key));
        } else {
            // In space configuration
            return ResponseEntityBuilder.ok(settingService.getConfigByKey(key, user));
        }
    }

    @ApiOperation(value = "Modify the information of a configuration item according to its name(super administrator/space administrator access)")
    @PutMapping(value = "{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object updateConfigByKey(@PathVariable(value = "key") String key,
                                    @RequestBody ConfigUpdateReq updateReq,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        log.debug("get config info.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        SettingItem resutl;
        if (user.isSuperuser()) {
            resutl = settingService.superUpdateConfigByKey(key, updateReq);
        } else {
            resutl = settingService.amdinUpdateConfigByKey(key, user, updateReq);
        }
        return ResponseEntityBuilder.ok(resutl);
    }

    @ApiOperation(value = "system init reset")
    @GetMapping(value = "reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object reset(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("system init reset.");
        int userId = authenticationService.checkUserAuthWithCookie(request, response);
        settingService.reset(userId);
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "LDAP authentication obtains a list of all users and selects one as the administrator")
    @GetMapping(value = "/ldapUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getUser(HttpServletRequest request, @RequestParam(value = "q", required = false, defaultValue = "") String q,
                          HttpServletResponse response) throws Exception {
        return ResponseEntityBuilder.ok(settingService.getAllUsers(q));
    }
}
