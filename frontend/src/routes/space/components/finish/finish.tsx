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

import { PageContainer } from '@ant-design/pro-layout';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { Result, Button, Checkbox, Form, Input, Row, Space } from 'antd';
import React, { useContext } from 'react';
import { useHistory } from 'react-router';

export function SpaceCreateFinish(props: any) {
    const { form } = useContext(NewSpaceInfoContext);
    const history = useHistory();
    const onFinish = (values: any) => {
        console.log('Success:', values);
    };
    return (
        <PageContainer
            header={{
                title: <h2>完成创建</h2>,
            }}
        >
            <Result
                status="success"
                title={
                    <div>
                        <div>空间创建成功</div>
                        <div>请设定集群root密码</div>
                    </div>
                }
                extra={[
                    <>
                        <Form
                            form={form}
                            name="basic"
                            labelCol={{ span: 10 }}
                            wrapperCol={{ span: 5 }}
                            initialValues={{ remember: true }}
                            onFinish={onFinish}
                            autoComplete="off"
                        >
                            <Form.Item
                                label="集群密码"
                                name="clusterPassword"
                                rules={[{ required: true, message: '用于集群初始root与admin用户' }]}
                            >
                                <Input.Password />
                            </Form.Item>
                        </Form>
                    </>,
                ]}
            />
            ,
        </PageContainer>
    );
}
