import { UserInfoContext } from '@src/common/common.context';
import { Header } from '@src/components/header/header';
import { Sidebar } from '@src/components/sidebar/sidebar';
import { useUserInfo } from '@src/hooks/use-userinfo.hooks';
import React from 'react';
import { Redirect, Route, Switch, useRouteMatch } from 'react-router-dom';
import { Space } from './space/space';

export function SuperAdminContainer(props: any) {
    const match = useRouteMatch();
    const [userInfo] = useUserInfo();

    return (
        <div>
            <UserInfoContext.Provider value={userInfo}>
                <Sidebar />
                <div style={{ marginLeft: 80 }}>
                    <Header mode="super-admin" />
                    <div>
                        <Switch>
                            <Route path={`${match.path}`} component={Space} />
                            <Redirect to={`${match.path}`} />
                        </Switch>
                    </div>
                </div>
            </UserInfoContext.Provider>
        </div>
    );
}
