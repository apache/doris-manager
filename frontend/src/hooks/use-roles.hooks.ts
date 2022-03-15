import { CommonAPI } from "@src/common/common.api";
import { IRole, IMember } from "@src/common/common.interface";
import { isSuccess } from "@src/utils/http";
import { Dispatch, SetStateAction, useState, useEffect } from "react";

export function useRoles(): {
    roles: IRole[];
    setRoles: Dispatch<SetStateAction<any[]>>;
    getRoles: () => void;
    loading: boolean;
} {
    const [roles, setRoles] = useState<IRole[]>([]);
    const [loading, setLoading] = useState(false);
    useEffect(() => {
        getRoles();
    }, []);

    async function getRoles() {
        setLoading(true);
        const res = await CommonAPI.getRoles();
        setLoading(false);
        if (isSuccess(res)) {
            setRoles(res.data);
        }
    }
    return {
        roles,
        setRoles,
        getRoles,
        loading
    };
}


export function useRoleMember(roleId: string): {
    members: IMember;
    setMembers: Dispatch<SetStateAction<IMember>>;
    getRoleMembers: () => void;
    loading: boolean;
} {
    const [members, setMembers] = useState<IMember>({name: '', id: 0, members: []});
    const [loading, setLoading] = useState(false);
    useEffect(() => {
        getRoleMembers();
    }, []);

    async function getRoleMembers() {
        setLoading(true);
        const res = await CommonAPI.getRoleMembersById({
            roleId: roleId
        });
        console.log(res);
        setLoading(false);
        if (isSuccess(res)) {
            setMembers(res.data);
        }
    }
    return {
        members,
        setMembers,
        getRoleMembers,
        loading
    };
}