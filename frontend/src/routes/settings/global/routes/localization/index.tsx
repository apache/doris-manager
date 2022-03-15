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
import { useSettings } from '../../global.hooks';
import LoadingLayout from '../../components/loading-layout';
import TemporalForm from './temporal-form';
import NumberForm from './number-form';
import { GlobalSettingsContext } from '../../context';
import { LOADING_WRAPPER_STYLE } from '../../constants';
import { Divider } from 'antd';
import { getValueFromJson } from '../../global.utils';
import { GlobalSettingItem } from '../../types';

const SETTING_KEYS = ['custom-formatting'] as const;

type SettingKeysTypes = typeof SETTING_KEYS[number];

type LocalizationNamspace = 'type/Temporal' | 'type/Number';

export default function Localization() {
    const { loading } = useContext(GlobalSettingsContext);
    const { settings, changeSettingItem } = useSettings<SettingKeysTypes>(SETTING_KEYS as any);
    const targetSettings = settings['custom-formatting'] || ({} as GlobalSettingItem);
    const defaultSettings = getValueFromJson(targetSettings?.default, {});

    const changeLocalizationSetting = (namespace: LocalizationNamspace) => (key: string) => (e: any) => {
        const value = e && e.target ? e.target.value : e;
        changeSettingItem('custom-formatting')({
            ...targetSettings.value,
            [namespace]: {
                ...targetSettings.value?.[namespace],
                [key]: value,
            },
        });
    };

    return (
        <LoadingLayout loading={loading} wrapperStyle={LOADING_WRAPPER_STYLE}>
            <div>
                <TemporalForm
                    settings={targetSettings?.value?.['type/Temporal'] || {}}
                    defaultSettings={defaultSettings['type/Temporal'] || {}}
                    changeLocalizationSettingItem={changeLocalizationSetting('type/Temporal')}
                />
                <Divider style={{ margin: '20px 0', width: 600 }} />
                <NumberForm
                    settings={targetSettings?.value?.['type/Number'] || {}}
                    defaultSettings={defaultSettings['type/Number'] || {}}
                    changeLocalizationSettingItem={changeLocalizationSetting('type/Number')}
                />
            </div>
        </LoadingLayout>
    );
}
