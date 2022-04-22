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

import React, { PropsWithChildren, useEffect, useState, useMemo } from 'react';
import { useAsync } from '../hooks';
import { DataType } from '../types';

export interface Column {
    dataIndex: string;
    title: string;
}

interface ResultData {
    data: DataType;
    error?: any;
    database_id: number;
}

interface DataContextProps {
    columns: Column[];
    dataSource: Record<string, any>[];
    setColumns: (val: Column[]) => void;
    data: ResultData | null;
    dataLoading: boolean;
    dataError: Error | null;
    runFetchData: (promise: Promise<ResultData>) => Promise<ResultData>;
}

export const DataContext = React.createContext<DataContextProps>({
    columns: [],
    dataSource: [],
    setColumns: () => {},
    data: null,
    dataLoading: false,
    dataError: null,
    runFetchData: () => Promise.resolve({ data: { cols: [], rows: [], native_form: { query: '' } }, database_id: 0 }),
});

export default function DataContextProvider(props: PropsWithChildren<{}>) {
    const [columns, setColumns] = useState<{ dataIndex: string; title: string }[]>([]);
    const { data: resultData, loading: dataLoading, error: dataError, run: runFetchData } = useAsync<ResultData>();

    const isFetchingError = dataError != null || (resultData && resultData.error);

    const dataSource = useMemo(() => {
        if (!resultData || isFetchingError) return [];
        return resultData.data.rows.map((item, index) => {
            return columns.reduce(
                (memo, current) => {
                    const columnIndex = resultData.data.cols.findIndex(col => col.name === current.dataIndex);
                    memo[current.dataIndex] = item[columnIndex];
                    return memo;
                },
                { key: index } as Record<string, any>,
            );
        });
    }, [resultData, columns, isFetchingError]);

    useEffect(() => {
        if (isFetchingError || resultData == null) {
            setColumns([]);
            return;
        }
        const newColumns = resultData.data.cols.map(item => ({
            title: item.displayName,
            dataIndex: item.name,
        }));
        setColumns(newColumns);
    }, [resultData, isFetchingError]);

    return (
        <DataContext.Provider
            value={{
                columns,
                dataSource,
                setColumns,
                data: resultData,
                dataLoading,
                dataError,
                runFetchData,
            }}
        >
            {props.children}
        </DataContext.Provider>
    );
}
