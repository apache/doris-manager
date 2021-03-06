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

import { http, isSuccess } from '@src/utils/http';

export function getClusterOverview() {
    return http.get('/api/cluster/overview').then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function startCluster(cluster_id: number) {
    return http.post('/api/control/cluster/start', { cluster_id }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function stopCluster(cluster_id: number) {
    return http.post('/api/control/cluster/stop', { cluster_id }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function restartCluster(cluster_id: number) {
    return http.post('/api/control/cluster/restart', { cluster_id }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function getNodeList(clusterId: number) {
    return http.get(`/api/control/cluster/${clusterId}/instances`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function getConfigurationList(type: 'be' | 'fe') {
    return http.post(`/api/rest/v2/manager/node/configuration_info?type=${type}`, { type }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

interface ChangeConfigurationParams {
    node: string[];
    persist: 'true' | 'false';
    value: string;
}

export function changeConfiguration(type: 'be' | 'fe', data: Record<string, ChangeConfigurationParams>) {
    return http.post(`/api/rest/v2/manager/node/set_config/${type}`, data).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}
