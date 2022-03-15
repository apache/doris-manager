import { AuthTypeEnum } from '@src/common/common.data';
import { Sidebar } from '@src/components/sidebar/sidebar';
import { Header } from '@src/components/studio-header/header';
import { useAuth } from '@src/hooks/use-auth';
import React, { useEffect } from 'react';
import { Route, Redirect, useRouteMatch, Switch, useHistory } from 'react-router';
import { InitializeAuth } from './auths/auth';
import { InitializePage } from './initialize';
import { StudioStepsEnum, LDAPStepsEnum } from './initialize.data';
import styles from './initialize.less';

export function Initialize(props: any) {
    const match = useRouteMatch();
    const history = useHistory();
    const {initStep, authType: currentAuthType, initialized} = useAuth();

    useEffect(() => {
        if (currentAuthType && initStep) {
            const feStep = initStep ? initStep - 1 : 1;
            let stepPage = '';
            if (currentAuthType === AuthTypeEnum.STUDIO) {
                stepPage = StudioStepsEnum[feStep];
            } else if (currentAuthType === AuthTypeEnum.LDAP){
                stepPage = LDAPStepsEnum[feStep];
            }
            if (initialized) {
                if (currentAuthType === AuthTypeEnum.STUDIO) {
                    stepPage = StudioStepsEnum[feStep];
                    if (feStep === 1) {
                        history.push('/space');
                    }
                } else if (currentAuthType === AuthTypeEnum.LDAP){
                    stepPage = LDAPStepsEnum[feStep];
                    if (feStep === 2) {
                        history.push('/space');
                    }
                }
            } else {
                history.push(`${match.path}/auth/${currentAuthType}/${stepPage}`);
            }
        }
    }, [currentAuthType, initialized]);
    return (
        <div>
            <Sidebar mode="initialize" />
            <div style={{marginLeft: 80}}>
                <div className={styles['initialize-container']}>
                    <Switch>
                        <Route exact path={`${match.path}/`} component={InitializePage} />
                        <Route path={`${match.path}/auth`} component={InitializeAuth} />
                        <Redirect to={`${match.path}/`} />
                    </Switch>
                </div>
            </div>
        </div>
    );
}
