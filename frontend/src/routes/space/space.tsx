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

