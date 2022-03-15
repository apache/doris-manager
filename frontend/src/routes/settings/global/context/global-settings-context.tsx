import React, { PropsWithChildren, useCallback, useEffect } from 'react';
import { message } from 'antd';
import { GlobalSettingItem } from '../types';
import { fetchGlobalSettingsApi } from '../global.api';
import { useAsync } from '@src/hooks/use-async';

interface GlobalSettingsContextProps {
    globalSettings: GlobalSettingItem[] | undefined;
    loading: boolean;
    error: Error | null;
    fetchGlobalSettings: () => Promise<void | GlobalSettingItem[]>;
}

export const GlobalSettingsContext = React.createContext<GlobalSettingsContextProps>({
    globalSettings: [],
    loading: true,
    error: null,
    fetchGlobalSettings: () => Promise.resolve(),
});

const ERROR_MESSAGE = '获取平台设置失败';

export default function GlobalSettingsContextProvider(props: PropsWithChildren<{}>) {
    const {
        data: globalSettings,
        loading,
        error,
        run,
    } = useAsync<GlobalSettingItem[]>({
        loading: true,
        data: [],
    });
    const fetchGlobalSettings = useCallback(() => {
        return run(fetchGlobalSettingsApi(), { setStartLoading: false }).catch(() => {
            message.error(ERROR_MESSAGE);
        });
    }, [run]);

    useEffect(() => {
        fetchGlobalSettings();
    }, [run, fetchGlobalSettings]);

    return (
        <GlobalSettingsContext.Provider
            value={{
                globalSettings,
                loading,
                error,
                fetchGlobalSettings,
            }}
        >
            {props.children}
        </GlobalSettingsContext.Provider>
    );
}
