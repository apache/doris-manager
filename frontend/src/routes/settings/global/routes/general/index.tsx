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
import { Input, Select } from 'antd';
import LoadingLayout from '../../components/loading-layout';
import SettingItemLayout from '../../components/setting-item-layout';
import { GlobalSettingsContext } from '../../context';
import { useSettings } from '../../global.hooks';
import { LOADING_WRAPPER_STYLE } from '../../constants';
import { getProtocol, getAddress } from '../../global.utils';

const { Option } = Select;

const SETTING_KEYS = ['site-name', 'site-url', 'admin-email'] as const;

type SettingsKeysTypes = typeof SETTING_KEYS[number];

const PROTOCOL_OPTIONS = [{ value: 'http://' }, { value: 'https://' }];

export default function General() {
    const { loading } = useContext(GlobalSettingsContext);
    const { settings, changeSettingItem } = useSettings<SettingsKeysTypes>(SETTING_KEYS as any);

    return (
        <LoadingLayout loading={loading} wrapperStyle={LOADING_WRAPPER_STYLE}>
            <SettingItemLayout title="网站名称" description={settings['site-name']?.description}>
                <Input
                    style={{ width: 400 }}
                    value={settings['site-name']?.value}
                    placeholder="请输入网站名称"
                    defaultValue={settings['site-name']?.default}
                    onChange={changeSettingItem('site-name', 'string')}
                />
            </SettingItemLayout>
            <SettingItemLayout title="网站地址" description={settings['site-url']?.description}>
                <Input
                    style={{ width: 400 }}
                    addonBefore={
                        <Select
                            style={{ width: 112 }}
                            value={getProtocol(settings['site-url']?.value) || undefined}
                            defaultValue={getProtocol(settings['site-url']?.default) || undefined}
                            placeholder="请选择协议"
                            onChange={v => {
                                const address = getAddress(settings['site-url']?.value);
                                changeSettingItem('site-url', 'string')(v + address);
                            }}
                        >
                            {PROTOCOL_OPTIONS.map(options => (
                                <Option value={options.value} key={options.value}>
                                    {options.value}
                                </Option>
                            ))}
                        </Select>
                    }
                    value={getAddress(settings['site-url']?.value)}
                    placeholder="请输入网站地址"
                    defaultValue={getAddress(settings['site-url']?.default)}
                    onChange={e => {
                        const protocol = getProtocol(settings['site-url']?.value);
                        changeSettingItem('site-url', 'string')(protocol + e.target.value);
                    }}
                />
            </SettingItemLayout>
            <SettingItemLayout title="客服邮箱" description={settings['admin-email']?.description}>
                <Input
                    style={{ width: 400 }}
                    value={settings['admin-email']?.value}
                    placeholder="请输入客服邮箱"
                    defaultValue={settings['admin-email']?.default}
                    onChange={changeSettingItem('admin-email', 'string')}
                />
            </SettingItemLayout>
        </LoadingLayout>
    );
}
