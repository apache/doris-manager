import { CommonAPI } from '@src/common/common.api';
import { InitializeAPI } from '@src/routes/initialize/initialize.api';
import { isSuccess } from '@src/utils/http';
import { Dispatch, SetStateAction, useState, useEffect } from 'react';

export function useLDAPUsers(): {
    ldapUsers: any[];
    setLDAPUsers: Dispatch<SetStateAction<any[]>>;
    getLDAPUsers: () => void;
} {
    const [ldapUsers, setLDAPUsers] = useState<any>();
    useEffect(() => {
        getLDAPUsers();
    }, []);

    async function getLDAPUsers() {
        const res = await InitializeAPI.getLDAPUser();
        if (isSuccess(res)) {
            setLDAPUsers(res.data);
        }
    }
    return { ldapUsers, getLDAPUsers, setLDAPUsers };
}
