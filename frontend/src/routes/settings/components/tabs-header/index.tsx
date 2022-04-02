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

import { useLocation, useNavigate } from 'react-router-dom';
import { Tabs } from 'antd';
import { useTranslation } from 'react-i18next';

const { TabPane } = Tabs;

export default function TabsHeader() {
    const { pathname } = useLocation();
    const navigate = useNavigate();

    const { t } = useTranslation();

    const handleTabChange = (key: string) => {
        navigate(key);
    };

    return (
        <Tabs onChange={handleTabChange} defaultActiveKey={pathname}>
            <TabPane tab={t`userManagement`} key="/settings/user" />
        </Tabs>
    );
}
