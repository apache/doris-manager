import React, { useState } from 'react';
import { Modal, Form, Input, Radio, Select, message } from 'antd';
import { useTranslation } from 'react-i18next';
import { ConfigurationItem } from '.';
import { useAsync } from '@src/hooks/use-async';
import * as ClusterAPI from '../cluster.api';

interface EditModalProps {
    visible: boolean;
    currentParameter: ConfigurationItem;
    onOk: () => void;
    onCancel: () => void;
}

interface FormInstanceProps {
    value: string;
    persist: boolean;
    range: 'all' | 'part';
    nodes: number[];
}

export default function EditModal(props: EditModalProps) {
    const { t } = useTranslation();
    const { visible, currentParameter, onOk, onCancel } = props;
    const { loading: confirmLoading, run: runChangeConfiguration } = useAsync();
    const [form] = Form.useForm<FormInstanceProps>();

    const handleOk = () => {
        form.validateFields()
            .then(values => {
                runChangeConfiguration(
                    ClusterAPI.changeConfiguration(currentParameter.type === 'Frontend' ? 'fe' : 'be', {
                        [currentParameter.name]: {
                            node: [...currentParameter.nodes],
                            value: values.value,
                            persist: values.persist ? 'true' : 'false',
                        },
                    }),
                )
                    .then(() => {
                        message.success('编辑成功');
                        onOk();
                        handleCancel();
                    })
                    .catch(res => {
                        message.error(res.msg);
                    });
            })
            .catch(console.log);
    };

    const handleCancel = () => {
        form.resetFields();
        // setRange('all');
        onCancel();
    };

    return (
        <Modal
            destroyOnClose
            title={currentParameter.name}
            confirmLoading={confirmLoading}
            visible={visible}
            onCancel={handleCancel}
            onOk={handleOk}
        >
            <Form form={form} autoComplete="off" labelCol={{ span: 5 }}>
                <Form.Item
                    name="value"
                    label={t`confValue`}
                    rules={[{ required: true, message: t`confValuePlaceholder` }]}
                >
                    <Input placeholder={t`confValuePlaceholder`} style={{ width: 300 }} />
                </Form.Item>
                <Form.Item
                    name="persist"
                    label={t`effectiveWay`}
                    rules={[{ required: true, message: t`effectiveWayRequiredMessage` }]}
                >
                    <Radio.Group>
                        <Radio value={true}>{t`permanentEffective`}</Radio>
                        <Radio value={false}>{t`onceEffective`}</Radio>
                    </Radio.Group>
                </Form.Item>
                {/* <Form.Item
                    name="range"
                    label={t`effectiveRange`}
                    rules={[{ required: true, message: t`effectiveRangeRequiredMessage` }]}
                >
                    <Radio.Group
                        onChange={e => {
                            setRange(e.target.value);
                        }}
                    >
                        <Radio value="all">{t`allNodes`}</Radio>
                        <Radio value="part">{t`certainNodes`}</Radio>
                    </Radio.Group>
                </Form.Item> */}
                {/* {range === 'part' && (
                    <Form.Item
                        name="nodes"
                        label={t`effectiveNodes`}
                        rules={[{ required: true, message: t`effectiveNodesPlaceholder` }]}
                    >
                        <Select mode="multiple" placeholder={t`effectiveNodesPlaceholder`}>
                            <Select.Option value="1">127.0.0.1</Select.Option>
                        </Select>
                    </Form.Item>
                )} */}
            </Form>
        </Modal>
    );
}
