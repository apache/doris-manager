import React, { PropsWithChildren } from 'react';
import GlobalSettingsContextProvider from './global-settings-context';

export default function GlobalSettingProvider(props: PropsWithChildren<{}>) {
    return <GlobalSettingsContextProvider>{props.children}</GlobalSettingsContextProvider>;
}

export * from './global-settings-context';
