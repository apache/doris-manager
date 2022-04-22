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

import { Loading } from './components/loading';
import { NotFound } from './components/not-found';
import { Suspense } from 'react';

import { Route, BrowserRouter, Routes, Navigate } from 'react-router-dom';
import { Settings } from './routes/settings/settings';
import { Initialize } from './routes/initialize/initialize';
import { SuperAdminContainer } from './routes/super-admin-container';
import { Admin } from './routes/admin/admin';
import { Login } from './routes/passport/login';
import { Container } from './routes/container';
import { RequireInitialized } from './components/auths/require-initialized';
import { AuthRoute } from './components/auths/auth-route';

const routes = (
    <Suspense fallback={<Loading />}>
        <BrowserRouter>
            <Routes>
                <Route
                    path="/passport/login"
                    element={
                        <RequireInitialized>
                            <Login />
                        </RequireInitialized>
                    }
                />
                <Route path="/initialize/*" element={<Initialize />} />
                <Route
                    path="/space/*"
                    element={
                        <AuthRoute>
                            <SuperAdminContainer />
                        </AuthRoute>
                    }
                />
                <Route
                    path="/settings/*"
                    element={
                        <AuthRoute>
                            <Settings />
                        </AuthRoute>
                    }
                />
                <Route
                    path="/admin/*"
                    element={
                        <AuthRoute>
                            <Admin />
                        </AuthRoute>
                    }
                />
                <Route
                    path="/*"
                    element={
                        <AuthRoute>
                            <Container />
                        </AuthRoute>
                    }
                />
                <Route path="/" element={<Navigate to="space" replace />} />
                <Route path="*" element={<NotFound />} />
            </Routes>
        </BrowserRouter>
    </Suspense>
);

export default routes;
