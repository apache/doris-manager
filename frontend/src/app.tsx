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
import { hot } from 'react-hot-loader/root';
import { BrowserRouter as Router, Switch } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ConfigProvider } from 'antd';
import zh from 'antd/lib/locale/zh_CN';
import en from 'antd/lib/locale/en_US';
import { RecoilRoot } from 'recoil';
import routes from './routes';

const languageMap = {
    zh,
    en,
};

const App = () => {
    const { i18n } = useTranslation();
    return (
        <ConfigProvider locale={languageMap[i18n.language]}>
            <RecoilRoot>{routes}</RecoilRoot>
        </ConfigProvider>
    );
};

export default hot(App);
