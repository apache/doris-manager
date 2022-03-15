import React, { useState } from 'react';
import { Button, Modal, Table } from 'antd';
import { useTranslation } from 'react-i18next';
import { ConfigurationItem } from '.';

interface CheckModalProps {
    visible: boolean;
    currentParameter: ConfigurationItem;
    onCancel: () => void;
}

export default function CheckModal(props: CheckModalProps) {
    const { t } = useTranslation();
    const { visible, currentParameter, onCancel } = props;
    const nodeList = currentParameter?.nodes?.map((node, index) => ({
        host: node,
        value: currentParameter.values[index],
    }));

    const columns = [
        {
            title: t`hostIp`,
            dataIndex: 'host',
        },
        {
            title: t`currentValue`,
            dataIndex: 'value',
        },
    ];
    return (
        <Modal
            title={currentParameter.name}
            visible={visible}
            footer={<Button onClick={onCancel}>{t`cancel`}</Button>}
            width={700}
            onCancel={onCancel}
        >
            <Table rowKey="host" dataSource={nodeList} columns={columns} pagination={false} />
        </Modal>
    );
}
