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

export  const BASE_FE_CONFIG = [
    {
        key: '0',
        config_item: 'JVM_XMX',
        default_value: '8g',
        desc: '待补充',
    },
    {
        key: '1',
        config_item: 'http_port',
        default_value: '8030',
        desc: '待补充',
    },
    {
        key: '2',
        config_item: 'rpc_port',
        default_value: '9020',
        desc: '待补充',
    },
    {
        key: '3',
        config_item: 'query_port',
        default_value: '9030',
        desc: '待补充',
    },
    {
        key: '4',
        config_item: 'edit_log_port',
        default_value: '9010',
        desc: '待补充',
    },
    {
        key: '5',
        config_item: 'priority_networks',
        default_value: '10.10.10.0/24',
        desc: '待补充',
    },
    {
        key: '6',
        config_item: 'meta_dir',
        default_value: '/usr/local/doris/fe/doris-meta',
        desc: '待补充',
    },
]
export  const BASE_BE_CONFIG = [
    {
        key: '0',
        config_item: 'be_port',
        default_value: '9060',
        desc: '待补充',
    },
    {
        key: '1',
        config_item: 'webserver_port',
        default_value: '8040',
        desc: '待补充',
    },
    {
        key: '2',
        config_item: 'heartbeat_service_port',
        default_value: '9050',
        desc: '待补充',
    },
    {
        key: '3',
        config_item: 'brpc_port',
        default_value: '8060',
        desc: '待补充',
    },
    {
        key: '4',
        config_item: 'priority_networks',
        default_value: '10.10.10.0/24',
        desc: '待补充',
    },
    {
        key: '5',
        config_item: 'storage_root_path',
        default_value: '/data01',
        desc: '待补充',
    }
]
export  const BASE_BROKER_CONFIG = [
    {
        key: '0',
        config_item: 'broker_ipc_port',
        default_value: '8000',
        desc: '待补充',
    }
]