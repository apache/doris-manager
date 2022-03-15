import React from 'react';
import { Role } from './role/role';
import { Redirect, Route, Switch } from 'react-router-dom';
import { User } from './user/user';

export function People(props: any) {
    const { match } = props;
    console.log(match)
    return (
        <>
            <Switch>
                <Route path={`${match.path}/user`} component={User} />
                <Route path={`${match.path}/role`} component={Role} />
                <Redirect to={`${match.path}/role`} />
            </Switch>
        </>
    );
}
