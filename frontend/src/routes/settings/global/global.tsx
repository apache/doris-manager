import React, { useContext, useEffect, useState } from 'react';
import { Typography, Row } from 'antd';
import { Switch, Route, Redirect, useRouteMatch } from 'react-router-dom';
import styles from './style.module.less';
import Sidebar from './components/sidebar';
import GlobalSettingProvider from './context';
import { UserInfoContext } from '@src/common/common.context';
import LoadingLayout from './components/loading-layout';
import { getGlobalRoutes } from './global.utils';

export function Global() {
    const [loading, setLoading] = useState(true);
    const match = useRouteMatch();
    const userInfo = useContext(UserInfoContext);
    const isLdap = userInfo.authType === 'ldap';

    useEffect(() => {
        if (userInfo.id != null) {
            setLoading(false);
        }
    }, [userInfo.id]);

    const globalRoutes = getGlobalRoutes(isLdap);

    return (
        <GlobalSettingProvider>
            <div className={styles.container}>
                <LoadingLayout loading={loading} wrapperStyle={{ textAlign: 'center', marginTop: 300 }}>
                    <Row style={{ marginBottom: 32 }}>
                        <Typography.Title level={4}>设置</Typography.Title>
                    </Row>
                    <div className={styles.main}>
                        <Sidebar />
                        <Switch>
                            {globalRoutes.map(route => (
                                <Route
                                    key={route.path}
                                    path={`${match.path}/${route.path}`}
                                    component={route.component}
                                />
                            ))}
                            <Redirect to={`${match.path}/${isLdap ? 'certificate' : 'public_sharing'}`} />
                        </Switch>
                    </div>
                </LoadingLayout>
            </div>
        </GlobalSettingProvider>
    );
}
