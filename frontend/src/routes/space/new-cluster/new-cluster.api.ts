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

import { IResult } from "@src/interfaces/http.interface";
import { API_SERVER_PREFIX } from "@src/utils/api";
import { http } from "@src/utils/http";
import * as types from "./types/index.type";


function getTaskStatus(data: number | string ): Promise<IResult<any>> {
    return http.get(`${API_SERVER_PREFIX}/process/${data}/currentTasks`);
}
function getTaskLog(taskId: number | string ): Promise<IResult<any>> {
    return http.get(`${API_SERVER_PREFIX}/process/task/log/${taskId}`);
}
function reTryTask(taskId: number | string ): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/process/task/retry/${taskId}`);
}
function skipTask(taskId: number | string ): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/process/task/skip/${taskId}`);
}

function getRoleList(data: {clusterId: number | string}): Promise<IResult<types.IroleListResult[]>> {
    return http.get(`${API_SERVER_PREFIX}/server/roleList`, data);
}


function createCluster(data?: types.CreateClusterRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/server/installAgent`, data);
}

function installService(data?: types.InstallServiceRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/installService`, data);
}
function getNodeHardware(data: {clusterId: number | string}): Promise<IResult<types.IroleListResult[]>> {
    return http.get(`${API_SERVER_PREFIX}/agent/hardware/0`);
}

function deployConfig(data?: types.DeployConfigRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/deployConfig`, data);
}

function startService(data?: types.StartServiceRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/startService`, data);
}
function startCluster(data?: types.StartClusterRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/buildCluster`, data);
}
function installComplete(processId: number): Promise<IResult<any>> {
    return http.post(`/api/process/installComplete/${processId}`);
}

function getCurrentProcess(): Promise<IResult<types.IcurrentProcess>> {
    return http.get(`/api/process/currentProcess`);
}


function goBackProcess(processId: number): Promise<IResult<types.IcurrentProcess>> {
    return http.post(`/api/process/back/${processId}`);
}
export default {
    getTaskStatus,
    getTaskLog,
    reTryTask,
    skipTask,
    getRoleList,
    
    createCluster,
    installService,
    getNodeHardware,
    deployConfig,
    startService,
    startCluster,
    getCurrentProcess,
    goBackProcess,
    installComplete
}