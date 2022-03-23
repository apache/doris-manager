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

import ProCard from '@ant-design/pro-card';
import { Button, message, Row, Space, Steps } from 'antd';
import React, { useEffect, useState } from 'react';
import { Switch, Route, Redirect, useRouteMatch, useHistory } from 'react-router';
import { NewClusterStepsEnum } from './new-cluster.data';
import { ClusterPlan } from './steps/cluster-plan/cluster-plan';
import { NodeConfig } from './steps/node-config/node-config';
import { RunCluster } from './steps/run-cluster/run-cluster';
import { stepState, CurrentProcessQuery, processId } from './recoils/index';
import { useRecoilState, useRecoilValue } from 'recoil';
import CacheRoute, { CacheSwitch } from 'react-router-cache-route';
import { pathToRegexp } from 'path-to-regexp';
import { AddNode } from './steps/add-node/add-node';
import { InstallOptions } from './steps/install-options/install-options';
import { ClusterDeploy } from './steps/cluster-deploy/cluster-deploy';
import { SpaceCreateFinish } from '../components/finish/finish';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { useForm } from 'antd/lib/form/Form';
import { SpaceRegister } from '../components/space-register/space-register';
import { NodeVerify } from '../components/node-verify/node-verify';
import { SpaceAPI } from '../space.api';
import { isSuccess } from '@src/utils/http';
import { requestInfoState, stepDisabledState } from '../access-cluster/access-cluster.recoil';
import { checkParam } from '../space.utils';
const { Step } = Steps;

const PREV_DISABLED_STEPS = [NewClusterStepsEnum[3], NewClusterStepsEnum[6], NewClusterStepsEnum[7]];
const NEXT_DISABLED_STEPS = [NewClusterStepsEnum[3], NewClusterStepsEnum[6]];

