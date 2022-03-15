import React, { useState, useContext, useEffect } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Button, Modal, Row, Space, Switch, Table, Radio, message, Tabs, Form } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router';
import * as types from '../../types/index.type';
import API from '../../new-cluster.api';
import { SpaceAPI } from '../../../space.api';
import { ResultModal } from '../../../components/result-modal';
import { modalState, processId, nodeHardwareQuery, stepState } from '../../recoils/index';
import { useRecoilState, useRecoilValue } from 'recoil';
import { isSuccess } from '@src/utils/http';
import { DorisNodeTypeEnum } from '../../types/index.type';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { FlatBtn } from '@src/components/flatbtn';
import { useAsync } from '@src/hooks/use-async';
const { confirm } = Modal;
const { TabPane } = Tabs;

export function ClusterPlan() {
    const { form } = useContext(NewSpaceInfoContext);
    return (
        <Form form={form} name="basic" labelCol={{ span: 2 }} wrapperCol={{ span: 10 }} autoComplete="off">
            <Form.Item name="nodeConfig" style={{ width: '100%' }} wrapperCol={{ span: 24 }}>
                <ClusterPlanContent />
            </Form.Item>
        </Form>
    );
}

export function ClusterPlanContent(props: any) {
    const { reqInfo } = useContext(NewSpaceInfoContext);
    const [visible, setVisible] = useState(false);
    const [confirmLoading, setConfirmLoading] = useState(false);
    const [selectedRowKeys, setSelectedRowKeys] = useState<any[]>([]);
    const [feChecked, setFeChecked] = useState<number[]>([]);
    const [feData, setFeData] = useState<any[]>([]);
    const [beChecked, setBeChecked] = useState<number[]>([]);
    const [beData, setBeData] = useState<any[]>([]);
    const [roleType, setRoleType] = useState<DorisNodeTypeEnum>(DorisNodeTypeEnum.FE);
    const [mixMode, setMixMode] = useState(false);
    const [modal, setModal] = useRecoilState(modalState);
    const processID = useRecoilValue(processId);
    const { data, run: runGetClusterNodes } = useAsync<any[]>({ data: [] });
    const [modalAllData, setModalAllData] = useState<any[]>(data || []);
    const [activeKey, setActiveKey] = useState(DorisNodeTypeEnum.FE);

    useEffect(() => {
        if (!reqInfo.cluster_id) return;
        runGetClusterNodes(
            SpaceAPI.getClusterNodes<any[]>({ clusterId: reqInfo.cluster_id }).then(res => {
                if (isSuccess(res)) return res.data;
                return Promise.reject(res);
            }),
        ).catch(res => {
            message.error(res.msg);
        });
    }, [runGetClusterNodes, reqInfo.cluster_id]);

    useEffect(() => {
        props?.onChange([
            {
                moduleName: 'fe',
                nodeIds: feData.map(item => item.nodeId),
            },
            {
                moduleName: 'be',
                nodeIds: beData.map(item => item.nodeId),
            },
        ]);
    }, [feData, beData]);

    const changeFeNodeType = (record: any, index: number, val: types.feNodeType) => {
        let tempFeData = [...feData];
        tempFeData[index] = {
            ...tempFeData[index],
            feNodeType: val,
        };
        console.log(tempFeData);
        setFeData(tempFeData);
    };
    const nodeColumns = [
        {
            title: '节点ID',
            dataIndex: 'nodeId',
        },
        {
            title: '节点IP',
            dataIndex: 'host',
            key: 'host',
        },
    ];
    const feColumns = [
        ...nodeColumns,
        {
            title: '操作',
            key: 'action',
            render: (e: any, record: any) => <FlatBtn onClick={() => handleRemove(record, 'fe')}>删除</FlatBtn>,
        },
    ];
    const beColumns = [
        ...nodeColumns,
        {
            title: '操作',
            key: 'action',
            render: (e: any, record: any) => <FlatBtn onClick={() => handleRemove(record, 'be')}>删除</FlatBtn>,
        },
    ];

    const handleRemove = (record: any, type: 'fe' | 'be') => {
        switch (type) {
            case 'fe':
                const feIndex = feData.findIndex(item => item.nodeId === record.nodeId);
                feData.splice(feIndex, 1);
                setFeData([...feData]);
                setFeChecked(feChecked.filter(item => item !== record.host));
                break;
            case 'be':
                const beIndex = beData.findIndex(item => item.nodeId === record.nodeId);
                beData.splice(beIndex, 1);
                setBeData([...beData]);
                setBeChecked(beChecked.filter(item => item !== record.host));
                break;
            default:
                break;
        }
    };

    const showModal = (val: DorisNodeTypeEnum) => {
        setVisible(true);
        setRoleType(val);
        setModalAllData(data || []);
        val === DorisNodeTypeEnum.FE ? setSelectedRowKeys(feChecked) : setSelectedRowKeys(beChecked);
    };

    const handleOk = () => {
        setConfirmLoading(true);
        setTimeout(() => {
            setVisible(false);
            setConfirmLoading(false);
        }, 0);

        if (roleType === DorisNodeTypeEnum.FE) {
            setFeChecked(selectedRowKeys);
            let tempFeData = data?.filter(item => selectedRowKeys.includes(item.host));
            setFeData(tempFeData || []);
        }
        if (roleType === DorisNodeTypeEnum.BE) {
            setBeChecked(selectedRowKeys);
            let tempBeData = data?.filter(item => selectedRowKeys.includes(item.host));
            setBeData(tempBeData || []);
        }
    };

    const handleCancel = () => {
        setSelectedRowKeys([]);
        setVisible(false);
    };
    const onSelectChange = (selectedRowKeys: any[]) => {
        setSelectedRowKeys(selectedRowKeys);
    };
    const rowSelection = {
        selectedRowKeys,
        onChange: onSelectChange,
    };
    // const handleInstallService = () => {
    //     // setModal({...modal, visible: true})
    //     // return;
    //     const temp1 = feData.map(item => ({
    //         host: item.host,
    //         role: DorisNodeTypeEnum.FE,
    //         feNodeType: item.feNodeType || types.feNodeType.OBSERVER,
    //     }));
    //     const temp2 = beData.map(item => ({
    //         host: item.host,
    //         role: DorisNodeTypeEnum.BE,
    //     }));
    //     const params = {
    //         processId: processID,
    //         installInfos: [...temp1, ...temp2],
    //     };
    //     API.installService(params).then(res => {
    //         if (isSuccess(res)) {
    //             setModal({ ...modal, visible: true });
    //         } else {
    //             message.info(res.msg);
    //         }
    //     });
    // };
    return (
        <PageContainer
            header={{
                title: <h2>规划节点</h2>,
            }}
        >
            <Tabs
                activeKey={activeKey}
                onChange={(key: any) => setActiveKey(key)}
                type="card"
                tabBarExtraContent={
                    <Button type="primary" onClick={() => showModal(activeKey)}>
                        分配节点
                    </Button>
                }
            >
                <TabPane tab="FE节点" key={DorisNodeTypeEnum.FE}></TabPane>
                <TabPane tab="BE节点" key={DorisNodeTypeEnum.BE}></TabPane>
            </Tabs>
            {activeKey === DorisNodeTypeEnum.FE && <Table bordered columns={feColumns} dataSource={feData} />}
            {activeKey === DorisNodeTypeEnum.BE && <Table bordered columns={beColumns} dataSource={beData} />}
            {visible && (
                <Modal
                    title="分配节点"
                    visible={visible}
                    onOk={handleOk}
                    confirmLoading={confirmLoading}
                    onCancel={handleCancel}
                    width={800}
                >
                    <>
                        <Alert message={`已选择 ${selectedRowKeys.length} 项目`} type="info" showIcon />
                        <Table
                            style={{ marginTop: 20 }}
                            scroll={{ y: 350 }}
                            pagination={false}
                            rowSelection={rowSelection}
                            columns={nodeColumns}
                            dataSource={modalAllData}
                            rowKey={'host'}
                        />
                    </>
                </Modal>
            )}
            {modal.visible && <ResultModal></ResultModal>}
        </PageContainer>
    );
}
