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
import { Button, Modal, Table } from 'antd';
import { useTranslation } from 'react-i18next';
import { ConfigurationItem } from '.';
import { transformHostToIp } from '../cluster.utils';

interface CheckModalProps {
    visible: boolean;
    currentParameter: ConfigurationItem;
    onCancel: () => void;
}

export default function CheckModal(props: CheckModalProps) {
    const { t } = useTranslation();
    const { visible, currentParameter, onCancel } = props;
    const nodeList = currentParameter?.nodes?.map((node, index) => ({
        host: node,
        value: currentParameter.values[index],
    }));

    const columns = [
        {
            title: t`hostIp`,
            dataIndex: 'host',
            render: (host: string) => transformHostToIp(host),
        },
        {
            title: t`currentValue`,
            dataIndex: 'value',
        },
    ];
    return (
        <Modal
            title={currentParameter.name}
            visible={visible}
            footer={<Button onClick={onCancel}>{t`cancel`}</Button>}
            width={700}
            onCancel={onCancel}
        >
            <Table rowKey="host" dataSource={nodeList} columns={columns} pagination={false} />
        </Modal>
    );
}
