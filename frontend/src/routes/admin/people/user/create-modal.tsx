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
import { Modal, Form, Select, message, Row } from 'antd';
import { useTranslation } from 'react-i18next';
import { IUser } from './user';
import { useAsync } from '@src/hooks/use-async';
import { addMemberToSpaceAPI } from './user.api';
import commonStyles from '../people.less';

const { Option } = Select;

interface CreateModalProps {
    visible: boolean;
    users: IUser[];
    onCancel: () => void;
    getSpaceMembers: () => void;
}

interface FormInstanceProps {
    users: number[];
}

export default function CreateModal(props: CreateModalProps) {
    const { t } = useTranslation();
    const [form] = Form.useForm<FormInstanceProps>();
    const { loading: confirmLoading, run: runAddMembers } = useAsync();
    const { visible, users, onCancel, getSpaceMembers } = props;

    const handleOk = () => {
        form.validateFields()
            .then(values => {
                runAddMembers(Promise.all(values.users.map(userId => addMemberToSpaceAPI(userId))))
                    .then(() => {
                        message.success(t`addSuccess`);
                        form.resetFields();
                        onCancel();
                    })
                    .catch(() => {
                        message.error(t`addFailed`);
                    })
                    .finally(() => {
                        getSpaceMembers();
                    });
            })
            .catch(console.log);
    };

    return (
        <Modal
            visible={visible}
            destroyOnClose
            title={t`addMembers`}
            onCancel={onCancel}
            onOk={handleOk}
            confirmLoading={confirmLoading}
            maskClosable={false}
        >
            <Form form={form}>
                <Form.Item name="users" label={t`users`} rules={[{ required: true, message: t`pleaseSelectUsers` }]}>
                    <Select
                        mode="multiple"
                        placeholder={t`pleaseSelectUsers`}
                        optionFilterProp="title"
                        optionLabelProp="title"
                    >
                        {users.map(user => (
                            <Option key={user.id} value={user.id} title={user.name}>
                                <Row justify="space-between">
                                    <div className={commonStyles.optionContent}>
                                        <strong>{user.name}</strong>
                                        <span>{user.email ? `(${user.email})` : ''}</span>
                                    </div>
                                </Row>
                            </Option>
                        ))}
                    </Select>
                </Form.Item>
            </Form>
        </Modal>
    );
}