export function NewCluster(props: any) {
    const match = useRouteMatch<{ requestId: string }>();
    const history = useHistory();
    const [step, setStep] = React.useState(0);
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);
    // const [curStep, setStepState] = useRecoilState(stepState);
    const [curProcessId, setProcessId] = useRecoilState(processId);
    const [requestInfo, setRequestInfo] = useRecoilState(requestInfoState);
    const [loading, setLoading] = useState(false);
    const [form] = useForm();

    useEffect(() => {
        if (history.location.pathname === '/space/list') {
            return;
        }
        const regexp = pathToRegexp(`${match.path}/:step`);
        const paths = regexp.exec(history.location.pathname);
        const step = (paths as string[])[2];
        setStep(NewClusterStepsEnum[step]);
        setStepDisabled({
            ...stepDisabled,
            next: NEXT_DISABLED_STEPS.includes(step),
            prev: PREV_DISABLED_STEPS.includes(step),
        });
        if (match.params.requestId && +match.params.requestId !== 0) {
            getRequestInfo();
        }
    }, [history.location.pathname]);

    async function getRequestInfo() {
        const requestId = match.params.requestId;
        const res = await SpaceAPI.getRequestInfo(requestId);
        if (isSuccess(res)) {
            setRequestInfo(res.data);
        }
    }

    async function nextStep() {
        const value = form.getFieldsValue();
        const newStep = step + 1;
        let isParamsValid = true;
        const params = {
            ...requestInfo.reqInfo,
            cluster_id: requestInfo.clusterId,
            request_id: requestInfo.requestId,
            event_type: (step + 1).toString(),
        };
        if (value && step === NewClusterStepsEnum['register-space']) {
            params.spaceInfo = {
                describe: value.describe,
                name: value.name,
                spaceAdminUsers: value.spaceAdminUsers,
            };
            isParamsValid =
                checkParam(params.spaceInfo.name, '请填写空间名称') &&
                checkParam(params.spaceInfo.spaceAdminUsers, '请填写管理员姓名');
        }
        if (value && step === NewClusterStepsEnum['add-node']) {
            params.authInfo = {
                sshKey: value.sshKey,
                sshPort: value.sshPort ? parseInt(value.sshPort) : value.sshPort,
                sshUser: value.sshUser,
            };
            params.hosts = value.hosts;
            isParamsValid =
                checkParam(params.authInfo.sshUser, '请填写SSH用户') &&
                checkParam(params.authInfo.sshPort, '请填写SSH端口') &&
                checkParam(params.authInfo.sshKey, '请填写SSH私钥') &&
                checkParam(params.hosts, '请填写节点列表');
        }
        if (value && step === NewClusterStepsEnum['install-options']) {
            params.installInfo = value.installDir;
            params.packageInfo = value.packageUrl;
            isParamsValid =
                checkParam(params.installInfo, '请填写代码包路径') && checkParam(params.packageInfo, '请填写安装路径');
        }
        if (value && step === NewClusterStepsEnum['cluster-plan']) {
            params.nodeConfig = value.nodeConfig;
            isParamsValid =
                checkParam(params.nodeConfig?.[0]?.nodeIds, '请分配FE节点') &&
                checkParam(params.nodeConfig?.[1]?.nodeIds, '请分配BE节点');
        }
        if (value && step === NewClusterStepsEnum['node-config']) {
            params.deployConfigs = value.deployConfigs;
            isParamsValid = params.deployConfigs.every((node: any) => {
                return node.configs.every((config: any) => {
                    return checkParam(config.value, `请完整配置${node.moduleName.toUpperCase()}节点参数`);
                });
            });
        }
        if (!isParamsValid) return;
        setLoading(true);
        const res = await SpaceAPI.createCluster(params);
        setLoading(false);
        if (isSuccess(res)) {
            setRequestInfo(res.data);
            setStep(newStep);
            setTimeout(() => {
                history.push(`/space/new/${res.data.requestId}/${NewClusterStepsEnum[newStep]}`);
            }, 0);
        } else {
            message.error(res.msg);
        }
    }

    function prevStep() {
        const newStep = step - 1;
        setStep(newStep);
        history.push(`/space/new/${requestInfo.requestId}/${NewClusterStepsEnum[newStep]}`);
    }

    async function finish() {
        const value = form.getFieldsValue();
        const params = {
            ...requestInfo.reqInfo,
            cluster_id: requestInfo.clusterId,
            request_id: requestInfo.requestId,
            event_type: (step + 1).toString(),
        };
        params.clusterPassword = value.clusterPassword;
        const isFinishParamValid = checkParam(params.clusterPassword, '请设定集群root密码');
        if (!isFinishParamValid) return;
        setLoading(true);
        const res = await SpaceAPI.createCluster(params);
        setLoading(false);
        if (isSuccess(res)) {
            history.push(`/space/list`);
        } else {
            message.error(res.msg);
        }
    }

    return (
        <>
            <NewSpaceInfoContext.Provider
                value={{
                    step,
                    form,
                    reqInfo: requestInfo.reqInfo || { authInfo: {}, spaceInfo: {}, clusterAccessInfo: {} },
                }}
            >
                <ProCard style={{ marginTop: 20 }}>
                    <div style={{ position: 'fixed', top: 80, right: 80 }}>
                        <Steps direction="vertical" current={step} style={{ padding: '20px 0 40px 0' }}>
                            <Step title="注册空间" description="&nbsp;&nbsp;" />
                            <Step title="添加主机" description="&nbsp;&nbsp;" />
                            <Step title="安装选项" description="&nbsp;&nbsp;" />
                            <Step title="校验主机" description="&nbsp;&nbsp;" />
                            <Step title="规划节点" description="&nbsp;&nbsp;" />
                            <Step title="配置参数" description="&nbsp;&nbsp;" />
                            <Step title="部署集群" description="&nbsp;&nbsp;" />
                            <Step title="完成创建" description="&nbsp;&nbsp;" />
                        </Steps>
                    </div>
                    <div style={{ marginRight: 240 }}>
                        <CacheSwitch>
                            <CacheRoute path={`${match.path}/register-space`} component={SpaceRegister} />
                            <CacheRoute path={`${match.path}/add-node`} component={AddNode} />
                            <CacheRoute path={`${match.path}/install-options`} component={InstallOptions} />
                            <CacheRoute path={`${match.path}/verify-node`} component={NodeVerify} />
                            <CacheRoute path={`${match.path}/cluster-plan`} component={ClusterPlan} />
                            <CacheRoute path={`${match.path}/node-config`} component={NodeConfig} />
                            <CacheRoute path={`${match.path}/cluster-deploy`} component={ClusterDeploy} />
                            <CacheRoute path={`${match.path}/finish`} component={SpaceCreateFinish} />
                            <Redirect to={`${match.path}/register-space`} />
                        </CacheSwitch>
                        <Row justify="end" style={{ marginTop: 20 }}>
                            <Space>
                                {step === 0 ? (
                                    <></>
                                ) : (
                                    <Button
                                        type="default"
                                        onClick={() => {
                                            prevStep();
                                        }}
                                        disabled={stepDisabled.prev}
                                    >
                                        上一步
                                    </Button>
                                )}
                                {step === NewClusterStepsEnum['finish'] ? (
                                    <Button
                                        type="primary"
                                        loading={loading}
                                        onClick={() => {
                                            finish();
                                        }}
                                    >
                                        完成
                                    </Button>
                                ) : (
                                    <Button
                                        type="primary"
                                        onClick={() => {
                                            nextStep();
                                        }}
                                        disabled={stepDisabled.next}
                                        loading={loading}
                                    >
                                        下一步
                                    </Button>
                                )}
                            </Space>
                        </Row>
                    </div>
                </ProCard>
            </NewSpaceInfoContext.Provider>
        </>
    );
}
