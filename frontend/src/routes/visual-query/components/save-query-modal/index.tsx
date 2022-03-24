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

import React, { useContext } from 'react';
import { Modal, Form, Input, Cascader, message } from 'antd';
import LoadingLayout from '@src/components/loading-layout';
import { CascaderItem } from '../../types';
import { DataContext } from '../../context';
import { useAsync } from '@src/hooks/use-async';
import { addCardAPI } from '../../visual-query.api';

interface SaveQueryModalProps {
    visible: boolean;
    loading?: boolean;
    collections: CascaderItem[];
    setSaveQueryModalVisible: (v: boolean) => void;
    setSaveDashboardModalVisible: (v: boolean) => void;
}

interface FormProps {
    name: string;
    description?: string;
    collection: number[];
}

export default function SaveQueryModal(props: SaveQueryModalProps) {
    const { visible, setSaveQueryModalVisible, loading = false, collections, setSaveDashboardModalVisible } = props;
    const { columns, data } = useContext(DataContext);

    const [form] = Form.useForm<FormProps>();

    const { loading: confirmLoading, run: runAddCard } = useAsync();

    const handleCancel = () => {
        setSaveQueryModalVisible(false);
    };

    const filter = (inputValue: string, path: any[]) => {
        return path.some(option => option.name.toLowerCase().indexOf(inputValue.toLowerCase()) > -1);
    };

    const displayRender = (label: string[], path?: any[]) => {
        return path?.map(item => item.name).join(' / ') || '';
    };

    const handleOk = async () => {
        try {
            const { collection, name, description } = await form.validateFields();
            const resultData = data!;
            const visualization_settings = {
                columns,
            }
            try {
                await runAddCard(
                    addCardAPI({
                        collection_id: collection[collection.length - 1],
                        dataset_query: {
                            database: resultData.database_id,
                            native: {
                                query: resultData.data.native_form.query,
                            },
                            type: 'NATIVE',
                        },
                        description: description || null,
                        display: 'table',
                        name,
                        result_metadata: {
                            columns: resultData.data.cols,
                        },
                        original_definition: {
                            database: resultData.database_id,
                            native: {
                                query: resultData.data.native_form.query,
                            },
                            type: 'NATIVE',
                        },
                        visualization_settings,
                    }),
                );
                message.success('保存查询成功');
                setSaveQueryModalVisible(false);
                Modal.confirm({
                    title: '是否将此查询保存到dashboard',
                    onOk: () => {
                        setSaveDashboardModalVisible(true);
                    },
                });
            } catch (e) {
                message.error('保存查询失败');
            }
        } catch (e) {
            console.log(e);
        }
    };

    return (
        <Modal
            title="保存查询"
            visible={visible}
            onCancel={handleCancel}
            onOk={handleOk}
            confirmLoading={confirmLoading}
            destroyOnClose
        >
            <LoadingLayout loading={loading} wrapperStyle={{ textAlign: 'center' }}>
                <Form form={form} autoComplete="off" labelCol={{ span: 5 }} wrapperCol={{ span: 17 }}>
                    <Form.Item
                        name="name"
                        label="名字"
                        rules={[{ required: true, message: '请为该查询命名' }]}
                        required
                    >
                        <Input placeholder="请为该查询命名" />
                    </Form.Item>
                    <Form.Item name="description" label="描述">
                        <Input.TextArea />
                    </Form.Item>
                    <Form.Item
                        name="collection"
                        label="所属文件夹"
                        required
                        rules={[{ required: true, message: '请选择所属文件夹' }]}
                    >
                        <Cascader
                            options={collections}
                            fieldNames={{ value: 'id' }}
                            showSearch={{
                                filter,
                                render: (inputValue: string, path: any[]) => {
                                    return path.map(item => item.name).join(' / ');
                                },
                            }}
                            displayRender={displayRender}
                            changeOnSelect
                        />
                    </Form.Item>
                </Form>
            </LoadingLayout>
        </Modal>
    );
}
