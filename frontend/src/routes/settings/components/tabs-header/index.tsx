import React from 'react';
import { useLocation, useHistory } from 'react-router-dom';
import { Tabs } from 'antd';
import { useTranslation } from 'react-i18next';

const { TabPane } = Tabs;

export default function TabsHeader() {
    const { pathname } = useLocation();
    const history = useHistory();

    const {t} = useTranslation()

    const handleTabChange = (key: string) => {
        history.replace(key);
    };

    return (
        <Tabs onChange={handleTabChange} defaultActiveKey={pathname}>
            <TabPane tab={t`userManagement`} key="/settings/user" />
        </Tabs>
    );
}
