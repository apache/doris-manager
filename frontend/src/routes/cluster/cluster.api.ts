import { http, isSuccess } from '@src/utils/http';

export function getClusterOverview() {
    return http.get('/api/cluster/overview').then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function startCluster(cluster_id: number) {
    return http.post('/api/control/cluster/start', { cluster_id }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function stopCluster(cluster_id: number) {
    return http.post('/api/control/cluster/stop', { cluster_id }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function restartCluster(cluster_id: number) {
    return http.post('/api/control/cluster/restart', { cluster_id }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function getNodeList(clusterId: number) {
    return http.get(`/api/control/cluster/${clusterId}/instances`).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

export function getConfigurationList(type: 'be' | 'fe') {
    return http.post(`/api/rest/v2/manager/node/configuration_info?type=${type}`, { type }).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}

interface ChangeConfigurationParams {
    node: string[];
    persist: 'true' | 'false';
    value: string;
}

export function changeConfiguration(type: 'be' | 'fe', data: Record<string, ChangeConfigurationParams>) {
    return http.post(`/api/rest/v2/manager/node/set_config/${type}`, data).then(res => {
        if (isSuccess(res)) return res.data;
        return Promise.reject(res);
    });
}
