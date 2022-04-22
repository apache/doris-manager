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

/** @format */

import { useState, useEffect } from 'react';
import styles from './table-content.module.less';
import CSSModules from 'react-css-modules';
import { Layout, Tabs } from 'antd';
import { TableOutlined } from '@ant-design/icons';
import { CommonHeader } from '@src/components/common-header/header';

import BaseInfo from './tabs/baseInfo';
import { Schema } from './schema/schema';
import { TableInfoTabTypeEnum } from './table-content.data';
import { Navigate, Route, Routes, useMatch, useNavigate } from 'react-router-dom';
import DataPreview from './tabs/data.pre';
const { Content } = Layout;
const iconTable = <TableOutlined />;
import { isTableIdSame } from '@src/utils/utils';
import { useTranslation } from 'react-i18next';
const { TabPane } = Tabs;

function TableContent(props: any) {
    const { t } = useTranslation();
    console.log(props);
    const navigate = useNavigate();
    const [id, setId] = useState(() => localStorage.getItem('table_id'));
    const [name, setName] = useState(() => localStorage.getItem('table_name'));
    const [dbId, setDbId] = useState<any>();
    const match = useMatch('/meta/table/:tableId/:tabType');
    useEffect(() => {
        setDbId(localStorage.getItem('database_id'));
        isTableIdSame();
    }, [window.location.href]);

    function refresh() {
        //
    }

    function handleTabChange(activeTab: string) {
        navigate(`${activeTab}`, {
            state: { id: id, name: name },
        });
    }

    return (
        <Content styleName="table-main">
            <CommonHeader title={name as string} icon={iconTable} callback={refresh}></CommonHeader>
            <div styleName="table-content">
                <Tabs
                    activeKey={match?.params.tabType ? match?.params.tabType : TableInfoTabTypeEnum.BasicInfo}
                    onChange={(activeTab: string) => handleTabChange(activeTab)}
                >
                    <TabPane tab={t`BasicInfo`} key={TableInfoTabTypeEnum.BasicInfo}></TabPane>
                    <TabPane tab={t`DataPreview`} key={TableInfoTabTypeEnum.DataPreview}></TabPane>
                    <TabPane tab="Schema" key={TableInfoTabTypeEnum.Schema}></TabPane>
                </Tabs>
                <Routes>
                    <Route
                        path={TableInfoTabTypeEnum.BasicInfo}
                        element={<BaseInfo tableId={match?.params.tableId} />}
                    />
                    <Route
                        path={TableInfoTabTypeEnum.DataPreview}
                        element={<DataPreview tableId={match?.params.tableId} />}
                    />
                    <Route path={TableInfoTabTypeEnum.Schema} element={<Schema tableId={match?.params.tableId} />} />
                    <Route path="/" element={<Navigate replace to={TableInfoTabTypeEnum.BasicInfo} />} />
                </Routes>
            </div>
        </Content>
    );
}

export default CSSModules(styles)(TableContent);
