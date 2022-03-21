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
