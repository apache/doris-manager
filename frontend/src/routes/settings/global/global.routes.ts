import PublicSharing from './routes/public-sharing';
import Localization from './routes/localization';
import Email from './routes/email';
import General from './routes/general';

export interface Route {
    path: string;
    label: string;
    component: () => JSX.Element;
}

export const DEFAULT_GLOBAL_ROUTES: Route[] = [
    {
        path: 'public_sharing',
        label: '公开分享',
        component: PublicSharing,
    },
    {
        path: 'localization',
        label: '本土化',
        component: Localization,
    },
    {
        path: 'email',
        label: '邮箱',
        component: Email,
    },
    {
        path: 'general',
        label: '访问与帮助',
        component: General,
    },
];
