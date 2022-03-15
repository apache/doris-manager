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

export function getSpaceMembersAPI() {
    return http.get(`/api/v2/user/space`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function removeMemberFromSpaceAPI(userId: number) {
    return http.delete(`/api/v2/user/move/${userId}`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function addMemberToSpaceAPI(userId: number) {
    return http.post(`/api/v2/user/add/${userId}`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}