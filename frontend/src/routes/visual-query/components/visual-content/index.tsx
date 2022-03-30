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

import React, { useContext, useImperativeHandle, useMemo, useRef } from 'react';
import * as _ from 'lodash-es';
import classnames from 'classnames';
import { Typography, Row, Spin } from 'antd';
import styles from './style.module.less';
import VisualEditor from './visual-editor';
import { DatabasesContext, DataContext } from '../../context';
import LoadingLayout from '../loading-layout';
import VisualTable from './visual-table';

function VisualContent(props: any, ref: any) {
    const { databasesLoading } = useContext(DatabasesContext);
    const { data: resultData, dataLoading, dataError } = useContext(DataContext);
    const visualWrapperRef = useRef<HTMLDivElement | null>(null);
    const visualTableRef =
        useRef<{ refreshTableHeight: (val: number) => void; refreshTableWidth: (val: number) => void }>(null);

    const isFetchingError = dataError != null || (resultData && resultData.error);

    const content = useMemo(() => {
        if (dataLoading)
            return (
                <Row align="middle" justify="center" style={{ height: '100%' }}>
                    <Spin />
                </Row>
            );
        if (isFetchingError)
            return (
                <Row align="middle" justify="center" style={{ height: '100%' }}>
                    <Typography.Text type="danger">
                        您的查询出现错误：{dataError ? '请检查您的网络' : resultData?.error}
                    </Typography.Text>
                </Row>
            );
        if (resultData == null)
            return (
                <Row align="middle" justify="center" style={{ height: '100%' }}>
                    <Typography.Text>您的查询结果将在这里展示</Typography.Text>
                </Row>
            );
        return (
            <>
                <div style={{ height: '80%' }}>
                    <VisualTable ref={visualTableRef} />
                </div>
            </>
        );
    }, [dataLoading, isFetchingError, resultData, dataError]);

    const onResize = _.throttle(() => {
        if (visualTableRef.current && visualWrapperRef.current) {
            visualTableRef.current.refreshTableHeight(visualWrapperRef.current.clientHeight * 0.8 - 120);
        }
    });

    const refreshTableWidth = () => {
        if (visualTableRef.current && visualWrapperRef.current) {
            visualTableRef.current.refreshTableWidth(visualWrapperRef.current.clientWidth);
        }
    };

    useImperativeHandle(ref, () => ({
        refreshTableWidth,
    }));

    return (
        <LoadingLayout loading={databasesLoading}>
            <div className={classnames(styles.content)}>
                <VisualEditor onResize={onResize} />
                <div className={styles.visualWrapper} ref={visualWrapperRef}>
                    {content}
                </div>
            </div>
        </LoadingLayout>
    );
}

export default React.forwardRef(VisualContent);
