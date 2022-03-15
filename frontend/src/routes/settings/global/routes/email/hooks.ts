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

import { useState } from 'react';
import { message } from 'antd';
import * as _ from 'lodash-es';
import { useAsync } from '@src/hooks/use-async';
import { changeEmailSettingApi, deleteEmailSettingApi, sendTestEmailApi } from '../../global.api';
import { SETTING_KEYS, SettingKeysTypes } from '.';
import { GlobalSettingItem } from '../../types';

export function useEmailSettings(
    settings: Record<SettingKeysTypes, GlobalSettingItem | null>,
    fetchGlobalSettings: () => Promise<void | GlobalSettingItem[]>,
) {
    const { loading: buttonLoading, run: runRemoteApi } = useAsync();
    const [modalVisible, setModalVisible] = useState(false);
    const [isChanged, setIsChanged] = useState(false);

    const handleSaveEmailSettings = _.debounce(() => {
        const params = Object.keys(settings).reduce((memo, current) => {
            if (SETTING_KEYS.includes(current as SettingKeysTypes)) {
                memo[current] = settings[current]?.value || null;
            }
            return memo;
        }, {} as Record<string, string>);
        runRemoteApi(changeEmailSettingApi(params))
            .then(() => {
                message.success('保存成功');
            })
            .catch(() => {
                message.error('保存失败');
            })
            .finally(() => fetchGlobalSettings())
            .then(() => setIsChanged(false));
    }, 200);

    const handleDeleteEmailSettings = _.debounce(() => {
        runRemoteApi(deleteEmailSettingApi())
            .then(() => {
                message.success('清除成功');
            })
            .catch(() => {
                message.error('清除失败');
            })
            .finally(() => fetchGlobalSettings())
            .then(() => setIsChanged(false));
    });

    const handleSendTestEmail = (email: string) => () => {
        runRemoteApi(sendTestEmailApi({ email }))
            .then(() => {
                message.success('发送成功');
            })
            .catch(() => {
                message.error('发送失败');
            })
            .finally(() => setModalVisible(false));
    };

    return {
        isChanged,
        modalVisible,
        buttonLoading,
        setModalVisible,
        setIsChanged,
        handleSaveEmailSettings,
        handleDeleteEmailSettings,
        handleSendTestEmail,
    };
}
