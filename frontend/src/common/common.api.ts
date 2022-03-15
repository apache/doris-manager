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
import { IMember } from './common.interface';

function getUserInfo() {
    return http.get(`/api/v2/user/current`);
}

function getRoleMembersById(data: { roleId: string }) {
    return http.get<IMember>(`/api/permissions/group/${data.roleId}`);
}

function getRoles() {
    return http.get('/api/permissions/group');
}

function getUsers(params?: { include_deactivated: boolean }) {
    return http.get('/api/v2/user/', params);
}

function getSpaceUsers() {
    return http.get('/api/v2/user/space');
}

export const CommonAPI = {
    getUserInfo,
    getRoleMembersById,
    getRoles,
    getUsers,
    getSpaceUsers,
};
