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

import React, { useContext, useState } from 'react';
import moment from 'moment';
import { Button, Table, Input, Modal, message, Switch, Row } from 'antd';
import { CopyOutlined } from '@ant-design/icons';
import { useForm } from 'antd/lib/form/Form';
import { useTranslation } from 'react-i18next';
import StatusMark from '@src/components/status-mark';
import { FlatBtnGroup, FlatBtn } from '@src/components/flatbtn';
import { UserInfoContext } from '@src/common/common.context';
import { isSuccess } from '@src/utils/http';
import { UserAPI } from '../user.api';
import { useGlobalUsers } from '../user.hooks';
import { generatePassword, copyText } from '../user.utils';
import { CreateOrEditRoleModal } from './create-or-edit-modal';
import styles from './list.module.less';
import { UserInfo } from '@src/common/common.interface';

export function UserList() {
    const { t } = useTranslation();
    const userInfo = useContext(UserInfoContext) as UserInfo;
    const { users, getUsers, loading, setUsers } = useGlobalUsers({
        include_deactivated: true,
    });
    const [visible, setVisible] = useState(false);
    const [currentUser, setCurrentUser] = useState<any>();
    const [form] = useForm();
    const columns = [
        {
            title: t`username`,
            key: 'name',
            dataIndex: 'name',
        },
        {
            title: t`Mail`,
            dataIndex: 'email',
            key: 'email',
        },
        {
            title: t`status`,
            dataIndex: 'is_active',
            filters: [
                { text: t`enabled`, value: true },
                { text: t`disabled`, value: false },
            ],
            render: (is_active: boolean) => (
                <StatusMark status={is_active ? 'success' : 'deactivated'}>
                    {is_active ? t`activated` : t`deactivated`}
                </StatusMark>
            ),
            onFilter: (value: any, record: any) => record.is_active === value,
        },
        {
            title: t`superAdministrator`,
            dataIndex: 'is_super_admin',
            key: 'is_super_admin',
            render: (is_super_admin: boolean, record: any, index: number) => (
                <Switch
                    checked={is_super_admin}
                    onChange={changeSuperAdmin(record.id, index)}
                    disabled={userInfo.id === record.id}
                />
            ),
        },
        {
            title: t`lastLogin`,
            dataIndex: 'last_login',
            key: 'last_login',
            render: (last_login: string) => {
                return (
                    <span>
                        {last_login == null ? t`neverLoggedIn` : moment(last_login).format('YYYY-MM-DD HH:mm:ss')}
                    </span>
                );
            },
        },
        {
            title: t`operation`,
            key: 'actions',
            render: (record: any) => {
                const disabled = userInfo.id === record.id;
                return (
                    <FlatBtnGroup showNum={4}>
                        <FlatBtn
                            onClick={() => {
                                setCurrentUser(record);
                                setVisible(true);
                            }}
                        >
                            {t`edit`}
                        </FlatBtn>

                        <FlatBtn
                            onClick={() => handleResetPassword(record)}
                            disabled={disabled}
                        >{t`resetPassword`}</FlatBtn>

                        <FlatBtn onClick={() => toggleActivate(record)} disabled={disabled}>
                            {record.is_active ? t`deactivateUser` : t`activateUser`}
                        </FlatBtn>
                    </FlatBtnGroup>
                );
            },
        },
    ];

    const handleResetPassword = (record: any) => {
        Modal.confirm({
            title: t`resetPasswordOrNot`,
            onOk: () => {
                const password = generatePassword();
                UserAPI.resetPassword({ user_id: record.id, password })
                    .then(res => {
                        if (isSuccess(res)) {
                            Modal.confirm({
                                title: t`pleaseSaveYourPassword`,
                                content: (
                                    <div className={styles.clipInput}>
                                        <Input.Password
                                            readOnly
                                            value={password}
                                            addonAfter={
                                                <CopyOutlined
                                                    onClick={() => {
                                                        try {
                                                            copyText(password);
                                                            message.success(t`copySuccess`);
                                                        } catch (e) {
                                                            message.error(t`copyError`);
                                                        }
                                                    }}
                                                />
                                            }
                                        />
                                    </div>
                                ),
                            });
                            return;
                        }
                        message.error(res.msg);
                    })
                    .catch(() => message.error(t`resetPasswordFailed`));
            },
        });
    };

    const changeSuperAdmin = (user_id: number, index: number) => async (checked: boolean) => {
        users.splice(index, 1, {
            ...users[index],
            is_super_admin: !users[index].is_super_admin,
        });
        setUsers([...users]);
        UserAPI.updateUserAdmin({ admin: checked, user_id })
            .then(res => {
                if (!isSuccess(res)) {
                    message.error(res.msg);
                    getUsers();
                }
            })
            .catch(() => {
                message.error(t`setupFailed`);
                getUsers();
            });
    };
    const toggleActivate = (record: any) => {
        const { is_active } = record;
        Modal.confirm({
            title: `${is_active ? t`whetherToDeactivate` : t`whetherToActivate`} ${record.name} ？`,
            content: `${is_active ? t`afterDeactivate` : t`afterActivate`} ${record.name} ${
                is_active ? t`canNotLogin` : t`canLoginAgain`
            }`,
            onOk() {
                const operator = is_active ? UserAPI.deactivateUser : UserAPI.activateUser;
                operator({ user_id: record.id })
                    .then(res => {
                        if (isSuccess(res)) {
                            message.success(t`setupSuccess`);
                            getUsers();
                        } else {
                            message.error(t`setupFailed`);
                        }
                    })
                    .catch(() => {
                        message.error(t`setupFailed`);
                    });
            },
        });
    };

    return (
        <>
            <Row justify="end" style={{ marginBottom: 20 }}>
                <Button
                    type="primary"
                    onClick={() => {
                        setVisible(true);
                        setCurrentUser(undefined);
                    }}
                >
                    {t`addUser`}
                </Button>
            </Row>
            <Table rowKey="id" bordered={true} columns={columns} dataSource={users} loading={loading} size="middle" />
            {visible && (
                <CreateOrEditRoleModal
                    onSuccess={() => {
                        setVisible(false);
                        form.resetFields();
                        getUsers();
                    }}
                    form={form}
                    user={currentUser}
                    onCancel={() => setVisible(false)}
                />
            )}
        </>
    );
}
