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

import { ExclamationCircleOutlined } from '@ant-design/icons';
import { IRole } from '@src/common/common.interface';
import { FlatBtnGroup, FlatBtn } from '@src/components/flatbtn';
import { useRoles } from '@src/hooks/use-roles.hooks';
import { isSuccess } from '@src/utils/http';
import { showName } from '@src/utils/utils';
import { Table, Modal, message, Row } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { RoleAPI } from '../role.api';
import { CreateOrEditRoleModal } from './create-or-edit-modal';

export function RoleList() {
    const { t } = useTranslation();
    const { roles, getRoles, loading } = useRoles();
    const [visible, setVisible] = useState(false);
    const [currentRole, setCurrentRole] = useState<IRole | undefined>();
    const [form] = useForm();
    const { confirm } = Modal;
    const columns = [
        {
            title: t`roleName`,
            key: 'name',
            render: (record: IRole) => (
                <FlatBtn to={`/admin/people/role/${record.id}`}>{showName(record.name)}</FlatBtn>
            ),
        },
        {
            title: t`members`,
            dataIndex: 'member_count',
            key: 'member_count',
        },
        {
            title: t`operation`,
            key: 'actions',
            render: (record: IRole) => {
                const forbiddenEditRole = record.name.includes('Administrators') || record.name.includes('All Users');
                return (
                    <FlatBtnGroup>
                        <FlatBtn
                            disabled={forbiddenEditRole}
                            onClick={() => {
                                console.log(record);
                                setCurrentRole(record);
                                setVisible(true);
                            }}
                        >
                            {t`edit`}
                        </FlatBtn>
                        <FlatBtn disabled={forbiddenEditRole} onClick={() => handleDelete(record)}>
                            {t`Delete`}
                        </FlatBtn>
                    </FlatBtnGroup>
                );
            },
        },
    ];
    function handleDelete(record: IRole) {
        confirm({
            title: t`deleteThisRole`,
            icon: <ExclamationCircleOutlined />,
            content: t`deleteThisRoleMessage`,
            onOk() {
                return RoleAPI.deleteRole({ roleId: record.id }).then(res => {
                    if (isSuccess(res)) {
                        message.success(t`DeleteSuccessTips`);
                        getRoles();
                    } else {
                        message.error(res.msg);
                    }
                });
            },
            okType: 'danger',
            onCancel() {
                console.log('Cancel');
            },
        });
    }
    return (
        <>
            <Row justify="space-between" align="middle" style={{ marginBottom: 20 }}>
                <span style={{ color: '#aaa' }}>{t`roleTopMessage`}</span>
            </Row>

            <Table
                pagination={false}
                rowKey="id"
                bordered={true}
                columns={columns}
                dataSource={roles}
                loading={loading}
            />
            {visible && (
                <CreateOrEditRoleModal
                    onSuccess={() => {
                        setVisible(false);
                        form.resetFields();
                        getRoles();
                    }}
                    form={form}
                    role={currentRole}
                    onCancel={() => setVisible(false)}
                />
            )}
        </>
    );
}
