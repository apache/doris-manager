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

import React, { useState } from 'react';
import { Modal, Form, Input, Radio, Select, message } from 'antd';
import { useTranslation } from 'react-i18next';
import { ConfigurationItem } from '.';
import { useAsync } from '@src/hooks/use-async';
import * as ClusterAPI from '../cluster.api';
import { transformHostToIp } from '../cluster.utils';

interface EditModalProps {
    visible: boolean;
    currentParameter: ConfigurationItem;
    onOk: () => void;
    onCancel: () => void;
}

interface FormInstanceProps {
    value: string;
    persist: boolean;
    range: 'all' | 'part';
    nodes: string[];
}

const enum RangeEnum {
    ALL = 'ALL',
    PART = 'PART',
}

export default function EditModal(props: EditModalProps) {
    const { t } = useTranslation();
    const { visible, currentParameter, onOk, onCancel } = props;
    const [range, setRange] = useState<RangeEnum>(RangeEnum.ALL);
    const { loading: confirmLoading, run: runChangeConfiguration } = useAsync();
    const [form] = Form.useForm<FormInstanceProps>();

    const handleOk = () => {
        form.validateFields()
            .then(values => {
                runChangeConfiguration(
                    ClusterAPI.changeConfiguration(currentParameter.type === 'Frontend' ? 'fe' : 'be', {
                        [currentParameter.name]: {
                            node: [...(range === RangeEnum.ALL ? currentParameter.nodes : values.nodes)],
                            value: values.value,
                            persist: values.persist ? 'true' : 'false',
                        },
                    }),
                )
                    .then(() => {
                        message.success('编辑成功');
                        onOk();
                        handleCancel();
                    })
                    .catch(res => {
                        message.error(res.msg);
                    });
            })
            .catch(console.log);
    };

    const handleCancel = () => {
        form.resetFields();
        setRange(RangeEnum.ALL);
        onCancel();
    };

    return (
        <Modal
            destroyOnClose
            title={currentParameter.name}
            confirmLoading={confirmLoading}
            visible={visible}
            onCancel={handleCancel}
            onOk={handleOk}
        >
            <Form form={form} autoComplete="off" labelCol={{ span: 5 }}>
                <Form.Item
                    name="value"
                    label={t`confValue`}
                    rules={[{ required: true, message: t`confValuePlaceholder` }]}
                >
                    <Input placeholder={t`confValuePlaceholder`} style={{ width: 300 }} />
                </Form.Item>
                <Form.Item
                    name="persist"
                    label={t`effectiveWay`}
                    rules={[{ required: true, message: t`effectiveWayRequiredMessage` }]}
                >
                    <Radio.Group>
                        <Radio value={true}>{t`permanentEffective`}</Radio>
                        <Radio value={false}>{t`onceEffective`}</Radio>
                    </Radio.Group>
                </Form.Item>
                <Form.Item
                    name="range"
                    label={t`effectiveRange`}
                    rules={[{ required: true, message: t`effectiveRangeRequiredMessage` }]}
                >
                    <Radio.Group
                        onChange={e => {
                            setRange(e.target.value);
                        }}
                    >
                        <Radio value={RangeEnum.ALL}>{t`allNodes`}</Radio>
                        <Radio value={RangeEnum.PART}>{t`certainNodes`}</Radio>
                    </Radio.Group>
                </Form.Item>
                {range === RangeEnum.PART && (
                    <Form.Item
                        name="nodes"
                        label={t`effectiveNodes`}
                        rules={[{ required: true, message: t`effectiveNodesPlaceholder` }]}
                    >
                        <Select mode="multiple" placeholder={t`effectiveNodesPlaceholder`}>
                            {currentParameter.nodes.map(nodeHost => (
                                <Select.Option key={nodeHost} value={nodeHost}>
                                    {transformHostToIp(nodeHost)}
                                </Select.Option>
                            ))}
                        </Select>
                    </Form.Item>
                )}
            </Form>
        </Modal>
    );
}
