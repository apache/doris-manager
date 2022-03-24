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

import React, { useEffect, useState } from 'react';
import { Form, Input, Button, Radio, Checkbox, message } from 'antd';
import styles from './index.module.less';
import { PassportAPI } from './passport.api';
import { useTranslation } from 'react-i18next';
import { useHistory } from 'react-router-dom';
import { useAuth } from '@src/hooks/use-auth';
export function Login(props: any) {
    const [form] = Form.useForm();
    const { t }  = useTranslation();
    const history = useHistory();
    const {initialized} = useAuth();
    function handleLogin(_value: any) {
        PassportAPI.SessionLogin(_value).then(res => {
            if (res.code === 0) {
                PassportAPI.getCurrentUser().then(user => {
                    window.localStorage.setItem('login', 'true')
                    window.localStorage.setItem('user', JSON.stringify(user.data));
                    history.push('/space/list');
                })
            } else {
                message.warn(res.msg);
            }
        });
    }

    return (
        <div className={styles['not-found']}>
            <div className={styles['login-container']}>
                <Form
                    form={form}
                    layout="vertical"
                    requiredMark={true}
                    onFinish={handleLogin}
                >
                    <h2 style={{ textAlign: 'center' }}>{ t`login`}</h2>
                    <Form.Item label={ t`Username or Mail`} required name="username">
                        <Input placeholder={t`Please input the username or email`} />
                    </Form.Item>
                    <Form.Item label={t`password`} required name="password">
                        <Input.Password
                            placeholder={t`Please input the password`}
                            style={{ width: '100%', borderRadius: 4, padding: '0.75em' }}
                        />
                    </Form.Item>
                    <Form.Item name="remember" valuePropName="checked">
                        <Checkbox>记住我</Checkbox>
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" style={{ width: '100%', borderRadius: 4, height: 45 }} htmlType="submit">
                            { t`SignIn`}
                        </Button>
                    </Form.Item>
                    {/* <Form.Item style={{ textAlign: 'center' }}>
                        <Link
                            style={{ fontSize: '14px', color: '#C7CFD4' }}
                            onClick={() => props.history.push(`/forgot`)}
                        >
                            { t`ForgetThePassword`}
                        </Link>
                    </Form.Item> */}
                </Form>
            </div>
        </div>
    );
};
