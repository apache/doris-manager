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

package org.apache.doris.manager.common.util;

import com.google.common.collect.Maps;

import java.util.Map;

public class ConfigDefault {
    private ConfigDefault() {
        throw new UnsupportedOperationException();
    }

    public static final String FE_LOG_CONFIG_NAME = "LOG_DIR";
    public static final String FE_META_CONFIG_NAME = "meta_dir";

    public static final String BE_LOG_CONFIG_NAME = "PPROF_TMPDIR";
    public static final String BE_DATA_CONFIG_NAME = "storage_root_path";

    public static final String FE_HTTP_PORT_CONFIG_NAME = "http_port";
    public static final String FE_QUERY_PORT_CONFIG_NAME = "query_port";
    public static final String FE_EDIT_LOG_PORT = "edit_log_port";

    public static final String BE_HEARTBEAT_PORT_CONFIG_NAME = "heartbeat_service_port";
    public static final String BE_WEBSERVER_PORT_NAME = "webserver_port";

    public static final String BROKER_PORT_CONFIG_NAME = "broker_ipc_port";

    public static final Map<String, String> FE_CONFIG_DEDAULT;

    public static final Map<String, String> BE_CONFIG_DEDAULT;

    public static final Map<String, String> BROKER_CONFIG_DEDAULT;

    static {
        FE_CONFIG_DEDAULT = Maps.newHashMap();
        FE_CONFIG_DEDAULT.put("LOG_DIR", "/log");
        FE_CONFIG_DEDAULT.put("DATE", "`date +%Y%m%d-%H%M%S`");
        FE_CONFIG_DEDAULT.put("JAVA_OPTS", "\"-Xmx4096m -XX:+UseMembar -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=7 "
                + "-XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC -XX:+UseParNewGC "
                + "-XX:+CMSClassUnloadingEnabled -XX:-CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=80 "
                + "-XX:SoftRefLRUPolicyMSPerMB=0 -Xloggc:$DORIS_HOME/log/fe.gc.log.$DATE\"");
        FE_CONFIG_DEDAULT.put("JAVA_OPTS_FOR_JDK_9", "\"-Xmx4096m -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=7 "
                + "-XX:+CMSClassUnloadingEnabled -XX:-CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=80 "
                + "-XX:SoftRefLRUPolicyMSPerMB=0 -Xlog:gc*:$DORIS_HOME/log/fe.gc.log.$DATE:time\"");
        FE_CONFIG_DEDAULT.put("sys_log_level", "INFO");
        FE_CONFIG_DEDAULT.put("meta_dir", "/doris-meta");
        FE_CONFIG_DEDAULT.put("http_port", "8030");
        FE_CONFIG_DEDAULT.put("rpc_port", "9020");
        FE_CONFIG_DEDAULT.put("query_port", "9030");
        FE_CONFIG_DEDAULT.put("edit_log_port", "9010");
        FE_CONFIG_DEDAULT.put("mysql_service_nio_enabled", "true");

        BE_CONFIG_DEDAULT = Maps.newHashMap();
        BE_CONFIG_DEDAULT.put("PPROF_TMPDIR", "/log/");
        BE_CONFIG_DEDAULT.put("sys_log_level", "INFO");
        BE_CONFIG_DEDAULT.put("be_port", "9060");
        BE_CONFIG_DEDAULT.put("be_rpc_port", "9070");
        BE_CONFIG_DEDAULT.put("webserver_port", "8040");
        BE_CONFIG_DEDAULT.put("heartbeat_service_port", "9050");
        BE_CONFIG_DEDAULT.put("brpc_port", "8060");
        BE_CONFIG_DEDAULT.put("storage_root_path", "/storage");

        BROKER_CONFIG_DEDAULT = Maps.newHashMap();
        BROKER_CONFIG_DEDAULT.put("broker_ipc_port", "8111");
        BROKER_CONFIG_DEDAULT.put("client_expire_seconds", "300");
    }
}
