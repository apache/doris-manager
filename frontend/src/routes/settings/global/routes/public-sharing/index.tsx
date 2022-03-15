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
import { Switch } from 'antd';
import LoadingLayout from '../../components/loading-layout';
import SettingItemLayout from '../../components/setting-item-layout';
import { GlobalSettingsContext } from '../../context';
import { useSettings } from '../../global.hooks';
import { LOADING_WRAPPER_STYLE } from '../../constants';
import { getValueFromJson } from '../../global.utils';

const SETTING_KEYS = ['enable-public-sharing'] as const;

type SettingKeysTypes = typeof SETTING_KEYS[number];

export default function PublicSharing() {
    const { loading } = useContext(GlobalSettingsContext);
    const { settings, changeSettingItem } = useSettings<SettingKeysTypes>(SETTING_KEYS as any);

    return (
        <LoadingLayout loading={loading} wrapperStyle={LOADING_WRAPPER_STYLE}>
            <SettingItemLayout title="开启公开共享" description={settings['enable-public-sharing']?.description}>
                <Switch
                    defaultChecked={getValueFromJson(settings['enable-public-sharing']?.default) || false}
                    checked={getValueFromJson(settings['enable-public-sharing']?.value) || false}
                    onChange={changeSettingItem('enable-public-sharing', 'boolean')}
                />
                <span style={{ marginLeft: 10 }}>
                    {getValueFromJson(settings['enable-public-sharing']?.value) ? '启用' : '取消'}
                </span>
            </SettingItemLayout>
        </LoadingLayout>
    );
}
