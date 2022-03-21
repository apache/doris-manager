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
            title: item.display_name,
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
