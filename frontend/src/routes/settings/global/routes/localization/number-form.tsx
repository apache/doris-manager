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

import React from 'react';
import { Select } from 'antd';
import FormItemLayout from './form-item-layout';
import FormLayout from './form-layout';
import { NUMBER_SEPARATORS_OPTIONS } from './constants';

const { Option } = Select;

interface NumberSettings {
    number_separators: string;
}

interface NumberFormProps {
    settings: NumberSettings;
    defaultSettings: NumberSettings;
    changeLocalizationSettingItem: (key: string) => (e: any) => void;
}

export default function NumberForm(props: NumberFormProps) {
    const { settings, defaultSettings, changeLocalizationSettingItem } = props;
    return (
        <FormLayout title="数字">
            <FormItemLayout title="分隔符">
                <Select
                    style={{ width: 300 }}
                    value={settings.number_separators}
                    defaultValue={defaultSettings.number_separators}
                    onChange={changeLocalizationSettingItem('number_separators')}
                >
                    {NUMBER_SEPARATORS_OPTIONS.map(options => (
                        <Option value={options.value} key={options.value}>
                            {options.label}
                        </Option>
                    ))}
                </Select>
            </FormItemLayout>
        </FormLayout>
    );
}
