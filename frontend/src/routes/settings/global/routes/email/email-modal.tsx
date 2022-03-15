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
