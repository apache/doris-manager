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

package org.apache.doris.stack.controller.construct;

import org.apache.doris.stack.controller.BaseController;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.rest.ResponseEntityBuilder;
import org.apache.doris.stack.service.user.AuthenticationService;
import org.apache.doris.stack.service.construct.MetadataService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "Metadata API")
@RestController
@RequestMapping(value = "/api/meta/")
@Slf4j
public class MetadataController extends BaseController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MetadataService metadataService;

    @ApiOperation(value = "Synchronize the latest metadata information in the Doris cluster to the manager.")
    @PostMapping(value = "sync",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object syncPaloMetadata(
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("sync palo metadata.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        metadataService.syncMetadataByUserId(user);
        return ResponseEntityBuilder.ok();
    }

    @ApiOperation(value = "Get the list of all databases in the data warehouse")
    @GetMapping(value = "nsId/{" + NS_KEY + "}/" + DATABASES,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getAllDatabases(
            @PathVariable(value = NS_KEY) int nsId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("get database list by namespace.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(metadataService.getDatabaseListByNs(nsId, user));
    }

    @ApiOperation(value = "Get database details")
    @GetMapping(value = "dbId/{" + DB_KEY + "}/info",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getDatabaseInfo(@PathVariable(value = DB_KEY) int dbId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("get database info by id.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(metadataService.getDatabaseInfo(dbId, user));
    }

    @ApiOperation(value = "Get a list of all tables in the database")
    @GetMapping(value = "dbId/" + "/{" + DB_KEY + "}/" + TABLES,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTables(@PathVariable(value = DB_KEY) int dbId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("get table list by database.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(metadataService.getTableListByDb(dbId, user));
    }

    @ApiOperation(value = "Get details of table")
    @GetMapping(value = "tableId" + "/{" + TABLE_KEY + "}/info",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTableInfo(@PathVariable(value = TABLE_KEY) int tableId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("get table info by id.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(metadataService.getTableInfo(tableId, user));
    }

    @ApiOperation(value = "Gets a list of all fields in a table")
    @GetMapping(value = "tableId" + "/{" + TABLE_KEY + "}/" + FIELDS,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTableFields(@PathVariable(value = TABLE_KEY) int tableId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("get field list by table.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(metadataService.getFieldListByTable(tableId, user));
    }

    @ApiOperation(value = "Get schema information of table")
    @GetMapping(value = "tableId" + "/{" + TABLE_KEY + "}/schema",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTableSchema(@PathVariable(value = TABLE_KEY) int tableId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("get table schema by id.");
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return ResponseEntityBuilder.ok(metadataService.getTableSchema(tableId, user));
    }
}
