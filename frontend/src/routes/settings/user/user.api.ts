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

function getUsers(data: { include_deactivated: boolean; cluster_id?: number }) {
    return http.get('/api/v2/user/', data);
}
function createUser(data: { name: string; email: string; password: string }) {
    return http.post('/api/v2/user/', data);
}
function updateUser(data: { name: string; user_id: number; email: string }) {
    return http.put(`/api/v2/user/${data.user_id}`, data);
}

function updateUserAdmin(data: { admin: boolean; user_id: number }) {
    return http.put(`/api/v2/user/${data.user_id}/admin`, { admin: data.admin });
}

function deactivateUser(data: { user_id: number }) {
    return http.delete(`/api/v2/user/${data.user_id}`);
}

function activateUser(data: { user_id: number }) {
    return http.put(`/api/v2/user/${data.user_id}/reactivate`);
}

function resetPassword(data: { user_id: number; password: string }) {
    return http.put(`/api/v2/user/${data.user_id}/password`, { password: data.password });
}

function syncLdapUser() {
    return http.get('/api/setting/syncLdapUser').then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export const UserAPI = {
    getUsers,
    createUser,
    updateUser,
    updateUserAdmin,
    deactivateUser,
    activateUser,
    resetPassword,
    syncLdapUser
};
