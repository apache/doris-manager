import { InitializeAPI } from '@src/routes/initialize/initialize.api';
import { isSuccess } from '@src/utils/http';
import { Form, Input, Button, message, Radio, InputNumber } from 'antd';
import React, { useState } from 'react';
import { useHistory } from 'react-router';

export function LDAPConfig(props: any) {
    const history = useHistory();
    const [loading, setLoading] = useState(false);

    async function onFinish(values: any) {
        const { password_confirm, username, ...params } = values;
        setLoading(true);
        const res = await InitializeAPI.setLDAP({
            authType: 'ldap',
            ldapSetting: {
                ...values,
                ['ldap-user-base']: [values['ldap-user-base']],
            },
        });
        setLoading(false);
        if (isSuccess(res)) {
            console.log(res);
            history.push('/initialize/auth/ldap/ldap-info');
        } else {
            message.error(res.msg);
            history.push('/initialize/auth/ldap/admin-user');
        }
    }
    return (
        <div style={{ paddingBottom: 40 }}>
            <h2>服务器</h2>
            <Form
                name="ldap_server"
                layout="vertical"
                onFinish={onFinish}
                autoComplete="off"
            >
                <Form.Item
                    label="LDAP主机"
                    tooltip="服务器主机名"
                    name="ldap-host"
                    rules={[{ required: true, message: '请输入LDAP服务器主机名' }]}
                >
                    <Input placeholder="请输入LDAP服务器主机名" />
                </Form.Item>

                <Form.Item
                    name="ldap-port"
                    label="LDAP端口"
                    rules={[{ required: true, message: '请输入LDAP端口' }]}
                    tooltip="服务器端口，如果使用SSL,端口号通常为389或636"
                >
                    <InputNumber min={0} placeholder="389" />
                </Form.Item>
                <Form.Item label="LDAP安全性" name="ldap-security" required>
                    <Radio.Group>
                        <Radio.Button value="none">None</Radio.Button>
                        <Radio.Button value="ssl">SSL</Radio.Button>
                        <Radio.Button value="startTLS">StartTLS</Radio.Button>
                    </Radio.Group>
                </Form.Item>
                <Form.Item
                    label="用户名或DN"
                    tooltip="LDAP中管理员用户名(Distinguished Name)，Studio将使用管理员账号查找其他用户信息"
                    name="ldap-bind-dn"
                    required
                    rules={[{ required: true, message: '请输入用户名或DN' }]}
                >
                    <Input placeholder="请输入LDAP服务器主机名" />
                </Form.Item>
                <Form.Item
                    name="ldap-password"
                    rules={[{ required: true, message: '请输入密码' }]}
                    tooltip="对应管理员登录LDAP的密码"
                    label="密码"
                    required
                >
                    <Input type="password" placeholder="请输入密码" />
                </Form.Item>
                {/* </Form> */}
                <h2>用户结构</h2>
                {/* <Form name="ldap_user" layout="vertical" onFinish={onFinish} autoComplete="off"> */}
                <Form.Item
                    label="用户搜索库"
                    tooltip="LDAP中的搜索基础（将采用递归检索）"
                    name="ldap-user-base"
                    rules={[{ required: true, message: '请输入用户搜索库' }]}
                >
                    <Input placeholder="ou=users,dc=example,dc=org" />
                </Form.Item>
                <Form.Item
                    label="用户筛选"
                    tooltip="LDAP中的用户查询筛选，占位符'{Login}'将被用户提供的登录信息替换"
                    name="ldap-user-filter"
                    rules={[{ required: true, message: '请输入用户筛选' }]}
                >
                    <Input placeholder="objectClass=inetOrgPerson" />
                </Form.Item>
                {/* </Form> */}
                <h2>属性</h2>
                {/* <Form name="ldap_attribute" layout="vertical" onFinish={onFinish} autoComplete="off"> */}
                <Form.Item
                    label="电子邮件属性"
                    tooltip="LDAP中用户的'Email'属性。（通常是'mail', 'email' or 'userPrincipalName'）"
                    name="ldap-attribute-email"
                    rules={[{ required: true, message: '请输入电子邮件属性' }]}
                >
                    <Input placeholder="Mail" />
                </Form.Item>
                <Form.Item
                    label="名字属性"
                    tooltip="LDAP中用户的'Last name'属性（通常是‘SN’）"
                    name="ldap-attribute-lastname"
                    rules={[{ required: true, message: '请输入名字属性' }]}
                >
                    <Input placeholder="uid" />
                </Form.Item>
                <Form.Item
                    label="姓氏属性"
                    tooltip="LDAP中用户的'First name'属性（通常是‘givenName’）"
                    name="ldap-attribute-firstname"
                    rules={[{ required: true, message: '请输入姓氏属性' }]}
                >
                    <Input placeholder="sn" />
                </Form.Item>
                <Form.Item>
                    <Button loading={loading} type="primary" htmlType="submit">
                        保存
                    </Button>
                </Form.Item>
            </Form>
        </div>
    );
}
