export interface CreateClusterRequestParams {
    clusterId: string;
    packageUrl: string;
    installDir: string;
    hosts: string[];
    user: string;
    sshPort: number;
    sshKey: string;
}
export enum DorisNodeTypeEnum {
    FE = "FE", 
    BE = "BE",
    BROKER = "BROKER"
}
export enum feNodeType {
    FOLLOWER = "FOLLOWER",
    OBSERVER = "OBSERVER"
}
export interface InstallServiceRequestParams {
    processId: number;
    installInfos: {
        host: string;
        role: DorisNodeTypeEnum;
        feNodeType?: feNodeType;
    }[];
}

export interface DeployConfigRequestParams {
    processId: number;
    deployConfigs: {
        hosts: string[];
        role: DorisNodeTypeEnum;
        conf: string;
    }[];
}

export interface StartServiceRequestParams {
    processId: number;
    dorisStarts: {
        host: string;
        role: DorisNodeTypeEnum;
    }[];
}

export interface StartClusterRequestParams {
    processId: number;
    feHosts: string[];
    beHosts: string[];
}