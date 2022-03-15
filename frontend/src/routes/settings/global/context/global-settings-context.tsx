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
