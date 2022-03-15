import React, { useContext, useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProCard from '@ant-design/pro-card';
import { Button, Row, Space, Table, Tabs } from 'antd';
import { useHistory, useRouteMatch } from 'react-router';
import TabPane from '@ant-design/pro-card/lib/components/TabPane';
import { DorisNodeTypeEnum } from '../../new-cluster/types/params.type';
import { SpaceAPI } from '../../space.api';
import { isSuccess } from '@src/utils/http';
import { NewSpaceInfoContext } from '@src/common/common.context';

export function ClusterVerify(props: any) {
    const history = useHistory();
    const [activeKey, setActiveKey] = useState(DorisNodeTypeEnum.FE);
    const {reqInfo} = useContext(NewSpaceInfoContext);
    const match = useRouteMatch<{spaceId: string}>();
    const clusterId = match.params.spaceId;
    const [instance, setInstance] = useState([]);
    const [nodeTypes, setNodeTypes] = useState<any[]>([]);
    const [feNodes, setFENodes] = useState([]);
    const [beNodes, setBENodes] = useState([]);
    const [brokerNodes, setBrokerNodes] = useState([]);


    const columns = [
        {
            title: '序号',
            dataIndex: 'instanceId',
            key: 'instanceId',
        },
        {
            title: '节点IP',
            dataIndex: 'nodeHost',
            key: 'nodeHost',
        },
        {
            title: '安装进度',
            dataIndex: 'operateStatus',
            key: 'operateStatus',
        },
        {
            title: '操作',
            key: 'option',
            render: () => (
                <Space>
                    <Button>重试</Button>
                    {/* <Button>跳过</Button>
                    <Button>查看日志</Button> */}
                </Space>
            ),
        },
    ];

    useEffect(() => {
        if (reqInfo && reqInfo.cluster_id) {
            getClusterInstance();
        }
    }, [reqInfo.cluster_id]);

    async function getClusterInstance() {
        const res = await SpaceAPI.getClusterInstance({clusterId: reqInfo.cluster_id});   
        if (isSuccess(res)) {
            setInstance(res.data);
            const types = [];
            const feNodes = res.data.filter(item => item.moduleName?.toUpperCase() === DorisNodeTypeEnum.FE);
            const beNodes = res.data.filter(item => item.moduleName?.toUpperCase() === DorisNodeTypeEnum.BE);
            const brokerNodes = res.data.filter(item => item.moduleName?.toUpperCase() === DorisNodeTypeEnum.BROKER);
            setFENodes(feNodes);
            setBENodes(beNodes);
            setBrokerNodes(brokerNodes);
            if (feNodes.length > 0) {
                types.push({key: DorisNodeTypeEnum.FE, tab: 'FE节点', moduleName: DorisNodeTypeEnum.FE });
            }
            if (beNodes.length > 0) {
                types.push({key: DorisNodeTypeEnum.BE, tab: 'BE节点', moduleName: DorisNodeTypeEnum.BE });
            }
            if (brokerNodes.length > 0) {
                types.push({key: DorisNodeTypeEnum.BROKER, tab: 'Broker节点', moduleName: DorisNodeTypeEnum.BROKER });
            }
            setNodeTypes(types);
        }
    }

    // async function getClusterModule() {
    //     const res = await SpaceAPI.getClusterModule({clusterId: reqInfo.cluster_id});   
    //     if (isSuccess(res)) {
    //         setNodeTypes(res.data);
    //     }
    // }


    return (
        <PageContainer
            header={{
                title: <h2>校验集群</h2>,
            }}
        >
            <Tabs activeKey={activeKey} onChange={(key: any) => setActiveKey(key)} type="card">
                {nodeTypes.map(item => (
                    <TabPane tab={item.tab} key={item.key}>
                    </TabPane>
                ))}
            </Tabs>
            {activeKey === DorisNodeTypeEnum.FE && (
                 <Table columns={columns} dataSource={feNodes} />
            )}
            {activeKey === DorisNodeTypeEnum.BE && (
                 <Table columns={columns} dataSource={beNodes} />
            )}
            {activeKey === DorisNodeTypeEnum.BROKER && (
                 <Table columns={columns} dataSource={brokerNodes} />
            )}
        </PageContainer>
    );
}
