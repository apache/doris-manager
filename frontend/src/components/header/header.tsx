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

import { Menu, Col, Row, Dropdown, Button } from 'antd';
import {SettingOutlined } from '@ant-design/icons';
import React, { useContext, useState } from 'react';
import { LayoutAPI } from './header.api';
import { useHistory } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import styles from './index.module.less';
import { UserInfoContext } from '@src/common/common.context';
import Swal from 'sweetalert2';
const VERSION = require('../../../package.json').version;

type HeaderMode = 'normal' | 'initialize' | 'super-admin';
interface HeaderProps {
    mode: HeaderMode;
}

export function Header(props: HeaderProps) {
    const { t, i18n } = useTranslation();
    const history = useHistory();
    const [statisticInfo, setStatisticInfo] = useState<any>({});
    const user = JSON.parse(window.localStorage.getItem('user') as string);
    const userInfo = useContext(UserInfoContext);
    function getCurrentUser() {
        LayoutAPI.getCurrentUser()
            .then(res => {
                window.localStorage.setItem('user', JSON.stringify(res.data))
                LayoutAPI.getSpaceName(res.data.space_id).then(res1 => {
                    setStatisticInfo(res1.data || {});
                })
            })
            .catch(err => {
                console.log(err);
            });
    }
    function onAccountSettings() {
        history.push( `/user-setting`);
    }
    function onLogout() {
        LayoutAPI.signOut()
            .then(res => {
                console.log(res)
                if (res.code === 0) {
                    localStorage.removeItem('login');
                    history.push(`/login`);
                }
            })

    }
    const menu = (
        <Menu>
            <Menu.Item style={{ padding: '10px 20px' }} onClick={onAccountSettings}>{t`accountSettings`}</Menu.Item>
            <Menu.Item
                onClick={() => {
                    Swal.fire({
                        width: '480px',
                        title: `<span style="font-size: 24px; color: #2e353b;">${t`Thanks for using`} Doris Manager!</span>`,
                        html: `<div style="margin: 4px 0;"><p style="font-size: 16px; font-weight: bold;">${t`Current Version`}: ${VERSION}</p><p style="color:#74838f; font-size: 14px">${t`Contact`}：dev@doris.apache.org</p></div>`,
                        footer: `<span style="font-size: 12px; color: rgb(116, 131, 143); padding: 0 0 20px 0; font-weight: bold;">Doris，${t`Born for data analysis`}</span>`,
                        imageUrl: '/src/assets/doris.png',
                        imageHeight: 68,
                        showConfirmButton: false,
                        imageAlt: 'Doris Manager',
                    });
                }}
                style={{ padding: '10px 20px' }}
            >
                {t`About`} Doris Manager
            </Menu.Item>
            <Menu.Item style={{ padding: '10px 20px' }} onClick={onLogout}>{t`Logout`}</Menu.Item>
        </Menu>
    );
    return (
        <div
            className={user && user?.is_super_admin ? styles['adminStyle']: styles['userStyle']}
            style={{ padding: 0, borderBottom: '1px solid #d9d9d9' }}
        > 
            <Row justify="end" align="middle" style={{paddingBottom: 8}}>
                {/* {
                    user && user.is_super_admin ? (
                        <div
                            className={styles['logo']}
                        />
                    ) :(
                        <Col style={{ marginLeft: '2em' }}>
                            <span>{t`namespace`}：{(userInfo as UserInfo)?.space_name}</span>
                        </Col>
                    )
                } */}
                 <Button
                    type="link"
                    onClick={() => {
                        i18n.changeLanguage(i18n.language === 'zh' ? 'en' : 'zh');
                    }}
                >
                    {i18n.language === 'zh' ? 'Switch to English' : '切换为中文'}
                </Button>
                <Col style={{ cursor: 'pointer', marginRight: 20, fontSize: 22}}>
                    <Dropdown overlay={menu} placement="bottomLeft">
                        <span onClick={e=> e.preventDefault()}>
                            <SettingOutlined />
                        </span>
                    </Dropdown>
                </Col>
            </Row>
        </div>
    );
}
