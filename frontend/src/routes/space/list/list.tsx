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

import { useContext, useEffect, useState } from 'react';
import { Button, Dropdown, Menu, Row, Table, Tabs } from 'antd';
import { Space } from 'antd';
import { SpaceAPI } from '../space.api';
import moment from 'moment';
import { FlatBtn, FlatBtnGroup } from '@src/components/flatbtn';
import { TABLE_DELAY } from '@src/config';
import { PageContainer } from '@ant-design/pro-layout';
import { useRequest } from 'ahooks';
import { DownOutlined } from '@ant-design/icons';
import { modal } from '@src/components/doris-modal/doris-modal';
import { AccessClusterStepsEnum, ACCESS_CLUSTER_REQUEST_INIT_PARAMS } from '../access-cluster/access-cluster.data';
import { useRecoilState } from 'recoil';
import { requestInfoState } from '../access-cluster/access-cluster.recoil';
import { UserInfoContext } from '@src/common/common.context';
import { useTranslation } from 'react-i18next';
import { NewClusterStepsEnum } from '../new-cluster/new-cluster.data';
import { useNavigate } from 'react-router';
const { TabPane } = Tabs;
type SpaceListType = 'finished' | 'draft';

export const SpaceList = () => {
    const userInfo = useContext(UserInfoContext);
    const navigate = useNavigate();
    const [activeKey, setActiveKey] = useState<SpaceListType>('finished');
    const [requestInfo, setRequestInfo] = useRecoilState(requestInfoState);
    const { t } = useTranslation();

    const {
        data: spaceList,
        loading,
        run: getSpaceList,
    } = useRequest(
        async () => {
            const res = await SpaceAPI.spaceList();
            return res.data;
        },
        { manual: true },
    );

    const {
        data: draftSpaceList,
        loading: draftLoading,
        run: getDraftSpaceList,
    } = useRequest(
        async () => {
            const res = await SpaceAPI.spaceList();
            return res.data;
        },
        { manual: true },
    );

    useEffect(() => {
        refresh();
    }, [activeKey]);

    function refresh() {
        if (activeKey === 'finished') {
            getSpaceList();
        } else {
            getDraftSpaceList();
        }
    }

    async function recover(record: any) {
        record.requestInfo.type === 'CREATION'
            ? navigate(`/space/new/${record.requestId}/${NewClusterStepsEnum[record.eventType]}`)
            : navigate(`/space/access/${record.requestId}/${AccessClusterStepsEnum[record.eventType]}`);
    }

    const enterSpace = (record: any) => {
        SpaceAPI.switchSpace(record.id).then(res => {
            if (res.code === 0) {
                navigate('/cluster');
            }
        });
    };

    function deleteSpace(record: any) {
        modal.confirm(t`Notice`, t`SpaceDeleteTips`, async () => {
            SpaceAPI.spaceDelete(record.id).then(result => {
                if (result && result.code !== 0) {
                    modal.error('失败', result.msg);
                } else {
                    modal.success(t`Delete Success`).then(result => {
                        if (result.isConfirmed) {
                            refresh();
                        }
                    });
                }
            });
        });
    }

    const columns = [
        {
            title: t`Space Name`,
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: t`CreationTime`,
            dataIndex: 'createTime',
            key: 'createTime',
            render: (createTime: string) => <span>{moment(new Date(createTime)).format('YYYY-MM-DD HH:mm:ss')}</span>,
        },
        {
            title: t`Actions`,
            dataIndex: 'status',
            key: 'status',
            width: '240px',
            render: (text: string, record: any) => (
                <FlatBtnGroup showNum={4}>
                    <FlatBtn onClick={() => enterSpace(record)}>{t`Enter Space`}</FlatBtn>
                    <FlatBtn onClick={() => deleteSpace(record)}>{t`Delete`}</FlatBtn>
                </FlatBtnGroup>
            ),
        },
    ];

    const draftColumns = [
        {
            title: t`Space Name`,
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: t`CreationTime`,
            dataIndex: 'createTime',
            key: 'createTime',
            render: (createTime: string) => <span>{moment(new Date(createTime)).format('YYYY-MM-DD HH:mm:ss')}</span>,
        },
        {
            title: t`Creator`,
            dataIndex: 'creator',
            key: 'creator',
        },
        {
            title: t`Actions`,
            dataIndex: 'status',
            key: 'status',
            width: '200px',
            render: (text: string, record: any) => (
                <FlatBtnGroup>
                    <FlatBtn onClick={() => recover(record)}>{t`Recover`}</FlatBtn>
                    <FlatBtn onClick={() => deleteSpace(record)}>{t`Delete`}</FlatBtn>
                </FlatBtnGroup>
            ),
        },
    ];
    const menu = (
        <Menu>
            <Menu.Item
                onClick={() => {
                    setRequestInfo(ACCESS_CLUSTER_REQUEST_INIT_PARAMS);
                    navigate('/space/new/0');
                }}
                key="1"
            >{t`New Cluster`}</Menu.Item>
            <Menu.Item
                onClick={() => {
                    setRequestInfo(ACCESS_CLUSTER_REQUEST_INIT_PARAMS);
                    navigate('/space/access/0');
                }}
                key="2"
            >{t`Cluster hosting`}</Menu.Item>
        </Menu>
    );

    return (
        <PageContainer
            header={{
                title: <h2>{t`Space List`}</h2>,
            }}
        >
            <Tabs
                activeKey={activeKey}
                onChange={(key: any) => setActiveKey(key)}
                type="card"
                tabBarExtraContent={
                    <>
                        {userInfo?.is_super_admin && (
                            <Row justify="end" style={{ marginBottom: 20 }}>
                                <Space>
                                    <Dropdown overlay={menu}>
                                        <Button>
                                            {t`New Space`} <DownOutlined />
                                        </Button>
                                    </Dropdown>
                                </Space>
                            </Row>
                        )}
                    </>
                }
            >
                <TabPane tab={t`Finished`} key="finished"></TabPane>
                {userInfo?.is_super_admin && <TabPane tab={t`Not Finished`} key="draft"></TabPane>}
            </Tabs>
            {activeKey === 'finished' && (
                <Table
                    columns={columns}
                    dataSource={spaceList?.filter((list: any) => list.requestCompleted)}
                    rowKey="name"
                    loading={{ spinning: loading, delay: TABLE_DELAY }}
                />
            )}
            {activeKey === 'draft' && (
                <Table
                    columns={draftColumns}
                    dataSource={draftSpaceList?.filter((list: any) => !list.requestCompleted)}
                    rowKey="name"
                    loading={{ spinning: loading, delay: TABLE_DELAY }}
                />
            )}
        </PageContainer>
    );
};
