import { AuthTypeEnum } from '@src/common/common.data';
import { InitializeAPI } from '@src/routes/initialize/initialize.api';
import { isSuccess } from '@src/utils/http';
import { useEffect, useState } from 'react';
import { useHistory } from 'react-router';

export function useAuth() {
    const [initialized, setInitialized] = useState(false);
    const history = useHistory();
    const [initStep, setInitStep] = useState(0);
    const [authType, setAuthType] = useState();
    useEffect(() => {
        getInitProperties();
    }, []);

    async function getInitProperties() {
        const res = await InitializeAPI.getInitProperties();
        if (isSuccess(res)) {
            setInitStep(res.data.initStep);
            setAuthType(res.data.auth_type);
            if (res.data.completed) {
                localStorage.setItem('initialized', 'true');
                setInitialized(true);
            } else {
                localStorage.setItem('initialized', 'false');
                setInitialized(false);
                history.push('/initialize');
            }
        }
    }
    return {
        initialized,
        initStep,
        authType,
        getInitProperties,
    }
}