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

import React from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProCard from '@ant-design/pro-card';
import { Button, Row, Space, Table } from 'antd';
import { useHistory } from 'react-router';

export function NodeRegister(props: any) {
    const history = useHistory();
    const columns = [
        {
            title: '序号',
            dataIndex: 'order',
            key: 'order',
        },
        {
            title: '节点IP',
            dataIndex: 'ip',
            key: 'ip',
        },
        {
            title: '注册状态',
            dataIndex: 'register_status',
            key: 'register_status',
        },
        {
            title: '操作',
            key: 'option',
            render: () => (
                <Space>
                    <Button>重试</Button>
                    <Button>跳过</Button>
                </Space>
            ),
        },
    ];
    return (
        <PageContainer
            header={{
                title: '节点列表',
            }}
        >
            <Table columns={columns} />
        </PageContainer>
    );
}
