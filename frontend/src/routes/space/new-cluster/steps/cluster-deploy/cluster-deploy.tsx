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

import React, { useContext, useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Table, Tabs, message, Steps } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import TabPane from '@ant-design/pro-card/lib/components/TabPane';
import { DorisNodeTypeEnum } from '../../types/params.type';
import { IResult } from '@src/interfaces/http.interface';
import { SpaceAPI } from '@src/routes/space/space.api';
import { OperateStatusEnum } from '@src/routes/space/space.data';
import { isSuccess } from '@src/utils/http';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { useRecoilState } from 'recoil';
import { stepDisabledState } from '@src/routes/space/access-cluster/access-cluster.recoil';

const { Step } = Steps;

const ERROR_STATUS = [OperateStatusEnum.FAIL, OperateStatusEnum.CANCEL];

const STEP_MAP = {
    GET_INSTALL_PACKAGE: '获取安装包',
    NODE_CONF: '下发节点配置',
    START_UP_NODE: '启动节点',
};

export function ClusterDeploy(props: any) {
    const { reqInfo, step } = useContext(NewSpaceInfoContext);
    const [activeKey, setActiveKey] = useState(DorisNodeTypeEnum.FE);
    const [instances, setInstances] = useState<any[]>([]);
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);
    const [readyLoading, setReadyLoading] = useState(true);
    const getJDBCReady = useRequest<IResult<any>, any>(
        (clusterId: string) => {
            return SpaceAPI.getJDBCReady<any>({ clusterId });
        },
        {
            manual: true,
            pollingInterval: 1000,
            onSuccess: (res: any) => {
                if (isSuccess(res)) {
                    const data: boolean = res.data;
                    if (data) {
                        getJDBCReady.cancel();
                        setReadyLoading(false);
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
    const getClusterInstances = useRequest<IResult<any>, any>(
        (clusterId: string) => {
            return SpaceAPI.getClusterInstance<any>({ clusterId });
        },
        {
            manual: true,
            pollingInterval: 2000,
            onSuccess: (res: any) => {
                if (isSuccess(res)) {
                    const data: any[] = res.data;
                    setInstances(data);
                    const failedInstance = data.find(item => ERROR_STATUS.includes(item.operateStatus));
                    if (failedInstance) {
                        message.error(failedInstance.operateResult);
                        getClusterInstances.cancel();
                    }
                    if (
                        data.every(item => item.operateStatus === OperateStatusEnum.SUCCESS && item.operateStage === 3)
                    ) {
                        getClusterInstances.cancel();
                        getJDBCReady.run(reqInfo.cluster_id);
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

    useEffect(() => {
        if (reqInfo.cluster_id && step === 6) {
            getClusterInstances.run(reqInfo.cluster_id);
        }
        return () => {
            getClusterInstances.cancel();
            getClusterInstances.cancel();
        };
    }, [reqInfo.cluster_id, step]);

    const getStepStatus = (record: any) => {
        const currentStepStatus = OperateStatusEnum.getStepStatus(record.operateStatus);
        if (currentStepStatus === 'error') return 'error';
        return readyLoading ? 'process' : 'finish';
    };

    const columns = [
        {
            title: '序号',
            dataIndex: 'instanceId',
        },
        {
            title: '节点IP',
            dataIndex: 'nodeHost',
        },
        {
            title: '安装进度',
            dataIndex: 'operateStage',
            render: (operateStage: number, record: any) => (
                <Steps
                    progressDot={(iconDot, { status }) => {
                        if (status === 'process') return <LoadingOutlined style={{ color: '#1890ff' }} />;
                        return iconDot;
                    }}
                    status={getStepStatus(record)}
                    current={record.operateStage - 1}
                    size="small"
                    style={{ marginLeft: -50 }}
                >
                    {Object.keys(STEP_MAP).map((stepKey, index) => (
                        <Step key={index} style={{ width: 80 }} title={STEP_MAP[stepKey]} />
                    ))}
                </Steps>
            ),
        },
    ];

    const getTableDataSource = (activeKey: string) => {
        return instances.filter(item => item.moduleName === activeKey.toLowerCase());
    };

    return (
        <PageContainer
            header={{
                title: <h2>部署集群</h2>,
            }}
        >
            <Tabs activeKey={activeKey} onChange={(key: any) => setActiveKey(key)} type="card">
                <TabPane tab="FE节点" key={DorisNodeTypeEnum.FE}></TabPane>
                <TabPane tab="BE节点" key={DorisNodeTypeEnum.BE}></TabPane>
                <TabPane tab="Broker节点" key={DorisNodeTypeEnum.BROKER}></TabPane>
            </Tabs>
            <Table columns={columns} dataSource={getTableDataSource(activeKey)} />
        </PageContainer>
    );
}
