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

import { isSuccess } from '@src/utils/http';
import { Dispatch, SetStateAction, useState, useEffect } from 'react';
import { UserAPI } from './user.api';

interface GetUsersParams {
    include_deactivated: boolean;
    cluster_id?: number;
}

export function useGlobalUsers(params: GetUsersParams): {
    users: any[];
    setUsers: Dispatch<SetStateAction<any[]>>;
    getUsers: (extraParams?: GetUsersParams) => Promise<void>;
    loading: boolean;
} {
    const [users, setUsers] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    useEffect(() => {
        getUsers();
    }, []);

    async function getUsers(extraParams?: GetUsersParams) {
        setLoading(true);
        const res = await UserAPI.getUsers({
            ...params,
            ...extraParams,
        });
        setLoading(false);
        if (isSuccess(res)) {
            setUsers(res.data);
        }
    }
    return {
        users,
        setUsers,
        getUsers,
        loading,
    };
}
