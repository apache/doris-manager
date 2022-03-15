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
