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

import { DEFAULT_GLOBAL_ROUTES, Route } from './global.routes';
import Certificate from './routes/certificate';

export function getValueFromJson(key: string, defaultValue?: any) {
    let res = defaultValue;
    try {
        res = JSON.parse(key);
    } catch (e) {}
    return res;
}

export function getProtocol(url: string) {
    const match = /^https?:\/\//.exec(url);
    return match ? match[0] : '';
}

export function getAddress(url: string) {
    const match = /^https?:\/\//.exec(url);
    return match ? url.slice(match[0].length) : url || '';
}

export function getGlobalRoutes(isLdap: boolean) {
    return [
        isLdap && {
            path: 'certificate',
            label: '认证',
            component: Certificate,
        },
        ...DEFAULT_GLOBAL_ROUTES,
    ].filter(Boolean) as Route[];
}
