import { Form, Input, PageHeader } from 'antd';
import React, { useContext } from 'react';
import ProCard from '@ant-design/pro-card';
import { NewSpaceInfoContext } from '@src/common/common.context';

export function InstallOptions(props: any) {
    const {form} = useContext(NewSpaceInfoContext);
    return (
        <ProCard title={<h2>安装选项</h2>} headerBordered>
            <PageHeader className="site-page-header" title="获取安装包" style={{ paddingLeft: 0 }} />
            <p>
                <div>Doris Manager将从提供的http地址直接获取安装包。</div>
                <div>
                    若Manager节点可访问公网，推荐直接使用预编译安装包地址；若Manager节点不可访问公网，推荐自行搭建http服务提供安装包。
                </div>
            </p>
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
                    packageUrl: 'http://10.193.215.27:8091/download/PALO-0.15.1-rc09-binary.tar.gz',
                }}
                autoComplete="off"
            >
                <Form.Item label="代码包路径" name="packageUrl" rules={[{ required: true, message: '请输入安装路径' }]}>
                    <Input />
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
                    installDir: '/usr/local/doris',
                }}
                autoComplete="off"
            >
                <Form.Item label="安装路径" name="installDir" rules={[{ required: true, message: '请输入安装路径' }]}>
                    <Input />
                </Form.Item>
            </Form>
        </ProCard>
    );
}
