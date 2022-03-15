import { DEFAULT_GLOBAL_ROUTES, Route } from './global.routes';
import Certificate from './routes/certificate';

export function getValueFromJson(key: string, defaultValue?: any) {
    let res = defaultValue;
    try {
        res = JSON.parse(key);
    } catch (e) {}
    return res;
}

export function getProtocol(url: string) {
    const match = /^https?:\/\//.exec(url);
    return match ? match[0] : '';
}

export function getAddress(url: string) {
    const match = /^https?:\/\//.exec(url);
    return match ? url.slice(match[0].length) : url || '';
}

export function getGlobalRoutes(isLdap: boolean) {
    return [
        isLdap && {
            path: 'certificate',
            label: '认证',
            component: Certificate,
        },
        ...DEFAULT_GLOBAL_ROUTES,
    ].filter(Boolean) as Route[];
}
