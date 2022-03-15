import { http } from '@src/utils/http';
import { IMember } from './common.interface';

function getUserInfo() {
    return http.get(`/api/v2/user/current`);
}

function getRoleMembersById(data: { roleId: string }) {
    return http.get<IMember>(`/api/permissions/group/${data.roleId}`);
}

function getRoles() {
    return http.get('/api/permissions/group');
}

function getUsers(params?: { include_deactivated: boolean }) {
    return http.get('/api/v2/user/', params);
}

function getSpaceUsers() {
    return http.get('/api/v2/user/space');
}

export const CommonAPI = {
    getUserInfo,
    getRoleMembersById,
    getRoles,
    getUsers,
    getSpaceUsers,
};
