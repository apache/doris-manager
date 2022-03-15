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

import React, { useContext, useEffect } from 'react';
import { message, Table } from 'antd';
import { useTranslation } from 'react-i18next';
import StatusMark from '@src/components/status-mark';
import { useAsync } from '@src/hooks/use-async';
import * as ClusterAPI from '../cluster.api';
import { UserInfoContext } from '@src/common/common.context';

const enum ModuleNameEnum {
    FRONTEND = 'fe',
    BACKEND = 'be',
    BROKER = 'broker',
}

const enum OperateStatusEnum {
    SUCCESS = 'SUCCESS',
    INIT = 'INIT',
    PROCESSING = 'PROCESSING',
    FAIL = 'FAIL',
    CANCEL = 'CANCEL',
}

interface NodeListItem {
    instanceId: number;
    moduleName: ModuleNameEnum;
    nodeHost: string;
    operateStatus: OperateStatusEnum;
}

export default function Nodes() {
    const { t } = useTranslation();
    const userInfo = useContext(UserInfoContext)!;
    const { data: nodeList, loading, run: runGetNodeList } = useAsync<NodeListItem[]>({ loading: true, data: [] });
    useEffect(() => {
        runGetNodeList(ClusterAPI.getNodeList(userInfo.space_id), { setStartLoading: false }).catch(res => {
            message.error(res.msg);
        });
    }, [runGetNodeList, userInfo.space_id]);
    const columns = [
        {
            title: t`nodeId`,
            dataIndex: 'instanceId',
        },
        {
            title: t`nodeType`,
            dataIndex: 'moduleName',
            filters: [
                {
                    text: 'Frontend',
                    value: ModuleNameEnum.FRONTEND,
                },
                {
                    text: 'Backend',
                    value: ModuleNameEnum.BACKEND,
                },
                {
                    text: 'Broker',
                    value: ModuleNameEnum.BROKER,
                },
            ],
            onFilter: (value: any, record: NodeListItem) => record.moduleName === value,
            render: (moduleName: ModuleNameEnum) => <span>{resolveModuleName(moduleName)}</span>,
        },
        {
            title: t`hostIp`,
            dataIndex: 'nodeHost',
        },
        {
            title: t`nodeStatus`,
            dataIndex: 'operateStatus',
            render: (status: OperateStatusEnum) => (
                <StatusMark status={status === OperateStatusEnum.SUCCESS ? 'success' : 'error'}>
                    {status === OperateStatusEnum.SUCCESS ? t`normal` : t`abnormal`}
                </StatusMark>
            ),
        },
    ];

    const resolveModuleName = (moduleName: ModuleNameEnum) => {
        switch (moduleName) {
            case ModuleNameEnum.FRONTEND:
                return 'Frontend';
            case ModuleNameEnum.BACKEND:
                return 'Backend';
            case ModuleNameEnum.BROKER:
                return 'Broker';
            default:
                return '';
        }
    };

    return <Table rowKey="instanceId" loading={loading} columns={columns} dataSource={nodeList} />;
}
