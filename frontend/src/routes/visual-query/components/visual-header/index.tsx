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
import { Button, message, Row, Typography } from 'antd';
import styles from './index.module.less';
import { DataContext } from '../../context';

interface VisualHeaderProps {
    setSaveQueryModalVisible: (v: boolean) => void;
}

export default function VisualHeader(props: VisualHeaderProps) {
    const { setSaveQueryModalVisible } = props;
    const { data } = useContext(DataContext);

    const handleSave = () => {
        if (data == null || data.error != null) {
            return message.info('请先进行有效查询');
        }
        setSaveQueryModalVisible(true);
    };

    return (
        <Row justify="space-between" align="middle" className={styles.container}>
            <Typography.Title level={4}>SQL编辑区</Typography.Title>
            <div>
                <Button type="link" onClick={handleSave}>
                    保存
                </Button>
            </div>
        </Row>
    );
}
