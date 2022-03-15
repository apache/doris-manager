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
import React, { useEffect, useState } from "react";
import { Redirect, Route, Switch, useHistory, useRouteMatch } from "react-router";
import { pathToRegexp } from 'path-to-regexp';
import { StudioStepsEnum } from "../../initialize.data";
import { AdminUser } from "../components/admin-user/admin-user";
import { AuthFinish } from "../components/finish/finish";

const { Step } = Steps;

export function AuthStudio(props: any) {
    const match = useRouteMatch();
    const [step, setStep] = useState(StudioStepsEnum['admin-user']);
    const history = useHistory();
    useEffect(() => {
        const regexp = pathToRegexp(`${match.path}/:step`);
        const paths = regexp.exec(history.location.pathname);
        const step = (paths as string[])[1];
        setStep(StudioStepsEnum[step]);
    }, [history.location.pathname]);
    return (
        <div>
            <Steps current={step}>
                <Step title="本地认证" />
                <Step title="完成" />
            </Steps>
            <div style={{marginTop: 80}}>
                <Switch>
                    <Route path={`${match.path}/admin-user`} component={AdminUser} />
                    <Route path={`${match.path}/finish`} render={() => <AuthFinish mode="studio" />} />
                    <Redirect to={`${match.path}/admin-user`} />
                </Switch>
            </div>
        </div>
    )
}


