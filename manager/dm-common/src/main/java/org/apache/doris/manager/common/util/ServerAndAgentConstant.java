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

public class ServerAndAgentConstant {

    private ServerAndAgentConstant() {
        throw new UnsupportedOperationException();
    }

    public static final String USER_ADMIN = "admin";
    public static final String USER_ROOT = "root";

    // TODO:Later, it will be defined uniformly through the cluster module template
    public static final String FE_NAME = "fe";
    public static final String BE_NAME = "be";
    public static final String BROKER_NAME = "broker";
    public static final String AGENT_NAME = "agent";

    // The path of borker module initialization when the installation package is downloaded
    public static final String BROKER_INIT_SUB_DIR = "apache_hdfs_broker";
    public static final String BAIDU_BROKER_INIT_SUB_DIR = "baidu_doris_broker";

    public static final String DORIS_INSTALL_HOME_EVN = "DORIS_INSTALL_HOME";
    public static final String DORIS_PACKAGE_URL_ENV = "DORIS_PACKAGE_URL";

    public static final String FE_PID_FILE = "fe.pid";
    public static final String BE_PID_FILE = "be.pid";
    public static final String BROKER_PID_FILE = "apache_hdfs_broker.pid";
    public static final String BAIDU_BROKER_PID_FILE = "baidu_doris_broker.pid";

    public static final String FE_PID_NAME = "PaloFe";
    public static final String BE_PID_NAME = "doris_be";
    public static final String BROKER_PID_NAME = "BrokerBootstrap";

    public static final String FE_START_SCRIPT = "start_fe.sh";
    public static final String BE_START_SCRIPT = "start_be.sh";
    public static final String BROKER_START_SCRIPT = "start_broker.sh";

    public static final String FE_STOP_SCRIPT = "stop_fe.sh";
    public static final String BE_STOP_SCRIPT = "stop_be.sh";
    public static final String BROKER_STOP_SCRIPT = "stop_broker.sh";

    public static final String FE_CONF_FILE = "fe.conf";
    public static final String BE_CONF_FILE = "be.conf";
    public static final String BROKER_CONF_FILE = "apache_hdfs_broker.conf";
    public static final String BAIDU_BROKER_CONF_FILE = "baidu_doris_broker.conf";

    public static final String PACKAGE_DOWNLOAD_SCRIPT = "download_doris.sh";
    public static final int SHELL_TIME_OUT = 2;

    // TODO:Later, it will be defined uniformly through the cluster module service template
    public static final String FE_JDBC_SERVICE = "fe_jdbc";
    public static final String FE_HTTP_SERVICE = "fe_http";
    public static final String FE_EDIT_SERVICE = "fe_edit";

    public static final String BE_HEARTBEAT_SERVICE = "be_heartbeat";
    public static final String BE_HTTP_SERVICE = "be_http";

    public static final String BROKER_PRC_SERVICE = "broker_rpc";

    public static final Map<String, String> BAIDU_BROKER_CONFIG_DEDAULT;

    static {
        BAIDU_BROKER_CONFIG_DEDAULT = Maps.newHashMap();
        BAIDU_BROKER_CONFIG_DEDAULT.put("afs_filesystem_impl", "org.apache.hadoop.fs.DFileSystem");
        BAIDU_BROKER_CONFIG_DEDAULT.put("hdfs_filesystem_impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        BAIDU_BROKER_CONFIG_DEDAULT.put("bos_filesystem_impl", "org.apache.hadoop.fs.bos.BaiduBosFileSystem");
        BAIDU_BROKER_CONFIG_DEDAULT.put("afs_agent_port", "20001");
        BAIDU_BROKER_CONFIG_DEDAULT.put("hdfs_agent_port", "20002");
        BAIDU_BROKER_CONFIG_DEDAULT.put("afs_client_auth_method", "3");
        BAIDU_BROKER_CONFIG_DEDAULT.put("hdfs_client_auth_method", "2");
        BAIDU_BROKER_CONFIG_DEDAULT.put("bos_client_auth_method", "2");
    }

}
