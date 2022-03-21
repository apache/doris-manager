import { http, isSuccess } from '@src/utils/http';
import { DataType } from './types';

export function fetchDatabases() {
    return http.get('/api/database/', { include: 'tables' }).then(res => res.data) as Promise<any[]>;
}

interface NativeSqlQueryParams {
    database: number;
    native: { query: string };
    type: 'NATIVE';
}

export function fetchData(params: NativeSqlQueryParams) {
    return http.post('/api/dataset/', params).then(res => res.data) as Promise<{
        data: DataType;
        error?: any;
        database_id: number;
    }>;
}

export function fetchCollectionAPI() {
    return http.get('/api/collection/').then(res => {
        if (isSuccess(res)) {
            return res.data;
        }
        return Promise.reject(res);
    });
}

interface AddCardParams {
    collection_id: number;
    dataset_query: {
        database: number;
        native: {
            query: string;
        };
        type: 'NATIVE';
    };
    description: string | null;
    display: 'table' | 'chart';
    name: string;
    result_metadata: {
        columns: any[];
    };
    original_definition: {
        database: number;
        native: {
            query: string;
        };
        type: 'NATIVE';
    };
    visualization_settings: any;
}

export function addCardAPI(params: AddCardParams) {
    return http.post('/api/card/', params).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

interface FetchChildOfCollectionParams {
    collectionId: number;
    model: 'collection' | 'dashboard' | 'card';
}

export function fetchChildOfCollectionAPI(params: FetchChildOfCollectionParams) {
    const { collectionId, ...restParams } = params;
    return http.get(`/api/collection/${collectionId}/items`, restParams).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}
