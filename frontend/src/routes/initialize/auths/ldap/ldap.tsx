import { Steps } from "antd";
import { pathToRegexp } from "path-to-regexp";
import React, { useEffect, useState } from "react";
import { Redirect, Route, Switch, useHistory, useRouteMatch } from "react-router";
import { LDAPStepsEnum } from "../../initialize.data";
import { AuthFinish } from "../components/finish/finish";
import { LDAPAdminUser } from "./ldap-admin-user/ldap-admin-user";
import { LDAPConfig } from "./ldap-config/ldap-config";
const { Step } = Steps;

export function AuthLDAP(props: any) {
    const match = useRouteMatch();
    const [step, setStep] = useState(LDAPStepsEnum['ldap-info']);
    const history = useHistory();
    useEffect(() => {
        const regexp = pathToRegexp(`${match.path}/:step`);
        const paths = regexp.exec(history.location.pathname);
        const step = (paths as string[])[1];
        setStep(LDAPStepsEnum[step]);
    }, [history.location.pathname]);
    return (
        <div>
            <Steps current={step}>
                <Step title="LDAP认证" />
                <Step title="指定超级管理员" />
                <Step title="完成" />
            </Steps>
            <div style={{marginTop: 60}}>
                <Switch>
                    <Route path={`${match.path}/ldap-info`} component={LDAPConfig} />
                    <Route path={`${match.path}/admin-user`} component={LDAPAdminUser} />
                    <Route path={`${match.path}/finish`} render={() => <AuthFinish mode="ldap" />} />
                    <Redirect to={`${match.path}/ldap-info`} />
                </Switch>
            </div>
        </div>
    )
}
