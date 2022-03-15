import React, { Suspense, useEffect, useMemo, useState } from 'react';
import { Redirect, Switch, Route, useHistory } from 'react-router-dom';
import { Card } from 'antd';
import { useTranslation } from 'react-i18next';
import styles from './admin.module.less';
import { UserInfoContext } from '@src/common/common.context';
import { Sidebar } from '@src/components/sidebar/sidebar';
import { Header } from '@src/components/header/header';
import TabsHeader from '@src/components/tabs-header';
import { SpaceDetail } from '../space/detail/space-detail';
import { People } from './people/people';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import LoadingLayout from '@src/components/loading-layout';

export function Admin() {
    const {t} = useTranslation()
    const history = useHistory();
    const [loading, setLoading] = useState(true);
    const [userInfo] = useUserInfo();
    useEffect(() => {
        if (userInfo.id == null) return;
        if (userInfo.id != null && !userInfo.is_super_admin && !userInfo.is_admin) {
            history.push('/');
            return;
        }
        setLoading(false);
    }, [userInfo.id, userInfo.is_super_admin, userInfo.is_admin]);
    const tabRoutes = useMemo(
        () => [
            { label: t`spaceInfo`, path: `/admin/space/${userInfo.space_id}` },
            { label: t`members`, path: '/admin/people/user' },
            { label: t`roles`, path: '/admin/people/role' },
        ],
        [userInfo.space_id, t],
    );
    return (
        <UserInfoContext.Provider value={userInfo}>
            <Sidebar />
            <Header mode="normal" />
            <div className={styles.container}>
                <Card className={styles.card}>
                    <TabsHeader routes={tabRoutes} />
                    <Suspense
                        fallback={<LoadingLayout loading wrapperStyle={{ textAlign: 'center', marginTop: 200 }} />}
                    >
                        <LoadingLayout loading={loading} wrapperStyle={{ textAlign: 'center', marginTop: 200 }}>
                            <Switch>
                                <Route path="/admin/space/:spaceId" component={SpaceDetail} />
                                <Route path="/admin/people" component={People} />
                                <Redirect to={`/admin/space/${userInfo.space_id}`} />
                            </Switch>
                        </LoadingLayout>
                    </Suspense>
                </Card>
            </div>
        </UserInfoContext.Provider>
    );
}
