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

import { http } from "@src/utils/http";

function createRole(data: {name: string}) {
    return http.post('/api/permissions/group', data)
}

function updateRole(data: {id: number, name: string}) {
    return http.put(`/api/permissions/group/${data.id}`, {name: data.name})
}

function deleteRole(data: {roleId: number}) {
    return http.delete(`/api/permissions/group/${data.roleId}`)
}

function deleteMember(data: {membership_id: number}) {
    return http.delete(`/api/permissions/membership/${data.membership_id}`)
}

function addMember(data: {group_id: number, user_ids: number[]}) {
    return http.post('/api/permissions/memberships', data)
}

export const RoleAPI = {
    createRole,
    updateRole,
    deleteRole,
    addMember,
    deleteMember
}