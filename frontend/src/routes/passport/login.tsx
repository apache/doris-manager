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

import { Form, Input, Button, Checkbox, message } from 'antd';
import styles from './index.module.less';
import { PassportAPI } from './passport.api';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import { useEffect } from 'react';
import { dorisAuthProvider } from '@src/components/auths/doris-auth-provider';
import { useAuth } from '@src/hooks/use-auth';
export function Login() {
    const [form] = Form.useForm();
    const { t } = useTranslation();
    const navigate = useNavigate();
    function handleLogin(_value: any) {
        PassportAPI.SessionLogin(_value).then(res => {
            if (res.code === 0) {
                PassportAPI.getCurrentUser().then(user => {
                    window.localStorage.setItem('login', 'true');
                    window.localStorage.setItem('user', JSON.stringify(user.data));
                    navigate('/space/list');
                });
            } else {
                message.warn(res.msg);
            }
        });
    }

    // check should switch to initialize page
    useAuth();

    useEffect(() => {
        const login = dorisAuthProvider.checkLogin();
        if (login) {
            navigate('/');
        }
    }, []);

    return (
        <div className={styles['not-found']}>
            <div className={styles['login-container']}>
                <Form form={form} layout="vertical" requiredMark={true} onFinish={handleLogin}>
                    <h2 style={{ textAlign: 'center' }}>{t`login`}</h2>
                    <Form.Item label={t`Username or Mail`} required name="username">
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
                            {t`SignIn`}
                        </Button>
                    </Form.Item>
                </Form>
            </div>
        </div>
    );
}
