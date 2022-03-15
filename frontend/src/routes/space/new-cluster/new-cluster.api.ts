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
// 查看Agent上安装的角色列表
function getRoleList(data: {clusterId: number | string}): Promise<IResult<types.IroleListResult[]>> {
    return http.get(`${API_SERVER_PREFIX}/server/roleList`, data);
}

// 步骤一
function createCluster(data?: types.CreateClusterRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/server/installAgent`, data);
}
// 步骤二
function installService(data?: types.InstallServiceRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/installService`, data);
}
function getNodeHardware(data: {clusterId: number | string}): Promise<IResult<types.IroleListResult[]>> {
    return http.get(`${API_SERVER_PREFIX}/agent/hardware/0`);
}
// 步骤三
function deployConfig(data?: types.DeployConfigRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/deployConfig`, data);
}
// 步骤四
function startService(data?: types.StartServiceRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/startService`, data);
}
function startCluster(data?: types.StartClusterRequestParams): Promise<IResult<any>> {
    return http.post(`${API_SERVER_PREFIX}/agent/buildCluster`, data);
}
function installComplete(processId: number): Promise<IResult<any>> {
    return http.post(`/api/process/installComplete/${processId}`);
}
// 获取当前步骤信息
function getCurrentProcess(): Promise<IResult<types.IcurrentProcess>> {
    return http.get(`/api/process/currentProcess`);
}

// 回退上一步
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