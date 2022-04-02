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
import { SettingOutlined } from '@ant-design/icons';
import { useContext } from 'react';
import { LayoutAPI } from './header.api';
import { useTranslation } from 'react-i18next';
import styles from './index.module.less';
import { UserInfoContext } from '@src/common/common.context';
import Swal from 'sweetalert2';
import { useNavigate } from 'react-router';
import { VERSION } from '@src/config';

export function Header() {
    const { t, i18n } = useTranslation();
    const navigate = useNavigate();
    const user = useContext(UserInfoContext);
    function onAccountSettings() {
        navigate(`/user-setting`);
    }
    function onLogout() {
        LayoutAPI.signOut().then(res => {
            console.log(res);
            if (res.code === 0) {
                localStorage.removeItem('login');
                navigate(`/login`);
            }
        });
    }
    const menu = (
        <Menu>
            <Menu.Item
                key="account_setting"
                style={{ padding: '10px 20px' }}
                onClick={onAccountSettings}
            >{t`accountSettings`}</Menu.Item>
            <Menu.Item
                key="about"
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
            <Menu.Item key="logout" style={{ padding: '10px 20px' }} onClick={onLogout}>{t`Logout`}</Menu.Item>
        </Menu>
    );
    return (
        <div
            className={user && user?.is_super_admin ? styles['adminStyle'] : styles['userStyle']}
            style={{ padding: 0, borderBottom: '1px solid #d9d9d9' }}
        >
            <Row justify="end" align="middle" style={{ paddingBottom: 8 }}>
                <Button
                    type="link"
                    style={{ marginTop: 2 }}
                    onClick={() => {
                        i18n.changeLanguage(i18n.language === 'zh' ? 'en' : 'zh');
                    }}
                >
                    {i18n.language === 'zh' ? 'Switch to English' : '切换为中文'}
                </Button>
                <Col style={{ cursor: 'pointer', marginRight: 20, fontSize: 22 }}>
                    <Dropdown overlay={menu} placement="bottomLeft">
                        <span onClick={e => e.preventDefault()}>
                            <SettingOutlined />
                        </span>
                    </Dropdown>
                </Col>
            </Row>
        </div>
    );
}
