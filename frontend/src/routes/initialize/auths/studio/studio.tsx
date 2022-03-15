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


