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

import { useState, useContext, useEffect, useCallback } from 'react';
import * as _ from 'lodash-es';
import { message } from 'antd';
import { GlobalSettingItem } from './types';
import { GlobalSettingsContext } from './context';
import { changeSettingApi, RemoteSettingParams } from './global.api';

function getSettings<T extends string>(settings: GlobalSettingItem[] | null, settingKeys: T[]) {
    return (settings
        ?.filter(item => settingKeys.includes(item.key as T))
        .reduce((memo, current) => {
            memo[current.key] = current;
            return memo;
        }, {}) || {}) as Record<T, GlobalSettingItem | null>;
}

export function useSettings<T extends string>(settingKeys: T[]) {
    const { globalSettings, fetchGlobalSettings } = useContext(GlobalSettingsContext);
    const initialSettings = getSettings(globalSettings, settingKeys);
    const [settings, setSettings] = useState({ ...initialSettings });

    useEffect(() => {
        const settings = getSettings(globalSettings, settingKeys);
        setSettings({ ...settings });
    }, [globalSettings, settingKeys]);

    const remoteSetting = useCallback(
        _.debounce((key: T, params: RemoteSettingParams) => {
            changeSettingApi(key, params)
                .then(() => {
                    message.success('设置成功');
                })
                .catch(() => {
                    message.error('设置失败');
                })
                .finally(() => fetchGlobalSettings());
        }, 200),
        [fetchGlobalSettings],
    );

    const changeSettingItem =
        (key: T, type?: string, changeRemote: boolean = true) =>
        (e: any) => {
            const value = e && e.target ? e.target.value : e;
            const targetSetting = settings[key]!;
            const newTargetSetting = {
                ...targetSetting,
                value,
            };
            setSettings({
                ...settings,
                [key]: newTargetSetting,
            });
            if (!changeRemote) return;
            remoteSetting(key, {
                ...newTargetSetting,
                type,
            });
        };

    return {
        settings,
        changeSettingItem,
    };
}
