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

import { CommonAPI } from '@src/common/common.api';
import { isSuccess } from '@src/utils/http';
import { Dispatch, SetStateAction, useState, useEffect } from 'react';

export function useUsers(): {
    users: any[];
    setUsers: Dispatch<SetStateAction<any[]>>;
    getUsers: () => void;
} {
    const [users, setUsers] = useState<any>();
    useEffect(() => {
        getUsers();
    }, []);

    async function getUsers() {
        const res = await CommonAPI.getUsers();
        if (isSuccess(res)) {
            setUsers(res.data);
        }
    }
    return { users, getUsers, setUsers };
}

export function useSpaceUsers(): {
    users: any[];
    setUsers: Dispatch<SetStateAction<any[]>>;
    getUsers: () => void;
} {
    const [users, setUsers] = useState<any>();
    useEffect(() => {
        getUsers();
    }, []);

    async function getUsers() {
        const res = await CommonAPI.getSpaceUsers()
        if (isSuccess(res)) {
            setUsers(res.data);
        }
    }
    return { users, getUsers, setUsers };
}
