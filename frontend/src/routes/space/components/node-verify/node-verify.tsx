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

import React, { useContext, useLayoutEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Button, message, Steps, Table } from 'antd';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { isSuccess } from '@src/utils/http';
import { SpaceAPI } from '../../space.api';
import { NodeVerifyStepEnum } from './node-verify.data';
import { useRequest } from 'ahooks';
import { IResult } from '@src/interfaces/http.interface';
import { OperateStatusEnum } from '../../space.data';
import { useRecoilState } from 'recoil';
import { stepDisabledState } from '../../access-cluster/access-cluster.recoil';
import { LoadingOutlined } from '@ant-design/icons';
const Step = Steps.Step;

const ERROR_STATUS = [OperateStatusEnum.FAIL, OperateStatusEnum.CANCEL];
const PENDING_STATUS = [OperateStatusEnum.PROCESSING, OperateStatusEnum.INIT];

export function NodeVerify(props: any) {
    const { reqInfo } = useContext(NewSpaceInfoContext);
    const [nodes, setNodes] = useState<any[]>([]);
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);

    const getClusterNodes = useRequest<IResult<any>, any>(
        (clusterId: string) => {
            return SpaceAPI.getClusterNodes<any>({ clusterId });
        },
        {
            manual: true,
            pollingInterval: 2000,
            onSuccess: (res: any) => {
                if (isSuccess(res)) {
                    const data: any[] = res.data;
                    setNodes(data);
                    const failedNode = data.find(item => ERROR_STATUS.includes(item.operateStatus));
                    if (failedNode) {
                        message.error(failedNode.operateResult);
                    }
                    if (data.filter(item => PENDING_STATUS.includes(item.operateStatus)).length === 0) {
                        getClusterNodes.cancel();
                    }
                    if (data.filter(item => item.operateStatus !== OperateStatusEnum.SUCCESS).length > 0) {
                        setStepDisabled({ ...stepDisabled, next: true });
                    } else {
                        setStepDisabled({ ...stepDisabled, next: false });
                    }
                }
            },
            onError: () => {
                if (reqInfo.cluster_id) {
                    message.error('请求出错');
                }
            },
        },
    );

    useLayoutEffect(() => {
        if (reqInfo.cluster_id) {
            getClusterNodes.run(reqInfo.cluster_id);
        }
    }, [reqInfo.cluster_id]);

    const columns = [
        {
            title: '序号',
            dataIndex: 'nodeId',
            key: 'nodeId',
        },
        {
            title: '节点IP',
            dataIndex: 'host',
            key: 'host',
        },
        {
            title: '校验进度',
            key: 'operateStage',
            render: (record: any) => {
                return (
                    <Steps
                        progressDot={(iconDot, { status }) => {
                            if (status === 'process') return <LoadingOutlined style={{ color: '#1890ff' }} />;
                            return iconDot;
                        }}
                        status={OperateStatusEnum.getStepStatus(record.operateStatus)}
                        current={record.operateStage - 1}
                        size="small"
                        style={{ marginLeft: -50 }}
                    >
                        <Step
                            style={{ width: 80 }}
                            title={NodeVerifyStepEnum.getTitle(NodeVerifyStepEnum.ACCESS_AUTH)}
                        />
                        <Step
                            style={{ width: 80 }}
                            title={NodeVerifyStepEnum.getTitle(NodeVerifyStepEnum.INSTALL_DIR_CHECK)}
                        />
                        <Step style={{ width: 80 }} title={NodeVerifyStepEnum.getTitle(NodeVerifyStepEnum.JDK_CHECK)} />
                        <Step
                            style={{ width: 80 }}
                            title={NodeVerifyStepEnum.getTitle(NodeVerifyStepEnum.AGENT_DEPLOY)}
                        />
                        <Step
                            style={{ width: 80 }}
                            title={NodeVerifyStepEnum.getTitle(NodeVerifyStepEnum.AGENT_START)}
                        />
                        <Step
                            style={{ width: 80 }}
                            title={NodeVerifyStepEnum.getTitle(NodeVerifyStepEnum.AGENT_REGISTER)}
                        />
                    </Steps>
                );
            },
        },
    ];
    return (
        <PageContainer
            header={{
                title: <h2>校验主机</h2>,
            }}
        >
            <Table columns={columns} dataSource={nodes} rowKey="nodeId" />
        </PageContainer>
    );
}
