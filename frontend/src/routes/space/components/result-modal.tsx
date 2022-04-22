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

import React, { useEffect, useState } from 'react';
import { Button, Space, Table, Modal, message } from 'antd';
import { fresh, modalState, stepState, processId } from '../new-cluster/recoils/index';
import { useRecoilState, useRecoilValue } from 'recoil';
import API from '../new-cluster/new-cluster.api';
import { isSuccess } from '@src/utils/http';
import * as types from '../new-cluster/types/index.type';
import { NewClusterStepsEnum } from '../new-cluster/new-cluster.data';
import ProCard from '@ant-design/pro-card';
import { DorisNodeTypeEnum } from '../new-cluster/types/index.type';
import { useNavigate } from 'react-router';

export function ResultModal(props: any) {
    const navigate = useNavigate();
    // const nodeStatus = useRecoilValue(nodeStatusQuery);
    const [modal, setModal] = useRecoilState(modalState);
    const [refresh, setFresh] = useRecoilState(fresh);
    const [step, setStep] = useRecoilState(stepState);
    const processID = useRecoilValue(processId);
    const [data, setData] = useState<types.ItaskResult[]>([]);
    const [loading, setloading] = useState(false);
    const columns = [
        {
            title: '序号',
            dataIndex: 'id',
            key: 'id',
        },
        {
            title: '节点IP',
            dataIndex: 'host',
            key: 'host',
        },
        {
            title: '状态信息',
            dataIndex: 'status',
            key: 'status',
            render: (_, record: any) => (
                <Space>
                    <span>
                        {record.status} {record.result}
                    </span>
                </Space>
            ),
        },
        {
            title: '操作',
            key: 'option',
            render: (_, record: any) => (
                <Space>
                    <Button
                        type="link"
                        disabled={record.status !== types.taskStatusEnum.FAILURE}
                        onClick={() => reTry(record)}
                    >
                        重试
                    </Button>
                    {/* <Button type="link" disabled={record.status !== types.taskStatusEnum.FAILURE} onClick={() => doSkip(record)}>跳过</Button>
                    <Button type="link" onClick={() => viewLog(record)}>查看日志</Button> */}
                </Space>
            ),
        },
    ];

    const handleOk = () => {
        setStep(step + 1);
        setModal({
            ...modal,
            visible: false,
        });
        console.log(NewClusterStepsEnum[step + 1]);
        // window.location.reload()  //todo
        navigate(`/cluster/new/${NewClusterStepsEnum[step + 1]}`);
    };
    const handleCancel = () => {
        setModal({
            ...modal,
            visible: false,
        });
    };

    const reTry = (record: any) => {
        API.reTryTask(record.id).then(res => {
            if (isSuccess(res)) {
                message.success(res.msg);
                setFresh(refresh + 1);
                setData([]);
                fetchNode();
            } else {
                message.warn(res.msg);
            }
        });
    };

    const fetchNode = () => {
        setloading(true);
        API.getTaskStatus(processID).then(res => {
            setData(res.data);
            setloading(false);
        });
    };

    useEffect(() => {
        fetchNode();
    }, [modal.visible]);

    useEffect(() => {
        let timer: any = null;
        console.log('00000');
        const len = data.filter(
            item => item.status === types.taskStatusEnum.SUCCESS || item.status === types.taskStatusEnum.FAILURE,
        ).length;
        if (modal.visible && (len < data.length || data.length === 0)) {
            timer = window.setTimeout(() => {
                setFresh(refresh + 1);
                fetchNode();
            }, 5000);
        }
        return () => {
            window.clearTimeout(timer);
        };
    }, [refresh, modal.visible]);

    return (
        <Modal
            title="节点状态"
            visible={modal.visible}
            onOk={handleOk}
            onCancel={handleCancel}
            width={'80%'}
            maskClosable={false}
            footer={[
                <Button key="back" onClick={handleCancel}>
                    取消
                </Button>,
                <Button
                    key="submit"
                    type="primary"
                    onClick={handleOk}
                    disabled={data.filter(item => item.status === types.taskStatusEnum.SUCCESS).length !== data.length}
                >
                    下一步
                </Button>,
                // <Button key="submit" type="primary" onClick={handleOk}>下一步</Button>
            ]}
        >
            {step === 0 ? (
                <Table columns={columns} dataSource={data} rowKey="id" key={1} loading={loading} />
            ) : (
                <>
                    <ProCard bordered title={'FE节点'}>
                        <Table
                            columns={columns}
                            dataSource={data.filter(item => item.taskRole === DorisNodeTypeEnum.FE)}
                            rowKey="id"
                            pagination={false}
                            loading={loading}
                        />
                    </ProCard>
                    <ProCard bordered title={'BE节点'}>
                        <Table
                            columns={columns}
                            dataSource={data.filter(item => item.taskRole === DorisNodeTypeEnum.BE)}
                            rowKey="id"
                            pagination={false}
                            loading={loading}
                        />
                    </ProCard>
                    <ProCard bordered title={'BROKER节点'}>
                        <Table
                            columns={columns}
                            dataSource={data.filter(item => item.taskRole === DorisNodeTypeEnum.BROKER)}
                            rowKey="id"
                            pagination={false}
                            loading={loading}
                        />
                    </ProCard>
                </>
            )}
        </Modal>
    );
}
