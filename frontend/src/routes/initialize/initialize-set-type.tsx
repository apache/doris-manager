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

import { AuthTypeEnum } from '@src/common/common.data';
import { isSuccess } from '@src/utils/http';
import { Button, Card, message, Radio, Row, Space } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import { InitializeAPI } from './initialize.api';
import styles from './initialize.less';

export function InitializeSetType() {
    console.log('hello');
    const [authType, setAuthType] = useState<AuthTypeEnum>(AuthTypeEnum.LOCAL);
    const navigate = useNavigate();
    async function handleSetAuthType() {
        const res = await InitializeAPI.setAuthType({ authType });
        if (isSuccess(res)) {
            navigate(authType);
        } else {
            message.error(res.msg);
        }
    }

    return (
        <div className={styles['initialize']}>
            <div className={styles['initialize-steps-content']}>
                <Card type="inner" title="管理用户">
                    <Radio.Group onChange={e => setAuthType(e.target.value)} value={authType}>
                        <Space direction="vertical">
                            <Radio value={AuthTypeEnum.LOCAL}>本地认证</Radio>
                        </Space>
                    </Radio.Group>
                    <p style={{ marginTop: 10 }}>注意，初始化选择好认证方式后不可再改变。</p>
                    <Row justify="end">
                        <Button type="primary" onClick={() => handleSetAuthType()}>
                            去配置
                        </Button>
                    </Row>
                </Card>
            </div>
        </div>
    );
}
