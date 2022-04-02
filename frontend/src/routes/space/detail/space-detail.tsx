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

import { useEffect, useState } from 'react';
import styles from '../space.less';
import { Button, Form, Input, Row, Space, Select, Col, InputNumber, Tag } from 'antd';
import { message } from 'antd';
import { SpaceAPI } from '../space.api';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import { useAsync } from '@src/hooks/use-async';
import { isSuccess } from '@src/utils/http';

export function SpaceDetail() {
    const { t } = useTranslation();
    const [form] = Form.useForm();
    const [userInfo, setUserInfo] = useUserInfo();
    const params = useParams<{ spaceId: string }>();
    const [saveButtonDisable, setSaveButtonDisable] = useState(true);
    const [initForm, setInitForm] = useState<any>({});
    const navigate = useNavigate();
    const { data: allUsers, loading, run: runGetAllUsers } = useAsync<any[]>({ data: [], loading: true });
    function getSpaceInfo() {
        SpaceAPI.spaceGet(params.spaceId as string).then(res => {
            const { msg, data, code } = res;
            if (code === 0) {
                if (res.data) {
                    form.setFieldsValue({ ...res.data });
                    setInitForm(res.data);
                    if (allUsers && allUsers?.length) {
                        setInitForm({
                            ...res.data,
                            spaceAdminUser: allUsers,
                        });
                    }
                }
            } else {
                message.error(msg);
            }
        });
    }

    useEffect(() => {
        runGetAllUsers(
            SpaceAPI.getUsers({ include_deactivated: true }).then(res => {
                if (isSuccess(res)) return res.data;
                return Promise.reject(res);
            }),
        );
    }, [runGetAllUsers]);

    useEffect(() => {
        if (loading) return;
        getSpaceInfo();
    }, [loading]);

    const handleSave = () => {
        form.validateFields().then(value => {
            SpaceAPI.spaceUpdate({
                describe: value.describe,
                name: value.name.trim(),
                spaceAdminUsers: value.spaceAdminUserId,
                spaceId: params.spaceId as string,
            }).then(res => {
                console.log(res);
                if (res.code === 0) {
                    message.success(res.msg);
                    setSaveButtonDisable(true);
                } else {
                    message.error(res.msg);
                }
            });
        });
    };

    return (
        <div style={{ padding: '50px' }}>
            <Row justify="center">
                <Col span={8}>
                    <Form
                        form={form}
                        layout="vertical"
                        onChange={() => {
                            if (!saveButtonDisable) return;
                            setSaveButtonDisable(false);
                        }}
                    >
                        <Form.Item
                            label={t`spaceName`}
                            name="name"
                            rules={[
                                {
                                    required: true,
                                    validator: async (rule, value) => {
                                        if (!value) {
                                            return Promise.reject(new Error(t`required`));
                                        }
                                        if (value === initForm?.name) {
                                            return Promise.resolve();
                                        }
                                        const resData = await SpaceAPI.spaceCheck(value);
                                        if (resData.code === 0) {
                                            return Promise.resolve();
                                        }
                                        return Promise.reject(new Error(resData.msg));
                                    },
                                },
                            ]}
                            validateTrigger="onBlur"
                        >
                            <Input placeholder={t`spaceName`} min={2} max={30} />
                        </Form.Item>
                        <Form.Item label={t`spaceIntroduction`} name="describe">
                            <Input.TextArea placeholder={t`spaceIntroduction`} maxLength={200} />
                        </Form.Item>
                        <Form.Item
                            label={t`adminName`}
                            name="spaceAdminUserId"
                            rules={[{ required: true, message: t`required`, type: 'array' }]}
                        >
                            <Select
                                showSearch
                                mode="multiple"
                                placeholder={t`pleaseSelectUsers`}
                                optionFilterProp="title"
                                optionLabelProp="label"
                                disabled={!userInfo?.is_super_admin && !userInfo.is_admin}
                                filterOption={(input, option) => {
                                    return (option?.title as string).toLowerCase().indexOf(input.toLowerCase()) >= 0;
                                }}
                                onChange={() => {
                                    if (!saveButtonDisable) return;
                                    setSaveButtonDisable(false);
                                }}
                            >
                                {initForm?.spaceAdminUser?.map((user: any, index: number) => {
                                    return (
                                        <Select.Option
                                            key={user.id.toString()}
                                            label={user.name}
                                            value={user.id}
                                            title={user.name}
                                            disabled={userInfo.id === user.id && !userInfo.is_super_admin}
                                        >
                                            <Row justify="space-between">
                                                <div className={styles.optionContent}>
                                                    <strong>{user.name}</strong>
                                                    <span>{user.email ? `(${user.email})` : ''}</span>
                                                </div>
                                            </Row>
                                        </Select.Option>
                                    );
                                })}
                            </Select>
                        </Form.Item>
                    </Form>
                </Col>
            </Row>

            <Row justify="center">
                <Space>
                    <Button type="primary" onClick={handleSave} disabled={saveButtonDisable}>
                        {t`Save`}
                    </Button>
                </Space>
            </Row>
        </div>
    );
}
