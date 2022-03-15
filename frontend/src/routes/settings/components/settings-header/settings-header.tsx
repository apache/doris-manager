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

import { SettingOutlined } from '@ant-design/icons';
import { UserInfoContext } from '@src/common/common.context';
import { Menu, Space } from 'antd';
import React, { useContext, useState } from 'react';
import { useHistory, useRouteMatch } from 'react-router';
import { SettingsIcon } from '../../../../components/settings-icon/settings-icon';
import styles from './settings-header.less';

export function SettingsHeader(props: any) {
    const history = useHistory();
    const match = useRouteMatch();
    const [current, setCurrent] = useState(() => {
        let tab = 'user';
        ['global', 'user'].map(key => {
            if (history.location.pathname.includes(key)) {
                tab = key;
            }
        });
        return tab;
    });
    const userInfo = useContext(UserInfoContext);
    function handleClick(e) {
        setCurrent(e.key);
        if (e.key === 'space') {
            history.push(`${match.path}/space/${userInfo.space_id}`);
            return;
        }
        history.push(`${match.path}/${e.key}`);
    }
    return (
        <div className={styles['admin-header']} style={{ display: 'flex'}}>
            <div className={styles['admin-header-logo']} onClick={() => {
                history.push(`/space`);
            }}>
                <Space>
                    <SettingOutlined />
                    Palo Studio平台管理
                </Space>
            </div>
            <Menu theme="dark" onClick={handleClick} selectedKeys={[current]} mode="horizontal" className={styles['admin-header']}>
                <Menu.Item key="user">
                    用户
                </Menu.Item>
                <Menu.Item key="global">
                    平台设置
                </Menu.Item>
            </Menu>
            <div className={styles['palo-opt-box']} style={{marginRight: 20}}>
                <div>
                    <SettingsIcon context="global" />
                </div>
            </div>
        </div>
    );
}
