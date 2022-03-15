import React from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import { RoleList } from './list/list';
import { RoleMembers } from './member/member';

export function Role(props: any) {
    const { match } = props;
    return (
        <>
            <Switch>
                <Route exact path={`${match.path}`} component={RoleList} />
                <Route path={`${match.path}/:roleId`} component={RoleMembers} />
                <Redirect to={`${match.path}`} />
            </Switch>
        </>
    );
}
