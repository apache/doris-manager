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

import { Divider, Form, Input, PageHeader } from 'antd';
import React, { useContext, useEffect } from 'react';
import ProCard from '@ant-design/pro-card';
import { NewSpaceInfoContext } from '@src/common/common.context';
import TextArea from 'antd/lib/input/TextArea';

export function ManagedOptions(props: any) {
    const { form, reqInfo } = useContext(NewSpaceInfoContext);
    useEffect(() => {
        form.setFieldsValue({...reqInfo.authInfo});
    }, [reqInfo.cluster_id]);
    return (
        <ProCard title={<h2>托管选项</h2>} headerBordered>
            <PageHeader className="site-page-header" title="SSH信任" style={{ paddingLeft: 0 }} />
            <span>
                请提前完成Manager节点与其他节点间SSH信任，并在下方填入Manager节点的SSH信息。<a>如何进行SSH信任？</a>
            </span>
            <Divider style={{ margin: 0, marginBottom: 24 }} />
            <Form
                form={form}
                name="basic"
                labelCol={{ span: 2 }}
                wrapperCol={{ span: 10 }}
                initialValues={{
                    user: reqInfo.authInfo?.sshUser,
                    sshPort: reqInfo.authInfo?.sshPort,
                    sshKey: reqInfo.authInfo?.sshKey,
                }}
                autoComplete="off"
            >
                <Form.Item label="SSH用户" name="sshUser" rules={[{ required: true, message: '请输入SSH用户!' }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="SSH端口" name="sshPort" rules={[{ required: true, message: '请输入SSH端口!' }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="SSH私钥" name="sshKey" rules={[{ required: true, message: '请输入SSH私钥' }]}>
                    <TextArea rows={5} />
                </Form.Item>
            </Form>
            <PageHeader className="site-page-header" title="指定安装路径" style={{ paddingLeft: 0 }} />
            <div>
                <p>Doris与Doris Manager Agent将安装至该目录下。请确保该目录为Doris及相关组件专用。</p>
            </div>
            <Form
                form={form}
                name="basic"
                labelCol={{ span: 2 }}
                wrapperCol={{ span: 10 }}
                initialValues={{
                    installInfo: reqInfo.installInfo,
                }}
                autoComplete="off"
            >
                <Form.Item label="安装路径" name="installInfo" rules={[{ required: true, message: '请输入安装路径' }]}>
                    <Input />
                </Form.Item>
            </Form>
        </ProCard>
    );
}
