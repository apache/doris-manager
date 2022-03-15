// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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
