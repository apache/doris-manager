import logo from '@assets/logo_nav.png';
import React, { useEffect, useMemo, useState } from 'react';
import styles from './index.module.less';
// import queryString from 'query-string';
import { Anchor, Col, Dropdown, Input, Layout, Menu, message, Row, Tooltip } from 'antd';
import {
    AppstoreAddOutlined,
    LeftOutlined,
    SearchOutlined,
} from '@ant-design/icons';
import { Link, RouteComponentProps, useHistory, useRouteMatch } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { SettingsIcon } from '../settings-icon/settings-icon';
import { auth } from '@src/utils/auth';

type HeaderMode = 'normal' | 'initialize' | 'super-admin';
interface HeaderProps {
    mode: HeaderMode;
}

export function Header(props: HeaderProps) {
    const { t } = useTranslation();
    const history = useHistory();
    // const { q } = queryString.parse(history.location.search) as { q: string };
    const { mode = 'normal' } = props;
    const showHeaderFuncs = mode !== 'initialize' && mode !== 'super-admin';
    const isHistoryQueryPage = history.location.pathname.includes('history-query');

    const initialized = auth.checkInitialized();

    // const searchBar = useMemo(
    //     () => (
    //         <Input
    //             key={q}
    //             placeholder={t`search`}
    //             prefix={<SearchOutlined className={styles['search-icon']} />}
    //             defaultValue={q}
    //             onPressEnter={val => {
    //                 history.push({ pathname: '/search', search: `?q=${val.target.value}` });
    //             }}
    //         />
    //     ),
    //     [q],
    // );
    return (
        <div className={styles['palo-header']}>
            <div className={styles['palo-logo']} onClick={() => {
                history.push(`/space`);
            }}>
                <img src={logo} alt="" />
            </div>
            {/* {showHeaderFuncs && <div className={styles['palo-search']}>{searchBar}</div>} */}
            <div className={styles['palo-opt-box']}>
                {/* {showHeaderFuncs && (
                    <div>
                        <Tooltip placement="bottom" title={t`workspace`}>
                            <AppstoreAddOutlined
                                className={styles['icon']}
                                onClick={() => {
                                    history.push(`/collection`);
                                }}
                            />
                        </Tooltip>
                    </div>
                )} */}
                {/* <div>
                    <ContainerOutlined
                        className={styles['icon']}
                        onClick={() => (window.location.href = `${window.location.origin}`)}
                    />
                    <span className={styles['icon-tip']}>创建查询</span>
                </div>
                <div>
                    <Tooltip placement="bottom" title={t`databse`}>
                        <HddOutlined
                            className={styles['icon']}
                            onClick={() => (window.location.href = `${window.location.origin}/new-studio/browse`)}
                        />
                    </Tooltip>
                </div>
                <div>
                    <Tooltip placement="bottom" title={t`workspace`}>
                        <AppstoreAddOutlined
                            className={styles['icon']}
                            onClick={() => (window.location.href = `${window.location.origin}/new-studio/`)}
                        />
                    </Tooltip>
                </div>
                {statisticInfo.manager_enable && (
                         <div>
                            <Tooltip placement="bottom" title={"运维监控"}>
                                <DesktopOutlined
                                    className={styles['icon']}
                                    onClick={() => (window.location.href = `${window.location.origin}/d-stack`)}
                                />
                            </Tooltip>
                        </div>
                    )
                }
                <div>
                    <Tooltip placement="bottom" title={t`help`}>
                        <QuestionCircleOutlined
                            className={styles['icon']}
                            onClick={() => {
                                const analyticsUrl = `${window.location.origin}/docs/pages/产品概述/产品介绍.html`;
                                window.open(analyticsUrl);
                            }}
                        />
                    </Tooltip>
                </div> */}
                {showHeaderFuncs && isHistoryQueryPage && (
                    <div>
                        <Tooltip placement="bottom" title={t`backTo` + 'Studio'}>
                            <LeftOutlined
                                className={styles['icon']}
                                onClick={() =>
                                    (window.location.href = `${window.location.origin}/question#eyJkYXRhc2V0X3F1ZXJ5Ijp7ImRhdGFiYXNlIjpudWxsLCJuYXRpdmUiOnsicXVlcnkiOiIiLCJ0ZW1wbGF0ZS10YWdzIjp7fX0sInR5cGUiOiJOQVRJVkUifSwiZGlzcGxheSI6InRhYmxlIiwidmlzdWFsaXphdGlvbl9zZXR0aW5ncyI6e319`)
                                }
                            />
                        </Tooltip>
                    </div>
                )}
                {initialized && (
                    <div>
                        <SettingsIcon mode="super-admin" />
                    </div>
                )}
            </div>
        </div>
    );
}
