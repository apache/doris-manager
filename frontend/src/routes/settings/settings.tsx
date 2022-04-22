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

import { useEffect, useState } from 'react';
import { Card } from 'antd';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import styles from './settings.module.less';
import { UserInfoContext } from '@src/common/common.context';
import { Sidebar } from '@src/components/sidebar/sidebar';
import { Header } from '@src/components/header/header';
import TabsHeader from './components/tabs-header';
import { User } from './user/user';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import LoadingLayout from './global/components/loading-layout';

export function Settings() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [userInfo] = useUserInfo();
    useEffect(() => {
        if (userInfo.id == null) return;
        if (userInfo.id != null && !userInfo.is_super_admin) {
            navigate('/space');
            return;
        }
        setLoading(false);
    }, [userInfo.id]);
    return (
        <>
            <UserInfoContext.Provider value={userInfo}>
                <Sidebar />
                <Header mode="super-admin" />
                <div className={styles.container}>
                    <Card className={styles.card}>
                        <TabsHeader />
                        <LoadingLayout loading={loading} wrapperStyle={{ textAlign: 'center', marginTop: 200 }}>
                            <Routes>
                                <Route path="users/*" element={<User />} />
                                <Route path="/" element={<Navigate replace to="users" />} />
                            </Routes>
                        </LoadingLayout>
                    </Card>
                </div>
            </UserInfoContext.Provider>
        </>
    );
}
