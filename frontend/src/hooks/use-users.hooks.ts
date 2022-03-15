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
