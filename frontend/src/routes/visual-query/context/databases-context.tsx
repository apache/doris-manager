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

import React, { PropsWithChildren, useEffect, useState } from 'react';
import { message } from 'antd';
import { fetchDatabases } from '../visual-query.api';
import { useAsync } from '../hooks';

interface DatabasesContextProps {
    databases: any[] | null;
    databasesLoading: boolean;
    selectedDatabaseId: number | null;
    setSelectedDatabaseId: (id: number) => void;
}

export const DatabasesContext = React.createContext<DatabasesContextProps>({
    databases: [],
    databasesLoading: true,
    selectedDatabaseId: null,
    setSelectedDatabaseId: () => {},
});

export default function QueryContextProvider(props: PropsWithChildren<{}>) {
    const {
        data: databases,
        loading: databasesLoading,
        run: runFetchDatabases,
    } = useAsync<any[]>({ loading: true, data: [] });
    const [selectedDatabaseId, setSelectedDatabaseId] = useState<number | null>(null);

    useEffect(() => {
        runFetchDatabases(fetchDatabases())
            .then(res => res && res.length > 0 && setSelectedDatabaseId(res[0].id))
            .catch(() => message.error('获取数据库列表失败'));
    }, [runFetchDatabases]);

    return (
        <DatabasesContext.Provider
            value={{
                databases,
                databasesLoading,
                selectedDatabaseId,
                setSelectedDatabaseId,
            }}
        >
            {props.children}
        </DatabasesContext.Provider>
    );
}
