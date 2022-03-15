import { http, isSuccess } from '@src/utils/http';

function getUsers(data: { include_deactivated: boolean; cluster_id?: number }) {
    return http.get('/api/v2/user/', data);
}
function createUser(data: { name: string; email: string; password: string }) {
    return http.post('/api/v2/user/', data);
}
function updateUser(data: { name: string; user_id: number; email: string }) {
    return http.put(`/api/v2/user/${data.user_id}`, data);
}

function updateUserAdmin(data: { admin: boolean; user_id: number }) {
    return http.put(`/api/v2/user/${data.user_id}/admin`, { admin: data.admin });
}

function deactivateUser(data: { user_id: number }) {
    return http.delete(`/api/v2/user/${data.user_id}`);
}

function activateUser(data: { user_id: number }) {
    return http.put(`/api/v2/user/${data.user_id}/reactivate`);
}

function resetPassword(data: { user_id: number; password: string }) {
    return http.put(`/api/v2/user/${data.user_id}/password`, { password: data.password });
}

function syncLdapUser() {
    return http.get('/api/setting/syncLdapUser').then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export const UserAPI = {
    getUsers,
    createUser,
    updateUser,
    updateUserAdmin,
    deactivateUser,
    activateUser,
    resetPassword,
    syncLdapUser
};
