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

import { Col, Divider, InputNumber, message, Modal, Row, Select, Space, Tag } from 'antd';
import React, { useContext, useEffect, useState } from 'react';
import { Form, Input, Button, Radio } from 'antd';
import styles from '../../space.less';
type RequiredMark = boolean | 'optional';
type testStatus = 'success' | 'fault' | 'none';
import { useHistory } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useRecoilValue } from 'recoil';
import { SpaceAPI } from '@src/routes/space/space.api';
import { usersQuery } from '@src/routes/space/space.recoil';
import TextArea from 'antd/lib/input/TextArea';
import { PageContainer } from '@ant-design/pro-layout';
import { NewSpaceInfoContext } from '@src/common/common.context';

export function SpaceRegister() {
    const { t } = useTranslation();
    const { form, reqInfo } = useContext(NewSpaceInfoContext);
    const allUsers = useRecoilValue(usersQuery);

    useEffect(() => {
        form.setFieldsValue({...reqInfo.spaceInfo});
    }, [reqInfo.cluster_id]);

    return (
        <PageContainer
            header={{
                title: <h2>{t`Space Register`}</h2>,
            }}
        >
            <Form
                form={form}
                layout="vertical"
                style={{ width: 400 }}
                name="spaceInfo"
                initialValues={{
                    name: reqInfo.spaceInfo.name,
                    spaceAdminUsers: reqInfo.spaceInfo.spaceAdminUsers,
                    describe: reqInfo.spaceInfo.describe,
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
                                    return Promise.reject(new Error(''));
                                }
                                let resData = await SpaceAPI.spaceCheck(value);
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
                    <TextArea rows={5} placeholder={t`spaceIntroduction`} maxLength={200} />
                </Form.Item>
                <Form.Item
                    label={t`adminName`}
                    name="spaceAdminUsers"
                    rules={[{ required: true, message: '', type: 'array' }]}
                >
                    <Select
                        showSearch
                        mode="multiple"
                        placeholder={t`Please Select Users...`}
                        optionFilterProp="title"
                        optionLabelProp="label"
                        filterOption={(input, option) => {
                            return (option?.title as string).toLowerCase().indexOf(input.toLowerCase()) >= 0;
                        }}
                    >
                        {allUsers
                            .filter(user => user.is_active)
                            .map(user => {
                                return (
                                    <Select.Option label={user.name} value={user.id} title={user.name} key={user.id}>
                                        <Row justify="space-between">
                                            <div className={styles.optionContent}>
                                                <strong>{user.name}</strong>
                                                <span>{user.email ? `(${user.email})` : ''}</span>
                                            </div>
                                        </Row>
                                    </Select.Option>
                                );
                            })}
                        {!allUsers.length && <span>无可选用户</span>}
                    </Select>
                </Form.Item>
            </Form>
        </PageContainer>
    );
}
