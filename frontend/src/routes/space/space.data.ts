import { StepsProps } from "antd";

export enum OperateStatusEnum {
    INIT = 'INIT',
    PROCESSING = 'PROCESSING',
    SUCCESS = 'SUCCESS',
    FAIL = 'FAIL',
    CANCEL = 'CANCEL',
}

export namespace OperateStatusEnum {
    export function getStepStatus(state: OperateStatusEnum): StepsProps['status'] {
        switch(state) {
            case OperateStatusEnum.INIT:
                return 'process';
            case OperateStatusEnum.PROCESSING:
                return 'process';
            case OperateStatusEnum.SUCCESS:
                return 'finish';
            case OperateStatusEnum.FAIL:
                return 'error';   
            case OperateStatusEnum.CANCEL:
                return 'error'; 
        }
    }
}