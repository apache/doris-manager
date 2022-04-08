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

import { AuthTypeEnum } from '@src/common/common.data';
import { InitializeAPI } from '@src/routes/initialize/initialize.api';
import { isSuccess } from '@src/utils/http';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';

export function useAuth() {
    const [initialized, setInitialized] = useState(false);
    const { pathname } = useLocation();
    const navigate = useNavigate();
    const [initStep, setInitStep] = useState(0);
    const [authType, setAuthType] = useState<AuthTypeEnum>();
    useEffect(() => {
        getInitProperties();
    }, []);

    async function getInitProperties() {
        const res = await InitializeAPI.getInitProperties();
        if (isSuccess(res)) {
            setInitStep(res.data.initStep);
            setAuthType(res.data.auth_type === 'studio' ? AuthTypeEnum.LOCAL : AuthTypeEnum.LDAP);
            if (res.data.completed) {
                localStorage.setItem('initialized', 'true');
                setInitialized(true);
            } else {
                localStorage.setItem('initialized', 'false');
                setInitialized(false);
                if (!pathname.includes('initialize')) {
                    navigate('/initialize');
                }
            }
        }
    }
    return {
        initialized,
        initStep,
        authType,
        getInitProperties,
    };
}
