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
import { Loading } from './components/loading';
import { NotFound } from './components/not-found';
import { Suspense } from 'react';


import { Route, Switch, BrowserRouter as Router } from 'react-router-dom';
import { InitializedRoute } from './components/initialized-route/initialized-route';
import { Settings } from './routes/settings/settings';
import { Initialize } from './routes/initialize/initialize.route';
import { SuperAdminContainer } from './routes/super-admin-container';
import { Admin } from './routes/admin/admin';
import { Login } from './routes/passport/login';
import { Container } from './routes/container';

const routes = (
    <Suspense fallback={<Loading />}>
        <Router>
            <Switch>
                <InitializedRoute path="/passport/login">
                    <Login />
                </InitializedRoute>
                <Route path="/initialize" component={Initialize} />
                <InitializedRoute path="/settings">
                    <Settings />
                </InitializedRoute>
                <InitializedRoute path="/space">
                    <SuperAdminContainer />
                </InitializedRoute>
                <InitializedRoute path="/admin">
                    <Admin />
                </InitializedRoute>
                <InitializedRoute path="/">
                    <Container />
                </InitializedRoute>
                <Route component={NotFound} />
            </Switch>
        </Router>
    </Suspense>
);

export default routes;
