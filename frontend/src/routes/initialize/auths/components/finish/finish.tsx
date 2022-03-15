import { Result, Button } from 'antd';
import React from 'react';
import { useHistory } from 'react-router';

export function AuthFinish(props: any) {
    const history = useHistory();
    const authType = props.mode === 'ldap' ? 'LDAP' : '本地';
    return (
        <Result
            status="success"
            title={`恭喜！${authType}认证完成！`}
            extra={[
                <Button type="primary" key="go-login" onClick={() => {
                    localStorage.setItem('initialized', 'true');
                    history.push('/passport/login');
                }}>
                    跳转至登录页面
                </Button>,
            ]}
        />
    );
}
