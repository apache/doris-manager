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

import React, { Suspense, useEffect, useMemo, useState } from 'react';
import { Redirect, Switch, Route, useHistory } from 'react-router-dom';
import { Card } from 'antd';
import { useTranslation } from 'react-i18next';
import styles from './admin.module.less';
import { UserInfoContext } from '@src/common/common.context';
import { Sidebar } from '@src/components/sidebar/sidebar';
import { Header } from '@src/components/header/header';
import TabsHeader from '@src/components/tabs-header';
import { SpaceDetail } from '../space/detail/space-detail';
import { People } from './people/people';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import LoadingLayout from '@src/components/loading-layout';

export function Admin() {
    const {t} = useTranslation()
    const history = useHistory();
    const [loading, setLoading] = useState(true);
    const [userInfo] = useUserInfo();
    useEffect(() => {
        if (userInfo.id == null) return;
        if (userInfo.id != null && !userInfo.is_super_admin && !userInfo.is_admin) {
            history.push('/');
            return;
        }
        setLoading(false);
    }, [userInfo.id, userInfo.is_super_admin, userInfo.is_admin]);
    const tabRoutes = useMemo(
        () => [
            { label: t`spaceInfo`, path: `/admin/space/${userInfo.space_id}` },
            { label: t`members`, path: '/admin/people/user' },
            { label: t`roles`, path: '/admin/people/role' },
        ],
        [userInfo.space_id, t],
    );
    return (
        <UserInfoContext.Provider value={userInfo}>
            <Sidebar />
            <Header mode="normal" />
            <div className={styles.container}>
                <Card className={styles.card}>
                    <TabsHeader routes={tabRoutes} />
                    <Suspense
                        fallback={<LoadingLayout loading wrapperStyle={{ textAlign: 'center', marginTop: 200 }} />}
                    >
                        <LoadingLayout loading={loading} wrapperStyle={{ textAlign: 'center', marginTop: 200 }}>
                            <Switch>
                                <Route path="/admin/space/:spaceId" component={SpaceDetail} />
                                <Route path="/admin/people" component={People} />
                                <Redirect to={`/admin/space/${userInfo.space_id}`} />
                            </Switch>
                        </LoadingLayout>
                    </Suspense>
                </Card>
            </div>
        </UserInfoContext.Provider>
    );
}
