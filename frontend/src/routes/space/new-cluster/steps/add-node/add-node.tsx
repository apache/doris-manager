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

import { Button, Divider, Form, Input, message, Modal, PageHeader, Row, Space, Steps, Table } from 'antd';
import React, { useContext, useMemo, useRef, useState, useEffect } from 'react';
import ProCard from '@ant-design/pro-card';
import { NewSpaceInfoContext, UserInfoContext } from '@src/common/common.context';
import { NodeList } from './node-list/node-list';

const { TextArea } = Input;

export function AddNode(props: any) {
    const userInfo = useContext(UserInfoContext);
    const { reqInfo, form } = useContext(NewSpaceInfoContext);
    useEffect(() => {
        const { sshUser, sshPort, sshKey } = reqInfo.authInfo || {};
        form.setFieldsValue({
            ...form.getFieldsValue(),
            sshUser,
            sshPort,
            sshKey,
            hosts: reqInfo.hosts,
        });
    }, [form, reqInfo.authInfo, reqInfo.hosts]);
    return (
        <ProCard title={<h2>添加节点</h2>} headerBordered>
            <PageHeader className="site-page-header" title="SSH信任" style={{ paddingLeft: 0 }} />
            <span>
                请提前完成Manager节点与其他节点间SSH信任，并在下方填入Manager节点的SSH信息。<a>如何进行SSH信任？</a>
            </span>
            <Divider style={{ margin: 0, marginBottom: 24 }} />
            <Form form={form} name="basic" labelCol={{ span: 2 }} wrapperCol={{ span: 10 }} autoComplete="off">
                <Form.Item label="SSH用户" name="sshUser" rules={[{ required: true, message: '请输入SSH用户!' }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="SSH端口" name="sshPort" rules={[{ required: true, message: '请输入SSH端口!' }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="SSH私钥" name="sshKey" rules={[{ required: true, message: '请输入SSH私钥' }]}>
                    <TextArea rows={5} />
                </Form.Item>
                <PageHeader className="site-page-header" title="节点列表" style={{ paddingLeft: 0 }} />
                <Divider style={{ margin: 0, marginBottom: 24 }} />
                <Form.Item name="hosts" style={{ width: '100%' }} wrapperCol={{ span: 24 }}>
                    <NodeList />
                </Form.Item>
            </Form>
        </ProCard>
    );
}
