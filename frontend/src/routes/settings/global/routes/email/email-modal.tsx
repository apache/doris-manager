import React, { useState } from 'react';
import { Input, Modal } from 'antd';

interface EmailModalProps {
    visible: boolean;
    confirmLoading: boolean;
    onOk: (email: string) => () => void;
    onCancel: () => void;
}

export default function EmailModal(props: EmailModalProps) {
    const { visible, onOk, onCancel, confirmLoading } = props;
    const [emailContent, setEmailContent] = useState('');

    return (
        <Modal
            title="发送测试邮件"
            visible={visible}
            onOk={onOk(emailContent)}
            onCancel={onCancel}
            okText="发送"
            cancelText="取消"
            confirmLoading={confirmLoading}
        >
            <Input
                value={emailContent}
                onChange={e => setEmailContent(e.target.value)}
                placeholder="请输入目标邮箱地址"
            />
        </Modal>
    );
}
