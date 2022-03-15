import React from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProCard from '@ant-design/pro-card';
import { Button, Row, Space, Table } from 'antd';
import { useHistory } from 'react-router';

export function NodeRegister(props: any) {
    const history = useHistory();
    const columns = [
        {
            title: '序号',
            dataIndex: 'order',
            key: 'order',
        },
        {
            title: '节点IP',
            dataIndex: 'ip',
            key: 'ip',
        },
        {
            title: '注册状态',
            dataIndex: 'register_status',
            key: 'register_status',
        },
        {
            title: '操作',
            key: 'option',
            render: () => (
                <Space>
                    <Button>重试</Button>
                    <Button>跳过</Button>
                </Space>
            ),
        },
    ];
    return (
        <PageContainer
            header={{
                title: '节点列表',
            }}
        >
            <Table columns={columns} />
        </PageContainer>
    );
}
