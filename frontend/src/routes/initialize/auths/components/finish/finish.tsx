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

import { Result, Button } from 'antd';
import { useNavigate } from 'react-router';

export function AuthFinish(props: any) {
    const navigate = useNavigate();
    const authType = props.mode === 'ldap' ? 'LDAP' : '本地';
    return (
        <Result
            status="success"
            title={`恭喜！${authType}认证完成！`}
            extra={[
                <Button
                    type="primary"
                    key="go-login"
                    onClick={() => {
                        localStorage.setItem('initialized', 'true');
                        navigate('/passport/login');
                    }}
                >
                    跳转至登录页面
                </Button>,
            ]}
        />
    );
}
