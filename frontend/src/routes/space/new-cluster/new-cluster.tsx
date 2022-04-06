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
import { NewClusterStepsEnum } from './new-cluster.data';
import { ClusterPlan } from './steps/cluster-plan/cluster-plan';
import { NodeConfig } from './steps/node-config/node-config';
import { useRecoilState } from 'recoil';
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
import { useTranslation } from 'react-i18next';

import { Navigate, Route, Routes, useLocation, useMatch, useNavigate } from 'react-router';
import { useSearchParams } from 'react-router-dom';
const { Step } = Steps;

const PREV_DISABLED_STEPS = [NewClusterStepsEnum[3], NewClusterStepsEnum[6], NewClusterStepsEnum[7]];
const NEXT_DISABLED_STEPS = [NewClusterStepsEnum[3], NewClusterStepsEnum[6]];

export function NewCluster() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const [step, setStep] = React.useState(0);
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);
    const [requestInfo, setRequestInfo] = useRecoilState(requestInfoState);
    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const [searchParams] = useSearchParams();
    const requestId = searchParams.get('requestId');
    const match = useMatch('space/new/:requestId/:step');

    useEffect(() => {
        if (location.pathname === '/space/list') {
            return;
        }
        const step = match?.params.step as string;
        setStep(NewClusterStepsEnum[step]);
        setStepDisabled({
            ...stepDisabled,
            next: NEXT_DISABLED_STEPS.includes(step),
            prev: PREV_DISABLED_STEPS.includes(step),
        });
        if (requestId && +requestId !== 0) {
            getRequestInfo();
        }
    }, [location.pathname]);

    async function getRequestInfo() {
        const res = await SpaceAPI.getRequestInfo(requestId as string);
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
            params.agentPort = value.agentPort ? parseInt(value.agentPort) : value.agentPort;
            isParamsValid =
                checkParam(params.installInfo, '请填写代码包路径') &&
                checkParam(params.packageInfo, '请填写安装路径') &&
                checkParam(params.agentPort, '请填写Agent启动端口');
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
                navigate(`/space/new/${res.data.requestId}/${NewClusterStepsEnum[newStep]}`);
            }, 0);
        } else {
            message.error(res.msg);
        }
    }

    function prevStep() {
        const newStep = step - 1;
        setStep(newStep);
        navigate(`/space/new/${requestInfo.requestId}/${NewClusterStepsEnum[newStep]}`);
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
            navigate(`/space/list`);
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
                            <Step title={t`Space Register`} description="&nbsp;&nbsp;" />
                            <Step title={t`AddHost`} description="&nbsp;&nbsp;" />
                            <Step title={t`SetupOptions`} description="&nbsp;&nbsp;" />
                            <Step title={t`CheckoutHost`} description="&nbsp;&nbsp;" />
                            <Step title={t`PlaningNodes`} description="&nbsp;&nbsp;" />
                            <Step title={t`SetUpParameters`} description="&nbsp;&nbsp;" />
                            <Step title={t`DeployCluster`} description="&nbsp;&nbsp;" />
                            <Step title={t`creationComplete`} description="&nbsp;&nbsp;" />
                        </Steps>
                    </div>
                    <div style={{ marginRight: 240 }}>
                        <Routes>
                            <Route path={`register-space`} element={<SpaceRegister />} />
                            <Route path={`add-node`} element={<AddNode />} />
                            <Route path={`install-options`} element={<InstallOptions />} />
                            <Route path={`verify-node`} element={<NodeVerify />} />
                            <Route path={`cluster-plan`} element={<ClusterPlan />} />
                            <Route path={`node-config`} element={<NodeConfig />} />
                            <Route path={`cluster-deploy`} element={<ClusterDeploy />} />
                            <Route path={`finish`} element={<SpaceCreateFinish />} />
                            <Route path="/" element={<Navigate replace to="register-space" />} />
                        </Routes>
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
                                        {t`previousStep`}
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
                                        {t`Accomplish`}
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
                                        {t`nextStep`}
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
