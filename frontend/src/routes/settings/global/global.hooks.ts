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
