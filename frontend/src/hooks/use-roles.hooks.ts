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