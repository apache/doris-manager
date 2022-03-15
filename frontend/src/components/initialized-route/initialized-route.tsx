import { auth } from "@src/utils/auth";
import React from "react";
import { Redirect, Route } from "react-router";

export function InitializedRoute({ children, ...rest }) {
    const isPassportLogin = location.pathname.includes("/passport/login");
    return (
        <Route {...rest} render={props => {
            if (!auth.checkInitialized()) {
                return <Redirect to="/initialize" />;
            } else {
                return auth.checkLogin() || isPassportLogin ? children : <Redirect to="/passport/login" />;
            }
        }} />
    )
}
