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
import org.apache.doris.manager.common.heartbeat.HeartBeatContext;
import org.apache.doris.manager.common.heartbeat.HeartBeatResult;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.control.ResourceClusterNodeService;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "Resource Cluster Node Agent API")
@RestController
@RequestMapping("/api/control/node/")
@Slf4j
public class ResourceClusterNodeController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ResourceClusterNodeService nodeService;

    @ApiOperation(value = "get heart beat context(node agent heartbeat and instance info)")
    @GetMapping(value = "{agentNodeId}/agent/context", produces = MediaType.APPLICATION_JSON_VALUE)
    public HeartBeatContext getHeartbeatContext(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @PathVariable(value = "agentNodeId") long agentNodeId) {
        return nodeService.getHeartBeatContext(agentNodeId);
    }

    @ApiOperation(value = "deal heart beat context)")
    @PostMapping(value = "{agentNodeId}/agent/context", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object postHeartbeatContext(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @PathVariable(value = "agentNodeId") long agentNodeId,
                                                @RequestBody HeartBeatResult ctx) {
        log.info("agent {} post heartbeat context", agentNodeId);
        nodeService.dealHeartbeatContext(ctx);
        return "SUCCESS";
    }

    @ApiOperation(value = "The user operates(AGENT_INSTALL) the agent on the node")
    @PostMapping(value = "{agentNodeId}/agent/operate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object operateAgent(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable(value = "agentNodeId") long agentNodeId,
                               @RequestParam(value = "operate_type", required = false,
                                       defaultValue = "AGENT_INSTALL") String operateType) throws Exception {
        log.debug("Super user create palo user space.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        authenticationService.checkUserIsAdmin(user);
        nodeService.operateAgent(agentNodeId, operateType);
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Cancel the user's ongoing operation on the agent")
    @PostMapping(value = "{agentNodeId}/agent/operate/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object operateAgentCancel(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable(value = "agentNodeId") long agentNodeId,
                               @RequestParam(value = "operate_type", required = false,
                                       defaultValue = "AGENT_INSTALL") String operateType) throws Exception {
        log.debug("Super user create palo user space.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        // check is super admin user
        authenticationService.checkUserIsAdmin(user);
        nodeService.operateAgent(agentNodeId, operateType);
        return ResponseEntityBuilder.ok();
    }

}
