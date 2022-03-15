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
