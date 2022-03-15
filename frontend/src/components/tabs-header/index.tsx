import React from 'react';
import { useLocation, useHistory } from 'react-router-dom';
import { Tabs } from 'antd';

const { TabPane } = Tabs;

interface TabsHeaderProps {
    routes: {
        label: string;
        path: string;
    }[];
}

export default function TabsHeader(props: TabsHeaderProps) {
    const { routes } = props;
    const { pathname } = useLocation();
    const history = useHistory();

    const handleTabChange = (key: string) => {
        history.replace(key);
    };

    const findActiveKey = (pathname: string) => {
        return routes.find(route => pathname.startsWith(route.path))?.path;
    };

    return (
        <Tabs onTabClick={handleTabChange} activeKey={findActiveKey(pathname)}>
            {routes.map(route => (
                <TabPane tab={route.label} key={route.path} />
            ))}
        </Tabs>
    );
}
