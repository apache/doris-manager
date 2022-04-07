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

import { Form, Input, PageHeader } from 'antd';
import React, { useContext } from 'react';
import ProCard from '@ant-design/pro-card';
import { NewSpaceInfoContext } from '@src/common/common.context';
import styles from './index.module.less';

export function InstallOptions(props: any) {
    const { form } = useContext(NewSpaceInfoContext);
    return (
        <ProCard title={<h2>安装选项</h2>} headerBordered>
            <PageHeader className="site-page-header" title="获取安装包" style={{ paddingLeft: 0 }} />
            <p>
                <div>Doris Manager将从提供的http地址直接获取安装包。</div>
                <div>
                    若Manager节点可访问公网，推荐直接使用预编译安装包地址；若Manager节点不可访问公网，推荐自行搭建http服务提供安装包。
                </div>
            </p>
            <Form form={form} name="basic" autoComplete="off">
                <Form.Item label="代码包路径" name="packageUrl" rules={[{ required: true, message: '请输入安装路径' }]}>
                    <Input className={styles.input} />
                </Form.Item>
            </Form>
            <PageHeader className="site-page-header" title="指定安装路径" style={{ paddingLeft: 0 }} />
            <div>
                <p>Doris与Doris Manager Agent将安装至该目录下。请确保该目录为Doris及相关组件专用。</p>
            </div>
            <Form form={form} name="basic" autoComplete="off">
                <Form.Item label="安装路径" name="installDir" rules={[{ required: true, message: '请输入安装路径' }]}>
                    <Input className={styles.input} />
                </Form.Item>
                <Form.Item
                    label="Agent启动端口"
                    name="agentPort"
                    rules={[{ required: true, message: '请输入Agent启动端口' }]}
                >
                    <Input className={styles.input} />
                </Form.Item>
            </Form>
        </ProCard>
    );
}
