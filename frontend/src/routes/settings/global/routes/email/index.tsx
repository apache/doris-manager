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

import React, { useContext } from 'react';
import { Input, InputNumber, Radio, Row, Button, Col } from 'antd';
import EmailModal from './email-modal';
import LoadingLayout from '../../components/loading-layout';
import SettingItemLayout from '../../components/setting-item-layout';
import { GlobalSettingsContext } from '../../context';
import { useSettings } from '../../global.hooks';
import { getValueFromJson } from '../../global.utils';
import { useEmailSettings } from './hooks';
import { LOADING_WRAPPER_STYLE } from '../../constants';

export const SETTING_KEYS = [
    'email-smtp-host',
    'email-smtp-port',
    'email-smtp-security',
    'email-smtp-username',
    'email-smtp-password',
    'email-from-address',
] as const;

export type SettingKeysTypes = typeof SETTING_KEYS[number];

const SMTP_SECURITY_OPTIONS = [
    { value: 'none', label: 'None' },
    { value: 'ssl', label: 'SSL' },
    { value: 'tls', label: 'TLS' },
    { value: 'starttls', label: 'STARTTLS' },
];

export default function Email() {
    const { loading, fetchGlobalSettings } = useContext(GlobalSettingsContext);
    const { settings, changeSettingItem } = useSettings<SettingKeysTypes>(SETTING_KEYS as any);
    const {
        buttonLoading,
        modalVisible,
        setModalVisible,
        isChanged,
        setIsChanged,
        handleSaveEmailSettings,
        handleDeleteEmailSettings,
        handleSendTestEmail,
    } = useEmailSettings(settings, fetchGlobalSettings);

    const buttonDisabled =
        !settings['email-smtp-host']?.value ||
        !settings['email-smtp-port']?.value ||
        !settings['email-from-address']?.value ||
        settings['email-smtp-security']?.value == null;

    const sendEmailButtonVisible = !isChanged && settings['email-from-address']?.value;

    const changeEmailSettingItem = (key: SettingKeysTypes) => (e: any) => {
        setIsChanged(true);
        changeSettingItem(key, undefined, false)(e);
    };

    return (
        <LoadingLayout loading={loading} wrapperStyle={LOADING_WRAPPER_STYLE}>
            <SettingItemLayout title="SMTP地址" description={settings['email-smtp-host']?.description}>
                <Input
                    style={{ width: 400 }}
                    value={settings['email-smtp-host']?.value}
                    placeholder="请输入SMTP地址"
                    defaultValue={settings['email-smtp-host']?.default}
                    onChange={changeEmailSettingItem('email-smtp-host')}
                />
            </SettingItemLayout>
            <SettingItemLayout title="SMTP端口" description={settings['email-smtp-port']?.description}>
                <InputNumber
                    style={{ width: 400 }}
                    value={getValueFromJson(settings['email-smtp-port']?.value)}
                    placeholder="请输入SMTP端口"
                    defaultValue={settings['email-smtp-port']?.default}
                    onChange={v => changeEmailSettingItem('email-smtp-port')(v + '')}
                />
            </SettingItemLayout>
            <SettingItemLayout title="SMTP安全">
                <Radio.Group
                    value={settings['email-smtp-security']?.value}
                    defaultValue={settings['email-smtp-security']?.default}
                    onChange={changeEmailSettingItem('email-smtp-security')}
                >
                    {SMTP_SECURITY_OPTIONS.map(options => (
                        <Radio value={options.value} key={options.value}>
                            {options.label}
                        </Radio>
                    ))}
                </Radio.Group>
            </SettingItemLayout>
            <SettingItemLayout title="SMTP用户名">
                <Input
                    style={{ width: 400 }}
                    value={settings['email-smtp-username']?.value}
                    placeholder="请输入SMTP用户名"
                    defaultValue={settings['email-smtp-username']?.default}
                    onChange={changeEmailSettingItem('email-smtp-username')}
                />
            </SettingItemLayout>
            <SettingItemLayout title="SMTP密码">
                <Input.Password
                    style={{ width: 400 }}
                    value={settings['email-smtp-password']?.value}
                    placeholder="请输入SMTP密码"
                    defaultValue={settings['email-smtp-password']?.default}
                    onChange={changeEmailSettingItem('email-smtp-password')}
                    autoComplete="new-password"
                />
            </SettingItemLayout>
            <SettingItemLayout title="选择地址" description={settings['email-from-address']?.description}>
                <Input
                    style={{ width: 400 }}
                    value={settings['email-from-address']?.value}
                    placeholder="请输入邮箱地址"
                    defaultValue={settings['email-from-address']?.default}
                    onChange={changeEmailSettingItem('email-from-address')}
                />
            </SettingItemLayout>
            <EmailModal
                visible={modalVisible}
                onOk={handleSendTestEmail}
                confirmLoading={buttonLoading}
                onCancel={() => {
                    setModalVisible(false);
                }}
            />
            <Row gutter={5}>
                <Col>
                    <Button
                        disabled={buttonDisabled}
                        type="primary"
                        loading={buttonLoading}
                        onClick={handleSaveEmailSettings}
                    >
                        保存修改
                    </Button>
                </Col>
                {sendEmailButtonVisible && (
                    <Col>
                        <Button loading={buttonLoading} onClick={() => setModalVisible(true)}>
                            发送邮件测试
                        </Button>
                    </Col>
                )}
                <Col>
                    <Button disabled={buttonDisabled} loading={buttonLoading} onClick={handleDeleteEmailSettings}>
                        清除
                    </Button>
                </Col>
            </Row>
        </LoadingLayout>
    );
}
