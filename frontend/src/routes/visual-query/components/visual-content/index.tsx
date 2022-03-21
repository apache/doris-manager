import React, { useContext, useImperativeHandle, useMemo, useRef } from 'react';
import * as _ from 'lodash-es';
import classnames from 'classnames';
import { Button, Typography, Row, Spin } from 'antd';
import styles from './style.module.less';
import VisualEditor from './visual-editor';
import VisualTable from './visual-table';
import { DatabasesContext, DataContext } from '../../context';
import { ChartTypeEnum, SidebarTypeEnum } from '../../types';
import LoadingLayout from '../loading-layout';

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
