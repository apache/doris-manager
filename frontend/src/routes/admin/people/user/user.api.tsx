import { http, isSuccess } from '@src/utils/http';

export function getSpaceMembersAPI() {
    return http.get(`/api/v2/user/space`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function removeMemberFromSpaceAPI(userId: number) {
    return http.delete(`/api/v2/user/move/${userId}`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function addMemberToSpaceAPI(userId: number) {
    return http.post(`/api/v2/user/add/${userId}`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}