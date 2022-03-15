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

export interface GetMetaInfoRequestParams {
    nsId: string;
}
export interface MetaInfoResponse {
    beCount: number;
    dbCount: number;
    diskOccupancy: number;
    feCount: number;
    remainDisk: number;
    tblCount: number;
}
export interface Space {
    name: string;
    description?: string;
    id: string;
}

export interface ISpaceParam {
    name: string;
    describe?: string;
    spaceId: string | number;
    spaceAdminUsers: number[];
}


export interface ISpaceUser {
    name: string;
    email: string;
    id: number;
    is_active: boolean
}
export type IRequiredMark = boolean | 'optional';

export interface AuthInfo {
    sshKey: string;
    sshPort: number;
    sshUser: string;
}

export interface ClusterAccessInfo {
    address: string;
    httpPort: number;
    passwd: string;
    queryPort: number;
    type: string;
    user: string;
}

export interface Cluster {
    address: string;
    httpPort: number;
    passwd: string;
    queryPort: number;
    type: string;
    user: string;
}

export interface SpaceInfo {
    cluster?: Cluster;
    describe: string;
    name: string;
    spaceAdminUsers: number[];
}

export interface ClusterAccessParams {
    cluster_id: string;
    request_id: string;
    event_type: string;
    authInfo?: AuthInfo;
    clusterAccessInfo?: ClusterAccessInfo;
    installInfo?: string;
    spaceInfo?: SpaceInfo;
}