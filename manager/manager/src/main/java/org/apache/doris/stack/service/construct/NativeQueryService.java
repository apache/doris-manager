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

package org.apache.doris.stack.service.construct;

import org.apache.doris.stack.constant.ConstantDef;
import org.apache.doris.stack.dao.ManagerDatabaseRepository;
import org.apache.doris.stack.driver.JdbcSampleClient;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.model.request.construct.SqlQueryReq;
import org.apache.doris.stack.model.response.construct.NativeQueryResp;
import org.apache.doris.stack.component.ClusterUserComponent;
import org.apache.doris.stack.component.DatabuildComponent;
import org.apache.doris.stack.connector.PaloQueryClient;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.ManagerDatabaseEntity;
import org.apache.doris.stack.model.response.construct.SqlQueryResp;
import org.apache.doris.stack.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

@Service
@Slf4j
public class NativeQueryService extends BaseService {

    @Autowired
    private PaloQueryClient paloQueryClient;

    @Autowired
    private ClusterUserComponent clusterUserComponent;

    @Autowired
    private DatabuildComponent databuildComponent;

    @Autowired
    private JdbcSampleClient jdbcSampleClient;

    @Autowired
    private ManagerDatabaseRepository databaseRepository;

    /**
     * Implement SQL query through Doris HTTP protocol
     * @param nsId
     * @param dbId
     * @param sql
     * @param user
     * @return
     * @throws Exception
     */
    public NativeQueryResp executeSql(int nsId, int dbId, String sql, CoreUserEntity user) throws Exception {
        log.debug("user {} execute sql {} in db {}", user.getId(), sql, dbId);
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        return executeSql(nsId, dbId, sql, user, clusterInfo);
    }

    public NativeQueryResp executeSql(int nsId, int dbId, String sql, CoreUserEntity user,
                                      ClusterInfoEntity clusterInfo) throws Exception {
        String dbName = null;
        if (dbId < 1) {
            dbName = ConstantDef.MYSQL_DEFAULT_SCHEMA;
        } else {
            ManagerDatabaseEntity databaseEntity = databuildComponent.checkClusterDatabase(dbId, clusterInfo.getId());
            dbName = databaseEntity.getName();
        }
        return paloQueryClient.executeSQL(sql, ConstantDef.DORIS_DEFAULT_NS, dbName, clusterInfo);
    }

    /**
     * Execute SQL statement
     * @param sql
     * @param dbName
     * @param user
     * @return
     * @throws Exception
     */
    public NativeQueryResp executeSql(String sql, String dbName, CoreUserEntity user) throws Exception {
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        return executeSql(sql, dbName, user, clusterInfo);
    }

    public NativeQueryResp executeSql(String sql, String dbName, CoreUserEntity user,
                                      ClusterInfoEntity clusterInfo) throws Exception {
        return paloQueryClient.executeSQL(sql, ConstantDef.DORIS_DEFAULT_NS, dbName, clusterInfo);
    }

    public SqlQueryResp querySql(CoreUserEntity user, SqlQueryReq queryReq) throws Exception {
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        ManagerDatabaseEntity databaseEntity = databaseRepository.findById(queryReq.getDatabase()).get();

        SqlQueryResp resp = new SqlQueryResp();
        long startTime = System.currentTimeMillis();
        resp.setStartedAt(new Timestamp(startTime));
        resp.setDatabaseId(queryReq.getDatabase());
        resp.setJsonQuery(queryReq);

        try {
            Statement statement = jdbcSampleClient.getStatement(clusterInfo.getAddress(), clusterInfo.getQueryPort(),
                    clusterInfo.getUser(), clusterInfo.getPasswd(), databaseEntity.getName());
            ResultSet result = jdbcSampleClient.executeSql(statement, queryReq.getQuery());
            resp.resultDataSet(result);
        } catch (Exception e) {
            log.error("Query sql error.");
            resp.failedInfoSet(e);
        }
        resp.setRunningTime(System.currentTimeMillis() - startTime);
        return resp;
    }
}
