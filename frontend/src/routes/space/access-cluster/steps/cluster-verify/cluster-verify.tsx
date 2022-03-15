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

import React, { useContext, useEffect, useLayoutEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProCard from '@ant-design/pro-card';
import { Button, message, Row, Space, Steps, Table, Tabs } from 'antd';
import { useHistory, useRouteMatch } from 'react-router';
import TabPane from '@ant-design/pro-card/lib/components/TabPane';
import { isSuccess } from '@src/utils/http';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { DorisNodeTypeEnum } from '@src/routes/space/new-cluster/types/params.type';
import { SpaceAPI } from '@src/routes/space/space.api';
import { useRequest } from 'ahooks';
import { IResult } from '@src/interfaces/http.interface';
import { OperateStatusEnum } from '@src/routes/space/space.data';
import { useRecoilState } from 'recoil';
import { stepDisabledState } from '../../access-cluster.recoil';
const Step = Steps.Step;

export function ClusterVerify(props: any) {
    const [activeKey, setActiveKey] = useState(DorisNodeTypeEnum.FE);
    const {reqInfo} = useContext(NewSpaceInfoContext);
    const match = useRouteMatch<{spaceId: string}>();
    const [instance, setInstance] = useState([]);
    const [nodeTypes, setNodeTypes] = useState<any[]>([]);
    const [feNodes, setFENodes] = useState([]);
    const [beNodes, setBENodes] = useState([]);
    const [brokerNodes, setBrokerNodes] = useState([]);
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);


    const columns = [
        {
            title: '序号',
            dataIndex: 'instanceId',
            key: 'instanceId',
        },
        {
            title: '节点IP',
            dataIndex: 'nodeHost',
            key: 'nodeHost',
        },
        {
            title: '校验结果',
            key: 'operateStatus',
            render: (record: any) => {
                return (
                    <Steps progressDot current={record.operateStage - 1} percent={60} size="small" style={{marginLeft: -50}} status={OperateStatusEnum.getStepStatus(record.operateStatus)}>
                        <Step style={{width: 80}} />
                    </Steps>
                )
            }
        },
    ];
    const getClusterInstance = useRequest<IResult<any>, any>(
        (clusterId: string) => {
            return SpaceAPI.getClusterInstance<any>({clusterId});
        },
        {
            manual: true,
            pollingInterval: 2000,
            onSuccess: (res: any) => {
                if (isSuccess(res)) {
                    const data: any[] = res.data;
                    setInstance(res.data);
                    const types = [];
                    const feNodes = res.data.filter(item => item.moduleName?.toUpperCase() === DorisNodeTypeEnum.FE);
                    const beNodes = res.data.filter(item => item.moduleName?.toUpperCase() === DorisNodeTypeEnum.BE);
                    const brokerNodes = res.data.filter(item => item.moduleName?.toUpperCase() === DorisNodeTypeEnum.BROKER);
                    setFENodes(feNodes);
                    setBENodes(beNodes);
                    setBrokerNodes(brokerNodes);
                    if (feNodes.length > 0) {
                        types.push({key: DorisNodeTypeEnum.FE, tab: 'FE节点', moduleName: DorisNodeTypeEnum.FE });
                    }
                    if (beNodes.length > 0) {
                        types.push({key: DorisNodeTypeEnum.BE, tab: 'BE节点', moduleName: DorisNodeTypeEnum.BE });
                    }
                    if (brokerNodes.length > 0) {
                        types.push({key: DorisNodeTypeEnum.BROKER, tab: 'Broker节点', moduleName: DorisNodeTypeEnum.BROKER });
                    }
                    setNodeTypes(types);
                    const CANCEL_STATUS = [OperateStatusEnum.PROCESSING, OperateStatusEnum.INIT];
                    if (data.filter(item => CANCEL_STATUS.includes(item.operateStatus)).length === 0) {
                        getClusterInstance.cancel();
                    }
                    if (data.filter(item => item.operateStatus !== OperateStatusEnum.SUCCESS).length > 0) {
                        setStepDisabled({...stepDisabled, next: true});
                    } else {
                        setStepDisabled({...stepDisabled, next: false});
                    }
                }
            },
            onError: () => {
                if (reqInfo.cluster_id) {
                    message.error('请求出错');
                    getClusterInstance.cancel();
                }
            },
        },
    );


    useEffect(() => {
        if (reqInfo.cluster_id) {
            console.log(reqInfo.cluster_id)
            getClusterInstance.run(reqInfo.cluster_id);
        }
    }, [reqInfo.cluster_id]);


    return (
        <PageContainer
            header={{
                title: <h2>校验集群</h2>,
            }}
        >
            <Tabs activeKey={activeKey} onChange={(key: any) => setActiveKey(key)} type="card">
                {nodeTypes.map(item => (
                    <TabPane tab={item.tab} key={item.key}>
                    </TabPane>
                ))}
            </Tabs>
            {activeKey === DorisNodeTypeEnum.FE && (
                 <Table columns={columns} dataSource={feNodes} rowKey="instanceId" />
            )}
            {activeKey === DorisNodeTypeEnum.BE && (
                 <Table columns={columns} dataSource={beNodes} rowKey="instanceId" />
            )}
            {activeKey === DorisNodeTypeEnum.BROKER && (
                 <Table columns={columns} dataSource={brokerNodes} rowKey="instanceId" />
            )}
        </PageContainer>
    );
}
