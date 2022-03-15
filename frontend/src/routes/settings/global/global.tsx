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
