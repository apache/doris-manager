import { http } from '@src/utils/http';
import { GlobalSettingItem } from './types';

export function fetchGlobalSettingsApi() {
    return http.get('/api/setting/global').then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    }) as Promise<GlobalSettingItem[]>;
}

export interface RemoteSettingParams extends GlobalSettingItem {
    type?: string;
}

export function changeSettingApi(key: string, params: RemoteSettingParams) {
    return http.put(`/api/setting/${key}`, params).then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function changeEmailSettingApi(params: Record<string, string>) {
    return http.put('/api/email/', params).then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function deleteEmailSettingApi() {
    return http.delete('/api/email/').then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function sendTestEmailApi(params: { email: string }) {
    return http.post('/api/email/test/', params).then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}

export function getLdapSettingsApi() {
    return http.get('/api/ldap/setting').then(res => {
        if (res.code === 0) return res.data;
        return Promise.reject(res);
    });
}
