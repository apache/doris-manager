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
