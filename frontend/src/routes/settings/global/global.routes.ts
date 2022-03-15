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

import PublicSharing from './routes/public-sharing';
import Localization from './routes/localization';
import Email from './routes/email';
import General from './routes/general';

export interface Route {
    path: string;
    label: string;
    component: () => JSX.Element;
}

export const DEFAULT_GLOBAL_ROUTES: Route[] = [
    {
        path: 'public_sharing',
        label: '公开分享',
        component: PublicSharing,
    },
    {
        path: 'localization',
        label: '本土化',
        component: Localization,
    },
    {
        path: 'email',
        label: '邮箱',
        component: Email,
    },
    {
        path: 'general',
        label: '访问与帮助',
        component: General,
    },
];
