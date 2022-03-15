import React from "react";
import { Redirect, Route, Switch, useRouteMatch } from "react-router";
import { AuthLDAP } from "./ldap/ldap";
import { AuthStudio } from "./studio/studio";

export function InitializeAuth(props: any) {
    const match = useRouteMatch();
    return (
        <Switch>
            <Route path={`${match.path}/ldap`} component={AuthLDAP} />
            <Route path={`${match.path}/studio`} component={AuthStudio} />
            <Redirect to={`${match.path}/studio`} />
        </Switch>
    )
}
