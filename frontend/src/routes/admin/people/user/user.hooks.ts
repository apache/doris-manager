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

import { useCallback, useEffect } from 'react';
import { message } from 'antd';
import { useTranslation } from 'react-i18next';
import { useAsync } from '@src/hooks/use-async';
import { CommonAPI } from '@src/common/common.api';
import { isSuccess } from '@src/utils/http';
import { IUser } from './user';
import { getSpaceMembersAPI } from './user.api';

export function useSpaceMembers(userInfo: any) {
    const { t } = useTranslation();
    const { data: users, run: runGetUsers } = useAsync<IUser[]>({ loading: true, data: [] });

    const {
        data: spaceMembers,
        loading: loading,
        run: runGetSpaceMembers,
    } = useAsync<IUser[]>({ loading: true, data: [] });

    const getUsers = useCallback(() => {
        runGetUsers(
            CommonAPI.getUsers({ include_deactivated: false }).then(res => {
                if (isSuccess(res)) return res.data;
                return Promise.reject(res);
            }),
        ).catch(() => message.error(t`fetchUserListFailed`));
    }, [runGetUsers]);

    const getSpaceMembers = useCallback(() => {
        runGetSpaceMembers(getSpaceMembersAPI()).catch(() => {
            message.error(t`fetchSpaceMemberListFailed`);
        });
    }, [runGetSpaceMembers]);

    useEffect(() => {
        if (userInfo.space_id == null) return;
        getSpaceMembers();
        getUsers();
    }, [getSpaceMembers, userInfo.space_id]);

    return {
        users,
        spaceMembers,
        getSpaceMembers,
        loading,
    };
}
