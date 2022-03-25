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

package org.apache.doris.stack.model.response.construct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.doris.stack.driver.DorisFieldTypeDef;
import org.apache.doris.stack.driver.SimpleColumn;
import org.apache.doris.stack.driver.SqlResultData;
import org.apache.doris.stack.model.request.construct.SqlQueryReq;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlQueryResp {
    // query result
    private SqlResultData data;

    // database_id
    private int databaseId;

    // started_at
    private Timestamp startedAt;

    // json_query
    private SqlQueryReq jsonQuery;

    private Status status;

    // row_count
    private int rowCount;

    // running_time,Run time, MS
    private long runningTime;

    // class，Error class name
    private String className;

    // Detailed error information
    private String stacktrace;

    // Cause of error
    private String error;

    public void failedInfoSet(Exception e) {
        this.setStatus(Status.failed);
        this.setClassName(e.getClass().getName());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        this.setStacktrace(sw.toString());

        if (e.getMessage() == null || e.getMessage().isEmpty()) {
            this.setError("Query unknown error, please check the error stack details");
        } else {
            this.setError(e.getMessage());
        }
    }

    public void resultDataSet(ResultSet resultSet) throws Exception {

        SqlResultData resultData = new SqlResultData();

        // Acquiring Metadata
        ResultSetMetaData meta = resultSet.getMetaData();
        int colCount = meta.getColumnCount();

        // Processing metadata information
        // TODO: The data of bigint type is specially processed. The string type is returned to the front end
        Set<Integer> bigintCloum = new HashSet<>();
        List<SimpleColumn> cols = new ArrayList<>();
        for (int i = 1; i <= colCount; i++) {
            SimpleColumn column = new SimpleColumn();
            String columnName = meta.getColumnName(i);
            column.setName(columnName);
            column.setDisplayName(columnName);

            String columnTypeName = meta.getColumnTypeName(i);

            String baseType = DorisFieldTypeDef.getDorisFieldType(columnTypeName);

            if (DorisFieldTypeDef.TYPE_BIGINTEGER.equals(baseType)) {
                bigintCloum.add(i);
            }
            column.setBaseType(baseType);
            cols.add(column);
        }
        resultData.setCols(cols);

        // Get all result data
        List<List<Object>> rows = new ArrayList<>();
        while (resultSet.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                if (bigintCloum.contains(i)) {
                    row.add(resultSet.getString(i));
                } else {
                    row.add(resultSet.getObject(i));
                }
            }
            rows.add(row);
        }
        resultData.setRows(rows);

        this.setData(resultData);
        this.setRowCount(rows.size());
    }

    /**
     * Status
     */
    public enum Status {
        completed,
        failed
    }
}
