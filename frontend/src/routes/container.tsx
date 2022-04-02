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

import styles from './container.less';

import { Layout } from 'antd';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Sidebar } from '@src/components/sidebar/sidebar';
import { Header } from '@src/components/header/header';
import { UserInfoContext } from '@src/common/common.context';
import { Meta } from './meta/meta';
import { Cluster } from './cluster/cluster';
import { UserSetting } from './user-setting';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import VisualQuery from './visual-query/visual-query';

export function Container() {
    const [userInfo] = useUserInfo();

    return (
        <UserInfoContext.Provider value={userInfo}>
            <Layout style={{ height: '100vh' }}>
                <Layout>
                    <Sidebar width={200} />
                    <div className={styles['container']}>
                        <Header mode="normal"></Header>
                        <div className={styles['container-content']}>
                            <Routes>
                                <Route path="/meta/*" element={<Meta />} />
                                <Route path="/cluster/*" element={<Cluster />} />
                                <Route path="/user-setting/*" element={<UserSetting />} />
                                <Route path="/visual-query/*" element={<VisualQuery />} />
                                <Route path="/" element={<Navigate replace to="meta" />} />
                            </Routes>
                        </div>
                    </div>
                </Layout>
            </Layout>
        </UserInfoContext.Provider>
    );
}
