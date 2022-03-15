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

import { http } from '@src/utils/http';
import { GlobalSettingItem } from './types';

export function fetchGlobalSettingsApi() {
    return http.get('/api/setting/global').then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    }) as Promise<GlobalSettingItem[]>;
}

export interface RemoteSettingParams extends GlobalSettingItem {
    type?: string;
}

export function changeSettingApi(key: string, params: RemoteSettingParams) {
    return http.put(`/api/setting/${key}`, params).then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function changeEmailSettingApi(params: Record<string, string>) {
    return http.put('/api/email/', params).then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function deleteEmailSettingApi() {
    return http.delete('/api/email/').then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function sendTestEmailApi(params: { email: string }) {
    return http.post('/api/email/test/', params).then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function getLdapSettingsApi() {
    return http.get('/api/ldap/setting').then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}
