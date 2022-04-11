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
import { Sidebar } from '@src/components/sidebar/sidebar';
import { useAuth } from '@src/hooks/use-auth';
import { useEffect } from 'react';
import { Route, Routes, useNavigate } from 'react-router';
import { InitializeAuth } from './auths/auth';
import { InitializeSetType } from './initialize-set-type';
import { LocalStepsEnum } from './initialize.data';
import styles from './initialize.less';

export function Initialize() {
    const navigate = useNavigate();
    const { initStep, authType: currentAuthType, initialized } = useAuth();

    useEffect(() => {
        if (currentAuthType && initStep) {
            const feStep = initStep ? initStep - 1 : 1;
            let stepPage = '';
            if (currentAuthType === AuthTypeEnum.STUDIO) {
                stepPage = LocalStepsEnum[feStep];
            }
            if (initialized) {
                if (currentAuthType === AuthTypeEnum.STUDIO) {
                    stepPage = LocalStepsEnum[feStep];
                    if (feStep === 1) {
                        navigate('/space');
                    }
                }
            } else {
                navigate(`auth/${currentAuthType}/${stepPage}`);
            }
        }
    }, [currentAuthType, initialized, initStep]);
    return (
        <div>
            <Sidebar mode="initialize" />
            <div style={{ marginLeft: 80 }}>
                <div className={styles['initialize-container']}>
                    <Routes>
                        <Route path="/" element={<InitializeSetType />} />
                        <Route path="auth/*" element={<InitializeAuth />} />
                    </Routes>
                </div>
            </div>
        </div>
    );
}
