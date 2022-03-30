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

package org.apache.doris.stack.connector;

import static org.apache.doris.stack.constant.ConstantDef.PALO_ANALYZER_USER_PASSWORD;

import org.apache.doris.stack.driver.JdbcSampleClient;
import org.apache.doris.stack.model.response.construct.NativeQueryResp;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PaloQueryClient extends PaloClient {
    protected HttpClientPoolManager poolManager;

    @Autowired
    public PaloQueryClient(HttpClientPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Autowired
    private JdbcSampleClient jdbcClient;
    /**
     * Create an ordinary analyst Palo large account user
     * @param ns
     * @param db
     * @param entity
     * @return
     */
    public String createUser(String ns, String db, ClusterInfoEntity entity, String userName) throws Exception {
        String passwd = PALO_ANALYZER_USER_PASSWORD;

        try {
            String createSql = "create user '" + userName + "'@'%' IDENTIFIED BY '" + passwd + "'";
            executeSQL(createSql, ns, db, entity);

            //            String permissionSql = "GRANT SELECT_PRIV ON *.* TO '" + userName + "'@'%'";
            //            executeSQL(permissionSql, ns, db, entity);

        } catch (Exception e) {
            log.warn("create user exception {}.", e.getMessage());
            throw e;
        }

        return passwd;
    }

    public void deleteUser(String ns, String db, ClusterInfoEntity entity, String userName) {
        if (userName == null) {
            log.warn("user name is null");
            return;
        }
        if (userName.startsWith("Analyzer") || userName.startsWith("Administrators")) {
            try {
                String deleteSql = "DROP USER '" + userName + "'@'%'";
                executeSQL(deleteSql, ns, db, entity);
            } catch (Exception e) {
                log.warn("delete user exception {}.", e);
            }
        }
    }

    public String createAdminRoleUser(String ns, String db, ClusterInfoEntity entity, String userName) throws Exception {

        log.debug("create admin role user for cluster {}", entity.getId());
        try {
            String createSql =
                    "CREATE USER '" + userName + "'@'%' IDENTIFIED BY '" + PALO_ANALYZER_USER_PASSWORD + "' DEFAULT ROLE 'admin'";

            executeSQL(createSql, ns, db, entity);
        } catch (Exception e) {
            log.warn("create user exception {}.", e.getMessage());
            throw e;
        }
        return PALO_ANALYZER_USER_PASSWORD;

    }

    public double countSQL(String sql, String ns, String db, ClusterInfoEntity entity) {
        try {
            NativeQueryResp resp = executeSQL(sql, ns, db, entity);
            List<List<String>> data = resp.getData();
            if (data.get(0).get(0) == null) {
                return 0;
            } else {
                return Double.parseDouble(data.get(0).get(0));
            }
        } catch (Exception e) {
            log.error("execute count sql {} error {}", sql, e);
            return 0.0;
        }
    }

    public NativeQueryResp executeSQL(String sql, String ns, String db, ClusterInfoEntity entity) throws Exception {

        Statement stmt = jdbcClient.getStatement(entity.getAddress(), entity.getQueryPort(),
                entity.getUser(), entity.getPasswd(), db);
        try {
            NativeQueryResp res = executeSql(stmt, sql);
            return res;
        } catch (Exception e) {
            log.error("execute sql {} exception: {}", sql, e);
            throw  e;
        } finally {
            jdbcClient.closeStatement(stmt);
        }
    }

    public NativeQueryResp executeSql(Statement stmt, String sql) throws Exception {
        log.info("execute sql: {}", sql);

        long startTimeMs = System.currentTimeMillis();
        boolean isResultSet = true;
        try {
            isResultSet = stmt.execute(sql);
        } catch (Exception e) {
            log.error("execute sql {} exception: {}", sql, e);
            throw e;
        }

        long endTimeMs = System.currentTimeMillis();

        NativeQueryResp resp = new NativeQueryResp();

        log.info("is result set {}", isResultSet);

        if (isResultSet) {
            resp.setType(NativeQueryResp.Type.result_set.name());
        } else {
            resp.setType(NativeQueryResp.Type.exec_status.name());
        }

        // time type should be long?
        resp.setTime((int) (endTimeMs - startTimeMs));

        if (resp.getType().equals(NativeQueryResp.Type.result_set.name())) {
            ResultSet res = stmt.getResultSet();

            // get meta info
            ResultSetMetaData meta = res.getMetaData();

            List<NativeQueryResp.Meta> metaList = new ArrayList<>();

            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String colName = meta.getColumnName(i);
                String colType = meta.getColumnTypeName(i);
                log.debug("{} column {} type is {}", i, colName, colType);
                metaList.add(new NativeQueryResp.Meta(colName, colType));
            }

            resp.setMeta(metaList);

            // get result info
            List<List<String>> tableData = new ArrayList<>();
            while (res.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.add(res.getString(i));
                }
                tableData.add(row);
                log.debug("add row {}", row);
            }

            resp.setData(tableData);
        }
        return resp;
    }
}
