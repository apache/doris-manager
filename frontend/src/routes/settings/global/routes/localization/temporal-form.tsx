import React, { useMemo } from 'react';
import { Select, Radio, Space, Switch } from 'antd';
import FormLayout from './form-layout';
import FormItemLayout from './form-item-layout';
import { DEFAULT_DATE_STYLE_OPTIONS, DATE_SEPARATOR_OPTIONS, TIME_STYLE_OPTIONS } from './constants';

const { Option } = Select;

interface TemporalSettings {
    date_abbreviate: boolean;
    date_separator: string;
    date_style: string;
    time_style: string;
}

interface TemporalFormProps {
    settings: TemporalSettings;
    defaultSettings: TemporalSettings;
    changeLocalizationSettingItem: (key: string) => (e: any) => void;
}

export default function TemporalForm(props: TemporalFormProps) {
    const { settings, defaultSettings, changeLocalizationSettingItem } = props;

    const DATE_STYLE_OPTIONS = useMemo(() => {
        return DEFAULT_DATE_STYLE_OPTIONS.map(options => ({
            value: options.value,
            label: options.label
                ? options.label.join(settings.date_separator)
                : settings.date_abbreviate
                ? options.compressedLabel
                : options.completeLabel,
        }));
    }, [settings.date_separator, settings.date_abbreviate]);

    return (
        <FormLayout title="日期和时间">
            <FormItemLayout title="日期格式">
                <Select
                    style={{ width: 300 }}
                    defaultValue={defaultSettings.date_style}
                    value={settings.date_style}
                    placeholder="请选择日期格式"
                    onChange={changeLocalizationSettingItem('date_style')}
                >
                    {DATE_STYLE_OPTIONS.map(item => (
                        <Option value={item.value} key={item.value}>
                            {item.label}
                        </Option>
                    ))}
                </Select>
            </FormItemLayout>
            <FormItemLayout title="日期分隔符">
                <Radio.Group
                    onChange={changeLocalizationSettingItem('date_separator')}
                    value={settings.date_separator}
                    defaultValue={defaultSettings.date_separator}
                >
                    <Space direction="vertical">
                        {DATE_SEPARATOR_OPTIONS.map(item => (
                            <Radio value={item.value} key={item.value}>
                                {item.label}
                            </Radio>
                        ))}
                    </Space>
                </Radio.Group>
            </FormItemLayout>
            <FormItemLayout title="日和月的缩写">
                <Switch
                    checked={settings.date_abbreviate}
                    defaultChecked={defaultSettings.date_abbreviate}
                    onChange={changeLocalizationSettingItem('date_abbreviate')}
                />
            </FormItemLayout>
            <FormItemLayout title="时间格式">
                <Radio.Group
                    onChange={changeLocalizationSettingItem('time_style')}
                    value={settings.time_style}
                    defaultValue={defaultSettings.time_style}
                >
                    <Space direction="vertical">
                        {TIME_STYLE_OPTIONS.map(item => (
                            <Radio value={item.value} key={item.value}>
                                {item.label}
                            </Radio>
                        ))}
                    </Space>
                </Radio.Group>
            </FormItemLayout>
        </FormLayout>
    );
}
