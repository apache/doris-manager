import * as types from './params.type';

export enum taskStatusEnum {
    "SUBMITTED" = "SUBMITTED", //已提交
    "RUNNING"= "RUNNING",
    "SUCCESS" = "SUCCESS",
    "FAILURE" = "FAILURE"
}
export enum taskTypeEnum {
    "INSTALL_AGENT" = "INSTALL_AGENT", //安装Agent
    "INSTALL_FE" =  "INSTALL_FE",
    "INSTALL_BE" = "INSTALL_BE",
    "DEPLOY_FE_CONFIG" = "DEPLOY_FE_CONFIG",
    "DEPLOY_BE_CONFIG" = "DEPLOY_BE_CONFIG",
    "START_BE" = "START_BE",
    "JOIN_BE" = "JOIN_BE" //加入集群
}
export enum processTypeEnum {
    "INSTALL_AGENT" =  "INSTALL_AGENT" , //安装Agent
    "INSTALL_SERVICE" = "INSTALL_SERVICE",
    "DEPLOY_CONFIG" = "DEPLOY_CONFIG", //分发配置
    "START_SERVICE" = "START_SERVICE", //启动服务
    "BUILD_CLUSTER" = "BUILD_CLUSTER", //组件集群
}
export interface ItaskResult{
    endTime: string;
    executorId: null;
    finish:  "NO"| "YES";
    host: string;
    id: number;
    processId: number;
    processType: processTypeEnum;
    result: string;
    skip: "NO"| "YES";
    startTime: string;
    status: taskStatusEnum;
    taskJson: string;
    taskRole: types.dorisRoleEnum;
    taskType: taskStatusEnum;
}

export interface IroleListResult{
    id: number;
    host: string;
    clusterId: number;
    role: types.dorisRoleEnum,
    feNodeType: types.feNodeType,
    installDir: string;
    register: "NO"| "YES"
}


export interface IroleListResult{
    cpu: string;
    totalMemory: string;
}

export interface IcurrentProcess{
    clusterId: number;
    id: number;
    processStep: number;
}