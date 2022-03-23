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
import { Redirect, useRouteMatch, useHistory } from 'react-router';
import CacheRoute, { CacheSwitch } from 'react-router-cache-route';
import { pathToRegexp } from 'path-to-regexp';
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
import { useRecoilState, useRecoilValue } from 'recoil';
import { requestInfoState, stepDisabledState } from './access-cluster.recoil';
import { ClusterVerify } from './steps/cluster-verify/cluster-verify';
import { SpaceAccessFinish } from './steps/finish/finish';
import { checkParam } from '../space.utils';
const { Step } = Steps;

export function AccessCluster(props: any) {
    const match = useRouteMatch<{ requestId: string }>();
    const history = useHistory();
    const [step, setStep] = React.useState(0);
    const [loading, setLoading] = useState(false);
    const [requestInfo, setRequestInfo] = useRecoilState(requestInfoState);
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);
    const hidePrevSteps = [
        AccessClusterStepsEnum['space-register'],
        AccessClusterStepsEnum['node-verify'],
        AccessClusterStepsEnum['cluster-verify'],
        AccessClusterStepsEnum.finish,
    ];

    useEffect(() => {
        if (history.location.pathname === '/space/list') {
            return;
        }
        const regexp = pathToRegexp(`${match.path}/:step`);
        const paths = regexp.exec(history.location.pathname);
        const step = (paths as string[])[2];
        setStep(AccessClusterStepsEnum[step]);

        setStepDisabled({ ...stepDisabled, next: false });

        if (match.params.requestId && +match.params.requestId !== 0) {
            getRequestInfo();
        }
    }, [history.location.pathname]);

    const [form] = useForm();

    async function getRequestInfo() {
        const requestId = match.params.requestId;
        const res = await SpaceAPI.getRequestInfo(requestId);
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
                checkParam(params.spaceInfo.name, '请填写空间名称') &&
                checkParam(params.spaceInfo.spaceAdminUsers, '请填写管理员姓名');
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
                checkParam(params.clusterAccessInfo.address, '请填写集群地址') &&
                checkParam(params.clusterAccessInfo.httpPort, '请填写HTTP端口') &&
                checkParam(params.clusterAccessInfo.queryPort, '请填写JDBC端口') &&
                checkParam(params.clusterAccessInfo.user, '请填写集群用户名');
        }

        if (value && step === AccessClusterStepsEnum['managed-options']) {
            params.authInfo = {
                sshKey: value.sshKey,
                sshPort: value.sshPort,
                sshUser: value.sshUser,
            };
            params.installInfo = value.installInfo;
            isParamsValid =
                checkParam(params.authInfo.sshUser, '请填写SSH用户') &&
                checkParam(params.authInfo.sshPort, '请填写SSH端口') &&
                checkParam(params.authInfo.sshKey, '请填写SSH私钥') && 
                checkParam(params.installInfo, '请填写安装路径')
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
                history.push(`/space/access/${res.data.requestId}/${AccessClusterStepsEnum[newStep]}`);
            }, 0);
        } else {
            message.error(res.msg);
        }
    }

    function prevStep() {
        const newStep = step - 1;
        setStep(newStep);
        setStepDisabled({ ...stepDisabled, prev: false });
        history.push(`/space/access/${requestInfo.requestId}/${AccessClusterStepsEnum[newStep]}`);
    }

    function finish() {
        history.push('/space/list');
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
                            <Step title="连接集群" description="&nbsp;&nbsp;" />
                            <Step title="托管选项" description="&nbsp;&nbsp;" />
                            <Step title="校验主机" description="&nbsp;&nbsp;" />
                            <Step title="校验集群" description="&nbsp;&nbsp;" />
                            <Step title="完成创建" description="&nbsp;&nbsp;" />
                        </Steps>
                    </div>
                    <div style={{ marginRight: 240 }}>
                        <CacheSwitch>
                            <CacheRoute path={`${match.path}/${AccessClusterStepsEnum[0]}`} component={SpaceRegister} />
                            <CacheRoute
                                path={`${match.path}/${AccessClusterStepsEnum[1]}`}
                                component={ConnectCluster}
                            />
                            <CacheRoute
                                path={`${match.path}/${AccessClusterStepsEnum[2]}`}
                                component={ManagedOptions}
                            />
                            <CacheRoute path={`${match.path}/${AccessClusterStepsEnum[3]}`} component={NodeVerify} />
                            <CacheRoute path={`${match.path}/${AccessClusterStepsEnum[4]}`} component={ClusterVerify} />
                            <CacheRoute
                                path={`${match.path}/${AccessClusterStepsEnum[5]}`}
                                component={SpaceAccessFinish}
                            />
                            <Redirect to={`${match.path}/${AccessClusterStepsEnum[0]}`} />
                        </CacheSwitch>
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
                                        上一步
                                    </Button>
                                )}
                                {step === AccessClusterStepsEnum['finish'] ? (
                                    <Button
                                        type="primary"
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
                                        loading={loading}
                                        disabled={stepDisabled.next}
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
