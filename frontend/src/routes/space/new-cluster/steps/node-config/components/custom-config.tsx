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

import React from 'react';
import { Input, Row, Space, Switch } from 'antd';
import { CustomConfigProps } from './custom-config.interface';

export function CustomConfig(props: CustomConfigProps) {
    const { dispatch, activeKey, custom } = props;
    return (
        <>
            <Row style={{ margin: '20px 0' }}>
                <Space>
                    <h3>自定义配置</h3>
                    <Switch
                        style={{ marginLeft: 40 }}
                        checkedChildren="开"
                        unCheckedChildren="关"
                        checked={custom.visible}
                        onChange={(visible: boolean) => {
                            dispatch({
                                type: activeKey,
                                payload: {
                                    ...custom,
                                    visible,
                                },
                            });
                        }}
                    />
                </Space>
            </Row>
            {custom.visible && (
                <Input.TextArea
                    value={custom.value}
                    onChange={e => {
                        dispatch({
                            type: activeKey,
                            payload: {
                                ...custom,
                                value: e.target.value,
                            },
                        });
                    }}
                    rows={5}
                    placeholder="请输入您的自定义配置项，若存在冲突，自定义配置将覆盖上方默认配置 格式: xxx=xxx"
                />
            )}
        </>
    );
}
