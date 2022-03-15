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
import org.apache.doris.stack.model.request.control.DorisClusterCreationReq;
import org.apache.doris.stack.model.request.control.DorisClusterTakeOverReq;
import org.apache.doris.stack.model.request.control.ModelControlReq;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.control.DorisClusterService;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "Doris application cluster management and control API")
@RestController
@RequestMapping(value = "/api/control/cluster/")
@Slf4j
public class DorisClusterController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DorisClusterService clusterService;

    @ApiOperation(value = "Super user deploy and create a doris cluster")
    @PostMapping(value = "creation", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object creation(HttpServletRequest request, HttpServletResponse response,
                           @RequestBody DorisClusterCreationReq creationReq) throws Exception {
        log.debug("Super user deploy and create a doris cluster.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(clusterService.creation(user, creationReq));
    }

    @ApiOperation(value = "Super user deploy and take over a doris cluster")
    @PostMapping(value = "takeOver", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object takeOver(
            HttpServletRequest request, HttpServletResponse response,
            @RequestBody DorisClusterTakeOverReq takeOverReq) throws Exception {
        log.debug("Super user take over a doris cluster {}.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        authenticationService.checkUserIsAdmin(user);
        return ResponseEntityBuilder.ok(clusterService.takeOver(user, takeOverReq));
    }

    @ApiOperation(value = "Super user stop a doris cluster")
    @PostMapping(value = "stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object stop(HttpServletRequest request, HttpServletResponse response,
                       @RequestBody ModelControlReq req) throws Exception {
        log.debug("Super user stop a doris cluster..");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        clusterService.stopCluster(user, req.getClusterId());
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Super user start a doris cluster")
    @PostMapping(value = "start", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object start(HttpServletRequest request, HttpServletResponse response,
                        @RequestBody ModelControlReq req) throws Exception {
        log.debug("Super user stop a doris cluster..");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        clusterService.startCluster(user, req.getClusterId());
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Super user stop a doris cluster")
    @PostMapping(value = "restart", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object restart(HttpServletRequest request, HttpServletResponse response,
                          @RequestBody ModelControlReq req) throws Exception {
        log.debug("Super user restart a doris cluster..");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        clusterService.restartCluster(user, req.getClusterId());
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Super user get cluster module list")
    @GetMapping(value = "{clusterId}/modules", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getMoudleList(@PathVariable(value = "clusterId") long clusterId,
                                HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        log.debug("Super user create palo user space.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(clusterService.getClusterModules(user, clusterId));
    }

    // TODO:Later, it is implemented in dorisclustermodulecontroller
    @ApiOperation(value = "Super user get cluster all instances list")
    @GetMapping(value = "{clusterId}/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getModelInstanceList(@PathVariable(value = "clusterId") long clusterId,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {
        log.debug("Super user create palo user space.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(clusterService.getClusterInstances(user, clusterId));
    }

    @ApiOperation(value = "Super user get cluster resource nodes list")
    @GetMapping(value = "{clusterId}/nodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getNodeList(@PathVariable(value = "clusterId") long clusterId,
                              HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        log.debug("Super user create palo user space.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(clusterService.getClusterResourceNodes(user, clusterId));
    }

    @ApiOperation(value = "Super user get JDBC service status of Doris cluster")
    @GetMapping(value = "{clusterId}/jdbc/service/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getJdbcService(@PathVariable(value = "clusterId") long clusterId,
                              HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        log.debug("Super user get JDBC service status of Doris cluster.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(clusterService.checkJdbcServiceReady(user, clusterId));
    }

}
