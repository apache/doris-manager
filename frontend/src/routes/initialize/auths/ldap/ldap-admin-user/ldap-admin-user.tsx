import { useUsers } from '@src/hooks/use-users.hooks';
import { InitializeAPI } from '@src/routes/initialize/initialize.api';
import { isSuccess } from '@src/utils/http';
import { Form, Radio, Input, Button, message, Select, Space } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import React from 'react';
import { RouteProps, useHistory, useRouteMatch } from 'react-router';
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
