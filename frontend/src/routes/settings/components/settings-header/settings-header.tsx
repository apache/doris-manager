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
