import React from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import { UserList } from './list/list';

export function User(props: any) {
    const { match } = props;
    return (
        <>
            <Switch>
                <Route exact path={`${match.path}`} component={UserList} />
                <Redirect to={`${match.path}`} />
            </Switch>
        </>
    );
}
