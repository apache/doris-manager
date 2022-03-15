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

import React, { useEffect } from 'react';
import { Form, Input, message, Radio } from 'antd';
import { useAsync } from '@src/hooks/use-async';
import LoadingLayout from '../../components/loading-layout';
import { LOADING_WRAPPER_STYLE } from '../../constants';
import { getLdapSettingsApi } from '../../global.api';

export default function Certificate() {
    const [form] = Form.useForm();
    const { data, loading, run } = useAsync({ loading: true, data: {} });
    useEffect(() => {
        run(getLdapSettingsApi()).catch(() => {
            message.error('获取ldap设置失败');
        });
    }, [run]);
    useEffect(() => {
        form.setFieldsValue(data);
    }, [data]);
    return (
        <LoadingLayout loading={loading} wrapperStyle={LOADING_WRAPPER_STYLE}>
            <Form name="ldap_server" form={form} layout="vertical" autoComplete="off">
                <h2>服务器</h2>
                <Form.Item label="LDAP主机" tooltip="服务器主机名" name="ldapHost">
                    <Input placeholder="请输入LDAP服务器主机名" readOnly />
                </Form.Item>
                <Form.Item name="ldapPort" label="LDAP端口" tooltip="服务器端口，如果使用SSL,端口号通常为389或636">
                    <Input placeholder="389" readOnly />
                </Form.Item>
                <Form.Item label="LDAP安全性" name="ldapSecurity">
                    <Radio.Group disabled>
                        <Radio.Button value="none">None</Radio.Button>
                        <Radio.Button value="ssl">SSL</Radio.Button>
                        <Radio.Button value="startTLS">StartTLS</Radio.Button>
                    </Radio.Group>
                </Form.Item>
                <Form.Item
                    label="用户名或DN"
                    tooltip="LDAP中管理员用户名(Distinguished Name)，Studio将使用管理员账号查找其他用户信息"
                    name="ldapBindDn"
                >
                    <Input placeholder="请输入LDAP服务器主机名" readOnly />
                </Form.Item>
                <Form.Item name="ldapPassword" tooltip="对应管理员登录LDAP的密码" label="密码">
                    <Input type="password" readOnly placeholder="请输入密码" />
                </Form.Item>
                <h2>用户结构</h2>
                <Form.Item label="用户搜索库" tooltip="LDAP中的搜索基础（将采用递归检索）" name="ldapUserBase">
                    <Input placeholder="ou=users,dc=example,dc=org" readOnly />
                </Form.Item>
                <Form.Item
                    label="用户筛选"
                    tooltip="LDAP中的用户查询筛选，占位符'{Login}'将被用户提供的登录信息替换"
                    name="ldapUserFilter"
                >
                    <Input placeholder="objectClass=inetOrgPerson" readOnly />
                </Form.Item>
                <h2>属性</h2>
                <Form.Item
                    label="电子邮件属性"
                    tooltip="LDAP中用户的'Email'属性。（通常是'mail', 'email' or 'userPrincipalName'）"
                    name="ldapAttributeEmail"
                >
                    <Input placeholder="Mail" readOnly />
                </Form.Item>
                <Form.Item
                    label="姓氏属性"
                    tooltip="LDAP中用户的'Last name'属性（通常是‘SN’）"
                    name="ldapAttributeLastName"
                >
                    <Input placeholder="sn" readOnly />
                </Form.Item>
                <Form.Item
                    label="名字属性"
                    tooltip="LDAP中用户的'First name'属性（通常是‘givenName’）"
                    name="ldapAttributeFirstName"
                >
                    <Input placeholder="uid" readOnly />
                </Form.Item>
            </Form>
        </LoadingLayout>
    );
}
