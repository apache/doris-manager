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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DorisFieldTypeDef {

    public static final String TYPE_INTEGER = "type/Integer";

    public static final String TYPE_BIGINTEGER = "type/BigInteger";

    public static final String TYPE_FLOAT = "type/Float";

    public static final String TYPE_DECIMAL = "type/Decimal";

    public static final String TYPE_TEXT = "type/Text";

    public static final String TYPE_DATETIME = "type/DateTime";

    public static final String TYPE_DATE = "type/Date";

    public static final String TYPE_BOOLEAN = "type/Boolean";

    public static final String TYPE_ALL = "type/*";

    public static Map<String, String> dorisFieldTypeMap = new HashMap<>();

    // Doris engine field type definition and mapping
    public static Set<String> dorisFieldType = new HashSet<>();

    static {
        // Doris engine field type definition and mapping with structured field types
        dorisFieldTypeMap.put("TINYINT", TYPE_ALL);
        dorisFieldTypeMap.put("DATE", TYPE_DATE);
        dorisFieldTypeMap.put("INT", TYPE_INTEGER);
        dorisFieldTypeMap.put("SMALLINT", TYPE_INTEGER);
        dorisFieldTypeMap.put("VARCHAR", TYPE_TEXT);
        dorisFieldTypeMap.put("DATETIME", TYPE_DATETIME);
        dorisFieldTypeMap.put("BOOLEAN", TYPE_BOOLEAN);
        dorisFieldTypeMap.put("BIT", TYPE_BOOLEAN);
        dorisFieldTypeMap.put("DOUBLE", TYPE_FLOAT);
        dorisFieldTypeMap.put("FLOAT", TYPE_FLOAT);
        dorisFieldTypeMap.put("DECIMAL", TYPE_DECIMAL);
        dorisFieldTypeMap.put("BIGINT", TYPE_BIGINTEGER);
        dorisFieldTypeMap.put("LARGEINT", TYPE_TEXT); // TODO：At present, largeint is set as text type to solve
                                                      // the problem of precision
        dorisFieldTypeMap.put("BITMAP", TYPE_ALL);
        dorisFieldTypeMap.put("CHAR", TYPE_TEXT);
        dorisFieldTypeMap.put("HLL", TYPE_ALL); // TODO：Currently set HLL as text type
        // Field type of Doris engine
        dorisFieldType.addAll(dorisFieldTypeMap.keySet());
    }

    /**
     * Get Doris mapping data type
     *
     * @param dorisType dorisType
     * @return dorisType
     */
    public static String getDorisFieldType(String dorisType) {
        String type = dorisType.replaceAll("[^a-zA-Z]", "");
        String result = dorisFieldTypeMap.get(type);
        if (result == null) {
            return TYPE_ALL;
        }
        return result;
    }
}
