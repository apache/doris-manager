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

import React from 'react';
import styles from './container.less';

import { Layout } from 'antd';
import { Redirect, Route, Router, Switch, useHistory } from 'react-router-dom';
import { Sidebar } from '@src/components/sidebar/sidebar';
import { Header } from '@src/components/header/header';
import { CommonAPI } from '@src/common/common.api';
import { UserInfoContext } from '@src/common/common.context';
import { UserInfo } from '@src/common/common.interface';
import { Dashboard } from './dashboard/dashboard';
import { Meta } from './meta/meta';
import { NodeDashboard } from './node/dashboard';
import {NodeList} from './node/list'; 
import { Configuration } from './node/list/configuration';
import { FEConfiguration } from './node/list/fe-configuration';
import { BEConfiguration } from './node/list/be-configuration';
import { Query } from './query';
import { QueryDetails } from './query/query-details';
import { Cluster } from './cluster/cluster';
import { UserSetting } from './user-setting';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
export function Container(props: any) {
    const [userInfo] = useUserInfo();
    const history = useHistory();

    return (
        <Router history={history}>
            <UserInfoContext.Provider value={userInfo}>
                <Layout style={{ height: '100vh' }}>
                    <Layout>
                        <Sidebar width={200} />
                        <div className={styles['container']}>
                            <Header mode="normal"></Header>
                            <div className={styles['container-content']}>
                                <Switch>
                                    <Route path="/dashboard" component={Dashboard} />
                                    <Route path="/meta" component={Meta} />
                                    <Route path="/cluster" component={Cluster} />
                                    <Route path="/list" component={NodeList} />
                                    <Route path="/configuration/fe" component={FEConfiguration} />
                                    <Route path="/configuration/be" component={BEConfiguration} />
                                    <Route path="/configuration" component={Configuration} />
                                    <Route path="/node-dashboard" component={NodeDashboard} />
                                    <Route path="/query" component={Query} />
                                    <Route path="/details/:queryId" component={QueryDetails} />
                                    <Route path="/user-setting" component={UserSetting} />
                                    <Redirect to="/cluster" />
                                </Switch>
                            </div>
                        </div>
                    </Layout>
                </Layout>
            </UserInfoContext.Provider>
        </Router>
    );
}
