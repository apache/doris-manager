import { CommonAPI } from '@src/common/common.api';
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
