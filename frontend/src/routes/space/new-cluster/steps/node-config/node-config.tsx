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

import React, { useState, useEffect, useReducer, useContext, useCallback } from 'react';
import { Table, Input, Button, Popconfirm, Form, Row, Space, Switch, Tabs, message } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { PageContainer } from '@ant-design/pro-layout';
import { CustomConfig } from './components/custom-config';
import ProCard from '@ant-design/pro-card';
import type { ProColumns } from '@ant-design/pro-table';
import { EditableProTable } from '@ant-design/pro-table';
import { useHistory } from 'react-router';
import { BASE_BE_CONFIG, BASE_FE_CONFIG, BASE_BROKER_CONFIG } from './data';
import * as types from './../../types/index.type';
import API from './../../new-cluster.api';
import { processId, roleListQuery, stepState } from './../../recoils/index';
import { useRecoilState, useRecoilValue } from 'recoil';
import { isSuccess } from '@src/utils/http';
import { DorisNodeTypeEnum } from './../../types/index.type';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { useAsync } from '@src/hooks/use-async';
import { SpaceAPI } from '@src/routes/space/space.api';
const { TabPane } = Tabs;

interface DataType {
    key: string;
    value: any;
    desc: string | null;
}

export interface Custom {
    value: string;
    visible: boolean;
}

interface CustomState {
    feCustom: Custom;
    beCustom: Custom;
    brokerCustom: Custom;
}

const initData: CustomState = {
    feCustom: {
        value: '',
        visible: false,
    },
    beCustom: {
        value: '',
        visible: false,
    },
    brokerCustom: {
        value: '',
        visible: false,
    },
};
const reducer = (state: CustomState, action: { type: DorisNodeTypeEnum; payload: Custom }) => {
    switch (action.type) {
        case DorisNodeTypeEnum.FE:
            return {
                ...state,
                feCustom: action.payload,
            };
        case DorisNodeTypeEnum.BE:
            return {
                ...state,
                beCustom: action.payload,
            };
        case DorisNodeTypeEnum.BROKER:
            return {
                ...state,
                brokerCustom: action.payload,
            };
        default:
            return state;
    }
};

function getDeployConfigs(name: string, modules: any[]) {
    return modules.find(module => module.name === name)?.config || [];
}

function tranverseModulesToObject(modules: DataType[]) {
    return modules.reduce((memo, current) => {
        memo[current.key] = {
            value: current.value,
            desc: current.desc,
        };
        return memo;
    }, {} as Record<string, Omit<DataType, 'key'>>);
}

function tranverseStringToObject(customString: string) {
    if (customString === '') return {};
    const customModules = customString.split('\n');
    return customModules.reduce((memo, current) => {
        const [key, value] = current.split('=');
        memo[key] = {
            value: value,
            desc: null,
        };
        return memo;
    }, {} as Record<string, Omit<DataType, 'key'>>);
}

function tranverseObjectToModules(moduleObject: Record<string, Omit<DataType, 'key'>>) {
    return Object.keys(moduleObject).map(moduleKey => ({
        key: moduleKey,
        value: moduleObject[moduleKey].value == null ? null : moduleObject[moduleKey].value,
        desc: moduleObject[moduleKey].desc == null ? null : moduleObject[moduleKey].desc,
    }));
}

function getNewConfigsWithCustom(modules: DataType[], customString: string) {
    const moduleObject = tranverseModulesToObject(modules);
    const customModuleObject = tranverseStringToObject(customString);
    return tranverseObjectToModules({
        ...moduleObject,
        ...customModuleObject,
    });
}

export function NodeConfig() {
    const { form } = useContext(NewSpaceInfoContext);
    return (
        <Form form={form} name="basic" labelCol={{ span: 2 }} wrapperCol={{ span: 10 }} autoComplete="off">
            <Form.Item name="deployConfigs" style={{ width: '100%' }} wrapperCol={{ span: 24 }}>
                <NodeConfigContent />
            </Form.Item>
        </Form>
    );
}

