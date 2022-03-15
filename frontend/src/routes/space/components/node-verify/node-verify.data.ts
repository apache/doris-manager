import { StepsProps } from "antd";

export enum NodeVerifyStepEnum {
    ACCESS_AUTH,
    INSTALL_DIR_CHECK,
    JDK_CHECK,
    AGENT_DEPLOY,
    AGENT_START,
    AGENT_REGISTER,
}

export namespace NodeVerifyStepEnum {
    export function getTitle(step: NodeVerifyStepEnum) {
        switch(step) {
            case NodeVerifyStepEnum.ACCESS_AUTH:
                return 'SSH校验';
            case NodeVerifyStepEnum.INSTALL_DIR_CHECK:
                return '安装路径校验';
            case NodeVerifyStepEnum.JDK_CHECK:
                return 'JDK依赖检查';
            case NodeVerifyStepEnum.AGENT_DEPLOY:
                return 'Agent安装';
            case NodeVerifyStepEnum.AGENT_START:
                return 'Agent启动';
            case NodeVerifyStepEnum.AGENT_REGISTER:
                return 'Agent注册';

        }
    }
}