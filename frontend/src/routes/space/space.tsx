import React from 'react';
import { Switch, Route, Redirect, useRouteMatch } from 'react-router';
import { SpaceList } from './list/list';
import { NewCluster } from './new-cluster/new-cluster';
import { Logs } from './new-cluster/logs/logs'
import { SpaceDetail } from './detail/space-detail';
import { AccessCluster } from './access-cluster/access-cluster';

export function Space() {
    const match = useRouteMatch();
    return (
        <>
            <Switch>
                <Route path={`${match.path}/list`} component={SpaceList} />
                <Route path={`${match.path}/new/:requestId`} component={NewCluster} />
                <Route path={`${match.path}/access/:requestId`} component={AccessCluster} />
                <Route path={`${match.path}/detail/:spaceId`} component={SpaceDetail} />
                <Route path={`${match.path}/logs/:taskId`} component={Logs} />
                <Redirect to={`${match.path}/list`} />
            </Switch>
        </>
    );
}

