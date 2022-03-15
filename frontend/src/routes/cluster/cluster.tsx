import React, { useEffect, useMemo, useState } from 'react';
import ProCard from '@ant-design/pro-card';
import { Card, Steps } from 'antd';
import { Switch, Route, Redirect, useRouteMatch } from 'react-router';
import { useTranslation } from 'react-i18next';
import styles from './cluster.module.less';
import { UserInfoContext } from '@src/common/common.context';
import ClusterOverview from './overview';
import Nodes from './nodes';
import Configuration from './configuration';
import { ClusterList } from './list/list';
import { ClusterMonitor } from './monitor/monitor';
import TabsHeader from '@src/components/tabs-header';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import LoadingLayout from '@src/components/loading-layout';
const { Step } = Steps;

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
        // <>
        //     <Switch>
        //         <Route path={`${match.path}/list`} component={ClusterList} />
        //         <Route path={`${match.path}/monitor`} component={ClusterMonitor} />
        //         <Redirect to={`${match.path}/monitor`} />
        //     </Switch>
        // </>
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
