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

import React, { useCallback, useContext, useEffect } from 'react';
import { Button, Col, Divider, message, PageHeader, Row, Typography } from 'antd';
import { DatabaseOutlined, TableOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import moment from 'moment';
import styles from './index.module.less';
import StatusMark from '@src/components/status-mark';
import LiquidFillChart from '../components/liquid-fill-chart';
import DataOverviewItem from '../components/data-overview-item';
import { UserInfoContext } from '@src/common/common.context';
import { useAsync } from '@src/hooks/use-async';
import * as ClusterAPI from '../cluster.api';
import { SpaceAPI } from '../../space/space.api';
import LoadingLayout from '@src/components/loading-layout';
import { isSuccess } from '@src/utils/http';

export default function ClusterOverview() {
    const { t } = useTranslation();
    const userInfo = useContext(UserInfoContext)!;
    const { loading: startLoading, run: runClusterStart } = useAsync();
    const { loading: stopLoading, run: runClusterStop } = useAsync();
    const { loading: restartLoading, run: runClusterRestart } = useAsync();
    const {
        data: clusterInfo,
        loading: clusterInfoLoading,
        run: runGetClusterInfo,
    } = useAsync<{
        overview: Record<string, any>;
        space: Record<string, any>;
    }>({
        loading: true,
        data: {
            overview: {},
            space: {},
        },
    });
    const getClusterInfo = useCallback(
        (setStartLoading = false) => {
            return runGetClusterInfo(
                Promise.all([
                    SpaceAPI.spaceGet(userInfo.space_id + '').then(res => {
                        if (isSuccess(res)) return res.data;
                        return Promise.reject(res);
                    }),
                    ClusterAPI.getClusterOverview(),
                ]).then(res => {
                    return {
                        space: res[0],
                        overview: res[1],
                    };
                }),
                { setStartLoading },
            ).catch(res => {
                message.error(res.msg);
            });
        },
        [runGetClusterInfo, userInfo.space_id],
    );

    useEffect(() => {
        getClusterInfo();
    }, [getClusterInfo]);

    const handleStart = () => {
        runClusterStart(ClusterAPI.startCluster(userInfo.space_id))
            .then(() => {
                message.success('启动成功');
                getClusterInfo(true);
            })
            .catch(res => message.error(res.msg));
    };

    const handleStop = () => {
        runClusterStop(ClusterAPI.stopCluster(userInfo.space_id))
            .then(() => {
                message.success('停止成功');
                getClusterInfo(true);
            })
            .catch(res => message.error(res.msg));
    };

    const handleRestart = () => {
        runClusterRestart(ClusterAPI.restartCluster(userInfo.space_id))
            .then(() => {
                message.success('重启成功');
                getClusterInfo(true);
            })
            .catch(res => message.error(res.msg));
    };

    return (
        <LoadingLayout loading={clusterInfoLoading} wrapperStyle={{ textAlign: 'center', marginTop: 200 }}>
            <PageHeader
                title={clusterInfo?.space.name}
                subTitle={
                    <StatusMark status={clusterInfo?.space.status === 'NORMAL' ? 'success' : 'error'}>
                        {clusterInfo?.space.status === 'NORMAL' ? t`normal` : t`abnormal`}
                    </StatusMark>
                }
                extra={
                    <>
                        <Button
                            onClick={handleStart}
                            loading={startLoading}
                            disabled={clusterInfo?.space.status === 'NORMAL'}
                            type="primary"
                        >
                            {t`start`}
                        </Button>
                        <Button onClick={handleStop} loading={stopLoading}>{t`stop`}</Button>
                        <Button onClick={handleRestart} loading={restartLoading} type="primary">{t`restart`}</Button>
                    </>
                }
            >
                <Row className={styles.infoRow}>
                    <Col span={3}>{t`clusterId`}: </Col>
                    <Col span={19} className={styles.infoRowContent}>
                        {clusterInfo?.space.id}
                    </Col>
                </Row>
                <Row className={styles.infoRow}>
                    <Col span={3}>{t`CreationTime`}: </Col>
                    <Col span={19} className={styles.infoRowContent}>
                        {moment(clusterInfo?.space.createTime).format('YYYY-MM-DD')}
                    </Col>
                </Row>
                <Row className={styles.infoRow}>
                    <Col span={3}>JDBC URL: </Col>
                    <Col span={19} className={styles.infoRowContent}>
                        jdbc:mysql://{clusterInfo?.space.address}:{clusterInfo?.space.queryPort}
                        /DB_NAME?user=USER_NAME&password=PASSWORD
                    </Col>
                </Row>
                <Row gutter={40} justify="space-between" style={{ marginTop: '3.7vh' }}>
                    <Col span={14}>
                        <Typography.Title level={5}>{t`sourceUsage`}</Typography.Title>
                        <Divider />
                        <Row gutter={20}>
                            <Col span={8}>
                                <LiquidFillChart value={clusterInfo?.overview.diskOccupancy} label={t`diskUsage`} />
                            </Col>
                        </Row>
                    </Col>
                    <Col span={10}>
                        <Typography.Title level={5}>{t`dataOverview`}</Typography.Title>
                        <Divider />
                        <Row
                            gutter={80}
                            justify="space-between"
                            align="middle"
                            style={{ marginTop: 60, marginBottom: 30 }}
                        >
                            <Col span={12}>
                                <DataOverviewItem
                                    label={t`DatabaseNum`}
                                    value={clusterInfo?.overview.dbCount}
                                    icon={<DatabaseOutlined />}
                                />
                            </Col>
                            <Col span={12}>
                                <DataOverviewItem
                                    label={t`DataTableNum`}
                                    value={clusterInfo?.overview.tblCount}
                                    icon={<TableOutlined />}
                                />
                            </Col>
                        </Row>
                    </Col>
                </Row>
            </PageHeader>
        </LoadingLayout>
    );
}
