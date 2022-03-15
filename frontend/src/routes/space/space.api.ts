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
/** @format */

import { http } from '@src/utils/http';
import { ISpaceParam, ISpaceUser, ClusterAccessParams } from './space.interface';
import { IResult } from 'src/interfaces/http.interface';
function spaceCreate(params: any): Promise<IResult<any>> {
    return http.post(`/api/space/create`, params);
}
function spaceList(): Promise<IResult<any[]>> {
    return http.get(`/api/space/all`);
}
function spaceCheck(name: string): Promise<IResult<any>> {
    return http.post(`/api/space/name/check`, { name: name && name.trim() });
}
function spaceValidate(data: any): Promise<IResult<any>> {
    return http.post(`/api/space/validate`, data);
}
function spaceDelete(spaceId: string): Promise<IResult<any>> {
    return http.delete(`/api/space/${spaceId}`);
}
function spaceGet(spaceId: string): Promise<IResult<any>> {
    return http.get(`/api/space/${spaceId}`);
}
function spaceUpdate(data: ISpaceParam): Promise<IResult<any>> {
    return http.put(`/api/space/${data.spaceId}/update`, data);
}
function getUsers(params?: { include_deactivated: boolean }): Promise<IResult<ISpaceUser[]>> {
    return http.get(`/api/user/`, params);
}
function switchSpace(spaceId: string): Promise<IResult<ISpaceUser[]>> {
    return http.post(`/api/user/current?cluster_id=${spaceId}`);
}
function metaOption(): Promise<IResult<ISpaceUser[]>> {
    return http.post(`/api/v2/meta/`);
}

function createCluster(data: any) {
    return http.post('/api/control/cluster/creation', data);
}

function accessCluster(data: ClusterAccessParams) {
    return http.post(`/api/control/cluster/takeOver`, data);
}
function getRequestInfo(requestId: string) {
    return http.get(`/api/control/request/${requestId}/info`);
}
function getClusterNodes<T>(data: any): Promise<IResult<T>> {
    return http.get(`/api/control/cluster/${data.clusterId}/nodes`);
}
function nodeVerify(data: any) {
    return http.get(`/api/control/cluster/{clusterId}/nodes`, data);
}
function getClusterInstance<T>(data: any): Promise<IResult<T>> {
    return http.get(`/api/control/cluster/${data.clusterId}/instances`);
}
function getClusterModule(data: any) {
    return http.get(`/api/control/cluster/${data.clusterId}/modules`);
}

function getJDBCReady<T>(data: any): Promise<IResult<T>> {
    return http.get(`/api/control/cluster/${data.clusterId}/jdbc/service/ready`);
}

export const SpaceAPI = {
    spaceCreate,
    spaceList,
    spaceCheck,
    spaceValidate,
    spaceDelete,
    spaceGet,
    spaceUpdate,
    getUsers,
    switchSpace,
    metaOption,
    createCluster,
    accessCluster,
    nodeVerify,
    getClusterNodes,
    getClusterInstance,
    getClusterModule,
    getRequestInfo,
    getJDBCReady,
};