export function NodeConfigContent(props: any) {
    const { reqInfo } = useContext(NewSpaceInfoContext);
    const history = useHistory();
    const [step, setStep] = useRecoilState(stepState);
    const [customState, dispatch] = useReducer(reducer, initData);
    const [activeKey, setActiveKey] = useState(DorisNodeTypeEnum.FE);
    const { data: clusterModules, run: runGetClusterModules, loading } = useAsync<any[]>({ data: [], loading: true });
    const columns: ProColumns<DataType>[] = [
        {
            title: '配置项',
            dataIndex: 'key',
            editable: false,
        },
        {
            title: '默认值',
            dataIndex: 'value',
            formItemProps: {
                rules: [
                    {
                        required: true,
                        message: '此项为必填项',
                    },
                ],
            },
            width: '30%',
        },
        {
            title: '说明',
            dataIndex: 'desc',
            editable: false,
            render: (desc: any, record: DataType) => {
                return <span>{record.desc == null ? '待补充' : record.desc}</span>;
            },
        },
    ];
    const [feDataSource, setFeDataSource] = useState<DataType[]>([]);
    const [beDataSource, setBeDataSource] = useState<DataType[]>([]);
    const [brokerDataSource, setBrokerDataSource] = useState<DataType[]>([]);

    useEffect(() => {
        if (!reqInfo.cluster_id) return;
        runGetClusterModules(
            SpaceAPI.getClusterModule({ clusterId: reqInfo.cluster_id }).then(res => {
                if (isSuccess(res)) return res.data;
                return Promise.reject(res);
            }),
        )
            .then(res => {
                setFeDataSource(getDeployConfigs('fe', res));
                setBeDataSource(getDeployConfigs('be', res));
                setBrokerDataSource(getDeployConfigs('broker', res));
            })
            .catch(res => {
                message.error(res.msg);
            });
    }, [runGetClusterModules, reqInfo.cluster_id]);

    const getNewConfigs = useCallback(
        (moduleName: string) => {
            const { feCustom, beCustom, brokerCustom } = customState;
            switch (moduleName) {
                case 'fe':
                    return getNewConfigsWithCustom(feDataSource, feCustom.visible ? feCustom.value : '');
                case 'be':
                    return getNewConfigsWithCustom(beDataSource, beCustom.visible ? beCustom.value : '');
                case 'broker':
                    return getNewConfigsWithCustom(brokerDataSource, brokerCustom.visible ? brokerCustom.value : '');
            }
        },
        [feDataSource, beDataSource, brokerDataSource, customState],
    );

    useEffect(() => {
        const newClusterModules = clusterModules!.map(module => ({
            moduleName: module.name,
            configs: getNewConfigs(module.name),
        }));
        props.onChange?.(newClusterModules);
    }, [getNewConfigs]);

    return (
        <>
            <PageContainer
                header={{
                    title: <h2>配置参数</h2>,
                }}
            >
                <Tabs activeKey={activeKey} onChange={(key: any) => setActiveKey(key)} type="card">
                    <TabPane tab="FE节点" key={DorisNodeTypeEnum.FE}></TabPane>
                    <TabPane tab="BE节点" key={DorisNodeTypeEnum.BE}></TabPane>
                    <TabPane tab="Broker节点" key={DorisNodeTypeEnum.BROKER}></TabPane>
                </Tabs>
                {activeKey === DorisNodeTypeEnum.FE && (
                    <>
                        <EditableProTable<DataType>
                            rowKey="key"
                            maxLength={5}
                            bordered
                            columns={columns}
                            value={feDataSource}
                            loading={loading}
                            editable={{
                                type: 'multiple',
                                editableKeys: feDataSource.map(item => item.key),
                                onValuesChange: (record, recordList) => {
                                    setFeDataSource(recordList);
                                },
                            }}
                        />
                        <CustomConfig dispatch={dispatch} activeKey={activeKey} custom={customState.feCustom} />
                    </>
                )}
                {activeKey === DorisNodeTypeEnum.BE && (
                    <>
                        <EditableProTable<DataType>
                            rowKey="key"
                            maxLength={5}
                            bordered
                            columns={columns}
                            value={beDataSource}
                            loading={loading}
                            editable={{
                                type: 'multiple',
                                editableKeys: beDataSource.map(item => item.key),
                                onValuesChange: (record, recordList) => {
                                    setBeDataSource(recordList);
                                },
                            }}
                        />
                        <CustomConfig dispatch={dispatch} activeKey={activeKey} custom={customState.beCustom} />
                    </>
                )}
                {activeKey === DorisNodeTypeEnum.BROKER && (
                    <>
                        <EditableProTable<DataType>
                            rowKey="key"
                            maxLength={5}
                            bordered
                            columns={columns}
                            value={brokerDataSource}
                            recordCreatorProps={false}
                            loading={loading}
                            editable={{
                                type: 'multiple',
                                editableKeys: brokerDataSource.map(item => item.key),
                                onValuesChange: (record, recordList) => {
                                    setBrokerDataSource(recordList);
                                },
                            }}
                        />
                        <CustomConfig dispatch={dispatch} activeKey={activeKey} custom={customState.brokerCustom} />
                    </>
                )}
            </PageContainer>
        </>
    );
}
