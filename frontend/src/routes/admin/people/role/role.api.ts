import { IRole } from "@src/common/common.interface";
import { http } from "@src/utils/http";

function createRole(data: {name: string}) {
    return http.post('/api/permissions/group', data)
}

function updateRole(data: {id: number, name: string}) {
    return http.put(`/api/permissions/group/${data.id}`, {name: data.name})
}

function deleteRole(data: {roleId: number}) {
    return http.delete(`/api/permissions/group/${data.roleId}`)
}

function deleteMember(data: {membership_id: number}) {
    return http.delete(`/api/permissions/membership/${data.membership_id}`)
}

function addMember(data: {group_id: number, user_ids: number[]}) {
    return http.post('/api/permissions/memberships', data)
}

export const RoleAPI = {
    createRole,
    updateRole,
    deleteRole,
    addMember,
    deleteMember
}