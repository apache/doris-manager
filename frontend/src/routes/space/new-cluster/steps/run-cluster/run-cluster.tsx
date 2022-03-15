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
import { Button, message, Row, Space, Steps, Table } from 'antd';
import React, {useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { processId, roleListQuery, stepState } from './../../recoils/index';
import { useRecoilState, useRecoilValue } from 'recoil';
import API from '../../new-cluster.api';
import * as types from '../../types/index.type'
import { isSuccess } from '@src/utils/http';
import { NewClusterStepsEnum } from '../../new-cluster.data';
import { DorisNodeTypeEnum } from '../../types/index.type';

export function RunCluster(props: any) {
    const history = useHistory();
    const processID = useRecoilValue(processId)
    const {fe, be} = useRecoilValue(roleListQuery);
    const [taskInfo, setTaskInfo] = useState<types.ItaskResult[]>([])
    const [step, setStep] = useRecoilState(stepState);
    const [completed, setCompleted] = useState(false);

    useEffect(() => {
        const feArr = fe.map(item => ({ host: item, role: DorisNodeTypeEnum.FE}))
        const beArr = be.map(item => ({ host: item, role: DorisNodeTypeEnum.BE}))
        API.startService({
            processId: processID,
            dorisStarts: [...feArr, ...beArr]
        }).then(res => {
            if(isSuccess(res)){
                API.getTaskStatus(processID).then(cur => {
                    setTaskInfo(cur.data)
                })
            }
        })
    }, [fe,be])

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
            render: (_, record:any) => (
                <Space>
                    <span>{record.status} {record.result}</span>
                </Space>
            ),
        },
        {
            title: '操作',
            key: 'option',
            render: (_, record:any) => (
                <Space>
                    <Button type="link" disabled={record.status !== types.taskStatusEnum.FAILURE} onClick={() => reTry(record)}>重试</Button>
                    <Button type="link" disabled={record.status !== types.taskStatusEnum.FAILURE} onClick={() => doSkip(record)}>跳过</Button>
                    <Button type="link" onClick={() => viewLog(record)}>查看日志</Button>
                </Space>
            ),
        },
    ];

    const reTry = (record: any) => {
        API.reTryTask(record.id).then(res => {
            if(isSuccess(res)){
                message.success(res.msg)
            }else{
                message.warn(res.msg)
            }
        })
    }
    const doSkip = (record: any) => {
        API.skipTask(record.id).then(res => {
            if(isSuccess(res)){
                message.success(res.msg)
            }else{
                message.warn(res.msg)
            }
        })
    }
    const viewLog = (record: any) => {
        history.push({
            pathname: `/cluster/logs/${record.id}`
        })
    }

    const startCluster = () => {
        API.getTaskStatus(processID).then(cur => {
            if(!isSuccess(cur)){
                message.error(cur.msg)
                return;
            }
            let JOIN_BE = cur.data.filter((item: any) => item.taskType === "JOIN_BE")
            if(JOIN_BE?.length){
                message.info('已经组建过了哦')
                return;
            }
            setTaskInfo(cur.data)
            let len = cur.data.filter((item: any) => item.status !== "SUCCESS")
            if(len?.length > 0){
                message.info('部分节点还未安装成功,请稍后重试!')
            }else{
                 API.startCluster({
                    processId: processID,
                    feHosts: fe,
                    beHosts: be
                 }).then(res => {
                    message.success(res.msg)
                    if(res.code === 0){
                        setCompleted(true)
                    }
                 })
            }
        })
       
    }

    return (
        <>
            <ProCard bordered title={'启动FE节点'}>
                <Table columns={columns} dataSource={taskInfo.filter(item => (item.taskRole === DorisNodeTypeEnum.FE))} rowKey="id" pagination={false}/>
            </ProCard>
            <ProCard bordered title={'启动BE节点'}>
                <Table columns={columns} dataSource={taskInfo.filter(item => (item.taskRole === DorisNodeTypeEnum.BE))} rowKey="id" pagination={false}/>
            </ProCard>
            <ProCard bordered title={'启动BROKER节点'}>
                <Table columns={columns} dataSource={taskInfo.filter(item => (item.taskRole === DorisNodeTypeEnum.BROKER))} rowKey="id" pagination={false}/>
            </ProCard>
            <ProCard bordered title={'组建集群'} 
                extra={<Button type="primary" onClick={startCluster} disabled={completed}>开始组建</Button>}>
            </ProCard>
        </>
    );
}
