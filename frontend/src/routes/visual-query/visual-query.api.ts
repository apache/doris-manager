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

import { http, isSuccess } from '@src/utils/http';
import { DataType } from './types';

export function fetchDatabases(nsId: number) {
    return http.get(`/api/meta/nsId/${nsId}/databases`).then(res => res.data.filter((item: any) => item.name !== 'information_schema')) as Promise<any[]>;
}

interface SQLQueryParams {
    database: number;
    query: string;
}
// query
export function fetchData(params: SQLQueryParams) {
    return http.post('/api/query/sql/', params).then(res => res.data) as Promise<{
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
