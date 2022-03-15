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

import { Button, PageHeader, Row, Space, Table } from 'antd';
import React from 'react';
import { useHistory, useRouteMatch } from 'react-router';

export function ClusterList(props: any) {
    const history = useHistory();
    const match = useRouteMatch();
    const columns = [
        {
            title: '集群',
            dataIndex: 'cluster',
            key: 'cluster',
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
        },
        {
            title: '链接信息',
            dataIndex: 'connect_info',
            key: 'connect_info',
        },
    ];
    const [clusterList, setClusterList] = React.useState([]);
    return (
        <div style={{ padding: '0 20px', backgroundColor: 'white', marginTop: 20, minHeight: 'calc(100% - 60px)' }}>
            <PageHeader className="site-page-header" title="集群列表" style={{ paddingLeft: 0 }} />
            <Row justify="end" style={{marginBottom: 20}}>
                <Space>
                    <Button type="primary" onClick={() => history.push(`/cluster/new`)}>新建集群</Button>
                    <Button type="primary">接入集群</Button>
                </Space>
            </Row>
            <Table
                columns={columns}
                dataSource={clusterList}
                rowKey={(record: any[]) => record[Object.keys(record).length - 1]}
                size="middle"
            />
        </div>
    );
}
