import React, { useContext } from 'react';
import { Menu } from 'antd';
import { useHistory, useLocation, useRouteMatch } from 'react-router';
import { UserInfoContext } from '@src/common/common.context';
import { getGlobalRoutes } from '../../global.utils';

interface MenuInfo {
    key: string;
}

export default function Sidebar() {
    const userInfo = useContext(UserInfoContext);
    const location = useLocation();
    const match = useRouteMatch();
    const history = useHistory();

    const handleClick = (menuInfo: MenuInfo) => {
        history.replace(menuInfo.key);
    };

    const globalRoutes = getGlobalRoutes(userInfo.authType === 'ldap');

    return (
        <Menu
            onClick={handleClick}
            style={{ width: 256, marginRight: 32 }}
            selectedKeys={[location.pathname]}
            mode="inline"
        >
            {globalRoutes.map(route => (
                <Menu.Item key={`${match.path}/${route.path}`}>{route.label}</Menu.Item>
            ))}
        </Menu>
    );
}
