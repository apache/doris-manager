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

import { Navigate, Route, Routes } from 'react-router';
import { AccessCluster } from './access-cluster/access-cluster';
import { SpaceDetail } from './detail/space-detail';
import { SpaceList } from './list/list';
import { Logs } from './new-cluster/logs/logs';
import { NewCluster } from './new-cluster/new-cluster';

export function Space() {
    return (
        <Routes>
            <Route path="list" element={<SpaceList />} />
            <Route path={`new/:requestId/*`} element={<NewCluster />} />
            <Route path={`access/:requestId/*`} element={<AccessCluster />} />
            <Route path={`detail/:spaceId`} element={<SpaceDetail />} />
            <Route path={`logs/:taskId`} element={<Logs />} />
            <Route path="/" element={<Navigate replace to="list" />} />
        </Routes>
    );
}
