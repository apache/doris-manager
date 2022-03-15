import { Button, Divider, Form, Input, message, Modal, PageHeader, Row, Space, Steps, Table } from 'antd';
import React, { useContext, useMemo, useRef, useState } from 'react';
import ProCard from '@ant-design/pro-card';
import { NewSpaceInfoContext, UserInfoContext } from '@src/common/common.context';
import {NodeList} from './node-list/node-list';

const { TextArea } = Input;

export function AddNode(props: any) {
    const userInfo = useContext(UserInfoContext);
    const {form} = useContext(NewSpaceInfoContext);
    return (
        <ProCard title={<h2>添加节点</h2>} headerBordered>
            <PageHeader className="site-page-header" title="SSH信任" style={{ paddingLeft: 0 }} />
            <span>请提前完成Manager节点与其他节点间SSH信任，并在下方填入Manager节点的SSH信息。<a>如何进行SSH信任？</a></span>
            <Divider style={{ margin: 0, marginBottom: 24 }} />
            <Form
                form={form}
                name="basic"
                labelCol={{ span: 2 }}
                wrapperCol={{ span: 10 }}
                initialValues={{
                    remember: true,
                    user: 'root',
                    sshPort: 8022,
                    sshKey: '',
                    installDir: '/usr/local/doris',
                    packageUrl: 'http://172.16.0.4:8002/PALO-0.15.1-rc09-binary.tar.gz',
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
                <PageHeader className="site-page-header" title="节点列表" style={{ paddingLeft: 0 }} />
                <Divider style={{ margin: 0, marginBottom: 24 }} />
                <Form.Item name="hosts" style={{width: '100%'}} wrapperCol={{span: 24}}>
                    <NodeList />
                </Form.Item>
            </Form>
        </ProCard>
    );
}
