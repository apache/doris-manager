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
import { NewSpaceInfoContext } from '@src/common/common.context';
import { useForm } from 'antd/lib/form/Form';
import { AccessClusterStepsEnum } from './access-cluster.data';
import { SpaceRegister } from '../components/space-register/space-register';
import { ConnectCluster } from './steps/connect-cluster/connect-cluster';
import { ManagedOptions } from './steps/managed-options/managed-options';
import { NodeVerify } from '../components/node-verify/node-verify';
import { isSuccess } from '@src/utils/http';
import { SpaceAPI } from '../space.api';
import { ClusterAccessParams } from '../space.interface';
import { useRecoilState } from 'recoil';
import { requestInfoState, stepDisabledState } from './access-cluster.recoil';
import { ClusterVerify } from './steps/cluster-verify/cluster-verify';
import { SpaceAccessFinish } from './steps/finish/finish';
import { checkParam } from '../space.utils';
import { Navigate, Route, Routes, useLocation, useMatch, useNavigate, useSearchParams } from 'react-router-dom';
const { Step } = Steps;

export function AccessCluster() {
    const location = useLocation();
    const navigate = useNavigate();
    const [step, setStep] = React.useState(0);
    const [loading, setLoading] = useState(false);
    const [requestInfo, setRequestInfo] = useRecoilState(requestInfoState);
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);
    const match = useMatch('space/access/:requestId/:step');
    const requestId = match?.params.requestId;
    const hidePrevSteps = [
        AccessClusterStepsEnum['space-register'],
        AccessClusterStepsEnum['node-verify'],
        AccessClusterStepsEnum['cluster-verify'],
        AccessClusterStepsEnum.finish,
    ];

    useEffect(() => {
        if (location.pathname === '/space/list') {
            return;
        }
        const step = match?.params.step as string;
        setStep(AccessClusterStepsEnum[step]);

        setStepDisabled({ ...stepDisabled, next: false });

        if (requestId && +requestId !== 0) {
            getRequestInfo();
        }
    }, [location.pathname]);

    const [form] = useForm();

    async function getRequestInfo() {
        const res = await SpaceAPI.getRequestInfo(requestId as string);
        if (isSuccess(res)) {
            setRequestInfo(res.data);
        }
    }

    async function nextStep() {
        const value = form.getFieldsValue();
        let isParamsValid = true;
        const newStep = step + 1;
        const params: ClusterAccessParams = {
            ...requestInfo.reqInfo,
            cluster_id: requestInfo.clusterId,
            request_id: requestInfo.requestId,
            event_type: (step + 1).toString(),
        };
        if (value && step === AccessClusterStepsEnum['space-register']) {
            params.spaceInfo = {
                describe: value.describe,
                name: value.name,
                spaceAdminUsers: value.spaceAdminUsers,
            };
            isParamsValid =
                checkParam(params.spaceInfo.name, '?????????????????????') &&
                checkParam(params.spaceInfo.spaceAdminUsers, '????????????????????????');
        }
        if (value && step === AccessClusterStepsEnum['connect-cluster']) {
            params.clusterAccessInfo = {
                address: value.address,
                httpPort: value.httpPort,
                passwd: value.passwd || '',
                queryPort: value.queryPort,
                type: value.type,
                user: value.user,
            };
            isParamsValid =
                checkParam(params.clusterAccessInfo.address, '?????????????????????') &&
                checkParam(params.clusterAccessInfo.httpPort, '?????????HTTP??????') &&
                checkParam(params.clusterAccessInfo.queryPort, '?????????JDBC??????') &&
                checkParam(params.clusterAccessInfo.user, '????????????????????????');
        }

        if (value && step === AccessClusterStepsEnum['managed-options']) {
            params.authInfo = {
                sshKey: value.sshKey,
                sshPort: value.sshPort,
                sshUser: value.sshUser,
            };
            params.installInfo = value.installInfo;
            params.agentPort = value.agentPort ? parseInt(value.agentPort) : value.agentPort;
            isParamsValid =
                checkParam(params.authInfo.sshUser, '?????????SSH??????') &&
                checkParam(params.authInfo.sshPort, '?????????SSH??????') &&
                checkParam(params.authInfo.sshKey, '?????????SSH??????') &&
                checkParam(params.installInfo, '?????????????????????') &&
                checkParam(params.agentPort, '?????????Agent????????????');
        }
        if (!isParamsValid) return;
        setLoading(true);
        const res = await SpaceAPI.accessCluster(params);
        setLoading(false);
        if (isSuccess(res)) {
            setRequestInfo(res.data);
            setStep(newStep);
            setStepDisabled({ ...stepDisabled, next: false });
            setTimeout(() => {
                navigate(`/space/access/${res.data.requestId}/${AccessClusterStepsEnum[newStep]}`);
            }, 0);
        } else {
            message.error(res.msg);
        }
    }

    function prevStep() {
        const newStep = step - 1;
        setStep(newStep);
        setStepDisabled({ ...stepDisabled, prev: false });
        navigate(`/space/access/${requestInfo.requestId}/${AccessClusterStepsEnum[newStep]}`);
    }

    function finish() {
        navigate('/space/list');
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
                            <Step title="????????????" description="&nbsp;&nbsp;" />
                            <Step title="????????????" description="&nbsp;&nbsp;" />
                            <Step title="????????????" description="&nbsp;&nbsp;" />
                            <Step title="????????????" description="&nbsp;&nbsp;" />
                            <Step title="????????????" description="&nbsp;&nbsp;" />
                            <Step title="????????????" description="&nbsp;&nbsp;" />
                        </Steps>
                    </div>
                    <div style={{ marginRight: 240 }}>
                        <Routes>
                            <Route path={`${AccessClusterStepsEnum[0]}`} element={<SpaceRegister />} />
                            <Route path={`${AccessClusterStepsEnum[1]}`} element={<ConnectCluster />} />
                            <Route path={`${AccessClusterStepsEnum[2]}`} element={<ManagedOptions />} />
                            <Route path={`${AccessClusterStepsEnum[3]}`} element={<NodeVerify />} />
                            <Route path={`${AccessClusterStepsEnum[4]}`} element={<ClusterVerify />} />
                            <Route path={`${AccessClusterStepsEnum[5]}`} element={<SpaceAccessFinish />} />
                            <Route path="/" element={<Navigate replace to={AccessClusterStepsEnum[0]} />} />
                        </Routes>
                        <Row justify="end" style={{ marginTop: 20 }}>
                            <Space>
                                {hidePrevSteps.includes(step) ? (
                                    <></>
                                ) : (
                                    <Button
                                        type="default"
                                        onClick={() => {
                                            prevStep();
                                        }}
                                    >
                                        ?????????
                                    </Button>
                                )}
                                {step === AccessClusterStepsEnum['finish'] ? (
                                    <Button
                                        type="primary"
                                        onClick={() => {
                                            finish();
                                        }}
                                    >
                                        ??????
                                    </Button>
                                ) : (
                                    <Button
                                        type="primary"
                                        onClick={() => {
                                            nextStep();
                                        }}
                                        loading={loading}
                                        disabled={stepDisabled.next}
                                    >
                                        ?????????
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
