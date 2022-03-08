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

package org.apache.doris.stack.driver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Component
@Slf4j
public class JdbcSampleClient {

    @Autowired
    public JdbcSampleClient() {
    }

    public boolean testConnetion(String host, int port, String dbName, String user,
                              String passwd) throws SQLException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("jdbc:mysql://");
        buffer.append(host);
        buffer.append(":");
        buffer.append(port);
        buffer.append("/");
        buffer.append(dbName);
        String url = buffer.toString();
        try {
            Connection myCon = DriverManager.getConnection(url, user, passwd);
            myCon.close();
            return true;
        } catch (SQLException e) {
            log.error("Get JDBC connection exception.");
            throw e;
        }
    }

    public Statement getStatement(String host, int port, String user, String passwd) throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("jdbc:mysql://");
        buffer.append(host);
        buffer.append(":");
        buffer.append(port);
        String url = buffer.toString();
        try {
            Connection myCon = DriverManager.getConnection(url, user, passwd);
            Statement stmt = myCon.createStatement();
            return stmt;
        } catch (Exception e) {
            log.error("Get doris jdbc connection error {}.", e);
            throw e;
        }
    }

    public void updateUserPassword(String user, String newPassword, Statement stmt) throws Exception {
        try {
            String sql = "SET PASSWORD FOR '" + user
                    + "' = PASSWORD('" + newPassword + "')";

            int result = stmt.executeUpdate(sql);
            if (result == -1) {
                throw new Exception("failed to execute sql: " + sql + ", result is -1");
            }
        } catch (Exception e) {
            log.error("Update password error {}.", e);
            throw e;
        }
    }

    public void addFeObserver(List<String> feObserverHostPorts, Statement stmt) throws Exception {
        try {
            for (String feObserverHostPort : feObserverHostPorts) {
                String sql = "ALTER SYSTEM ADD OBSERVER \"" + feObserverHostPort + "\"";

                int result = stmt.executeUpdate(sql);
                if (result == -1) {
                    throw new Exception("failed to execute sql: " + sql + ", result is -1");
                }
            }
        } catch (Exception e) {
            log.error("Add be error {}.", e);
            throw e;
        }
    }

    public void addBe(List<String> beHostPorts, Statement stmt) throws Exception {
        try {
            for (String beHostPort : beHostPorts) {
                String sql = "ALTER SYSTEM ADD BACKEND \"" + beHostPort + "\"";

                int result = stmt.executeUpdate(sql);
                if (result == -1) {
                    throw new Exception("failed to execute sql: " + sql + ", result is -1");
                }
            }
        } catch (Exception e) {
            log.error("Add be error {}.", e);
            throw e;
        }
    }

    public void addBrokerName(List<String> brokerHostPorts, Statement stmt) throws Exception {
        try {
            StringBuffer brokers = new StringBuffer();
            for (String brokerHostPort : brokerHostPorts) {
                brokers.append("\"");
                brokers.append(brokerHostPort);
                brokers.append("\",");
            }
            // Remove the comma at the end
            brokers.deleteCharAt(brokers.length() - 1);
            String sql = "ALTER SYSTEM ADD BROKER broker_name " + brokers.toString();

            int result = stmt.executeUpdate(sql);
            if (result == -1) {
                throw new Exception("failed to execute sql: " + sql + ", result is -1");
            }
        } catch (Exception e) {
            log.error("Add be error {}.", e);
            throw e;
        }
    }

    public void closeStatement(Statement stmt) {
        try {
            stmt.close();
        } catch (SQLException e) {
            log.error("close doris statement error.", e);
        }
    }
}
