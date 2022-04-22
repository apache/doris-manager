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

import React, { useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { useNavigate, useParams } from 'react-router';
import API from '../new-cluster.api';
import { Button } from 'antd';

export function Logs() {
    const navigate = useNavigate();
    const params = useParams<{ taskId: string }>();
    const [logText, setlogText] = useState('');

    useEffect(() => {
        API.getTaskLog(params.taskId as string).then(res => {
            setlogText(res.data.log);
        });
    }, []);
    return (
        <PageContainer
            header={{
                title: '日志',
            }}
            extra={[
                <Button
                    key="1"
                    type="primary"
                    onClick={() => {
                        navigate(-1);
                    }}
                >
                    返回
                </Button>,
            ]}
        >
            <div style={{ width: '100%', background: '#000', color: '#fff', height: '80vh', overflow: 'scroll' }}>
                <code>{logText}</code>
            </div>
        </PageContainer>
    );
}
