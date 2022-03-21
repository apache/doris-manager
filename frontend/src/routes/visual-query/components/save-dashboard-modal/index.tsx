import React, { useEffect } from 'react';
import { useImmer } from 'use-immer';
import { Modal, Form, Cascader } from 'antd';
import styles from './index.module.less';
import LoadingLayout from '@src/components/loading-layout';
import { CascaderItem } from '../../types';
import { processCollections } from '../../utils';
import { fetchChildOfCollectionAPI } from '../../visual-query.api';

interface SaveDashboardModalProps {
    visible: boolean;
    loading?: boolean;
    collections: CascaderItem[];
    setSaveDashboardModalVisible: (v: boolean) => void;
}

export default function SaveDashboardModal(props: SaveDashboardModalProps) {
    const { visible, loading = false, collections: defaultCollections, setSaveDashboardModalVisible } = props;
    const [collections, setCollections] = useImmer([...processCollections(defaultCollections)]);

    useEffect(() => {
        setCollections([...processCollections(defaultCollections)]);
    }, [defaultCollections]);

    const [form] = Form.useForm();

    const handleCancel = () => {
        setSaveDashboardModalVisible(false);
    };

    const loadData = (path: any[]) => {
        const currentCollection = path[path.length - 1];
        currentCollection.loading = true;
        fetchChildOfCollectionAPI({
            collectionId: currentCollection.id,
            model: 'dashboard',
        }).then(res => {
            // Todo
        });
    };

    return (
        <Modal visible={visible} onCancel={handleCancel} title="把查询添加到仪表盘">
            <LoadingLayout loading={loading} wrapperStyle={{ textAlign: 'center' }}>
                <Form form={form} autoComplete="off" labelCol={{ span: 5 }} wrapperCol={{ span: 17 }}>
                    <Form.Item
                        name="dashboard"
                        label="仪表盘"
                        required
                        rules={[{ required: true, message: '请选择仪表盘' }]}
                    >
                        <Cascader options={collections} fieldNames={{ value: 'id' }} loadData={loadData} />
                    </Form.Item>
                </Form>
            </LoadingLayout>
        </Modal>
    );
}
