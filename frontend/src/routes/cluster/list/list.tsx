import { Button, PageHeader, Row, Space, Table } from 'antd';
import React from 'react';
import { useHistory, useRouteMatch } from 'react-router';

export function ClusterList(props: any) {
    const history = useHistory();
    const match = useRouteMatch();
    const columns = [
        {
            title: '集群',
            dataIndex: 'cluster',
            key: 'cluster',
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
        },
        {
            title: '链接信息',
            dataIndex: 'connect_info',
            key: 'connect_info',
        },
    ];
    const [clusterList, setClusterList] = React.useState([]);
    return (
        <div style={{ padding: '0 20px', backgroundColor: 'white', marginTop: 20, minHeight: 'calc(100% - 60px)' }}>
            <PageHeader className="site-page-header" title="集群列表" style={{ paddingLeft: 0 }} />
            <Row justify="end" style={{marginBottom: 20}}>
                <Space>
                    <Button type="primary" onClick={() => history.push(`/cluster/new`)}>新建集群</Button>
                    <Button type="primary">接入集群</Button>
                </Space>
            </Row>
            <Table
                columns={columns}
                dataSource={clusterList}
                rowKey={(record: any[]) => record[Object.keys(record).length - 1]}
                size="middle"
            />
        </div>
    );
}
