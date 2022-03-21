import { useEffect, useMemo } from 'react';
import { message } from 'antd';
import { useAsync } from '@src/hooks/use-async';
import { CascaderItem } from '../types';
import { fetchCollectionAPI } from '../visual-query.api';
import { getCollections } from '../utils';

export function useCollections() {
    const { data, loading, run: runFetchCollections } = useAsync<CascaderItem[]>({ data: [] });

    useEffect(() => {
        runFetchCollections(fetchCollectionAPI()).catch(() => {
            message.error('获取文件夹列表失败');
        });
    }, [runFetchCollections]);

    const collections = useMemo(() => {
        return getCollections(data as CascaderItem[]);
    }, [data]);

    return {
        loading,
        collections,
    };
}
