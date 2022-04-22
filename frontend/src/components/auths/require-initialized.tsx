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

import { Navigate, useLocation } from 'react-router';
import { dorisAuthProvider } from './doris-auth-provider';

export function RequireInitialized({ children }: { children: JSX.Element }) {
    const location = useLocation();

    if (!dorisAuthProvider.checkInitialized()) {
        // Redirect them to the /initialize page, but save the current location they were
        // trying to go to when they were redirected. This allows us to send them
        // along to that page after they initialize, which is a nicer user experience
        // than dropping them off on the home page.
        return <Navigate to="/initialize" state={{ from: location }} replace />;
    }

    return children;
}
