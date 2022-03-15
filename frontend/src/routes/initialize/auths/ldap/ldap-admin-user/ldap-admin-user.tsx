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

import { InitializeAPI } from '@src/routes/initialize/initialize.api';
import { isSuccess } from '@src/utils/http';
import { Form, Button, message, Select, Space } from 'antd';
import React from 'react';
import { RouteProps, useHistory } from 'react-router';
import { useLDAPUsers } from './use-ldap-user.hooks';
const { Option } = Select;

interface AdminUserProps extends RouteProps {}

export function LDAPAdminUser(props: AdminUserProps) {
    const history = useHistory();
    const { ldapUsers } = useLDAPUsers();
    async function onFinish(values: any) {
        const { username } = values;
        const res = await InitializeAPI.setAdmin({ name: username });
        if (isSuccess(res)) {
            history.push('/initialize/auth/ldap/finish');
        } else {
            message.error(res.msg);
        }
    }

    return (
        <div>
            <Form name="basic" layout="vertical" onFinish={onFinish} autoComplete="off">
                <Form.Item
                    label="选择超级管理员"
                    tooltip="用于访问Palo Studio最高级管理员权限"
                    name="username"
                    rules={[{ required: true, message: '请选择超级管理员' }]}
                >
                    <Select showSearch={true} placeholder="请选择超级管理员" allowClear optionFilterProp="filter" optionLabelProp="title">
                        {ldapUsers?.map(user => (
                            <Option
                                key={user.id}
                                value={user.id}
                                title={user.name}
                                filter={user.name}
                            >
                                <Space>
                                    <strong>{user.name}</strong>
                                    {user.email && <span>({user.email})</span>}
                                </Space>
                            </Option>
                        ))}
                    </Select>
                </Form.Item>

                <Form.Item>
                    <Button type="primary" htmlType="submit">
                        保存
                    </Button>
                </Form.Item>
            </Form>
        </div>
    );
}
