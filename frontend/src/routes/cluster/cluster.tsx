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

import React, { useEffect, useMemo, useState } from 'react';
import { Steps } from 'antd';
import { Switch, Route, Redirect, useRouteMatch } from 'react-router';
import { useTranslation } from 'react-i18next';
import styles from './cluster.module.less';
import { UserInfoContext } from '@src/common/common.context';
import ClusterOverview from './overview';
import Nodes from './nodes';
import Configuration from './configuration';
import TabsHeader from '@src/components/tabs-header';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import LoadingLayout from '@src/components/loading-layout';

export function Cluster(props: any) {
    // const match = useRouteMatch();
    const { t } = useTranslation();
    const [loading, setLoading] = useState(true);
    const [userInfo] = useUserInfo();
    useEffect(() => {
        if (userInfo.id == null) return;
        setLoading(false);
    }, [userInfo.id]);
    const tabRoutes = useMemo(
        () => [
            { label: t`clusterOverview`, path: '/cluster/overview' },
            { label: t`nodeList`, path: '/cluster/nodes' },
            { label: t`parameterConf`, path: '/cluster/configuration' },
        ],
        [t],
    );
    return (
        <UserInfoContext.Provider value={userInfo}>
            <div className={styles.container}>
                <TabsHeader routes={tabRoutes} />
                <LoadingLayout loading={loading} wrapperStyle={{ textAlign: 'center', marginTop: 200 }}>
                    <Switch>
                        <Route path="/cluster/overview" component={ClusterOverview} />
                        <Route path="/cluster/nodes" component={Nodes} />
                        <Route path="/cluster/configuration" component={Configuration} />
                        <Redirect to="/cluster/overview" />
                    </Switch>
                </LoadingLayout>
            </div>
        </UserInfoContext.Provider>
    );
}
