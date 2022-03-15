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

import React from 'react';
import { Row, Col } from 'antd';
import styles from './index.module.less';

interface DataOverviewItemProps {
    label: string;
    value: number;
    icon: React.ReactNode;
}

export default function DataOverviewItem(props: DataOverviewItemProps) {
    const { icon, label, value } = props;
    return (
        <div className={styles.container}>
            <Row justify="center" gutter={20} style={{ fontSize: 60, width: '100%' }}>
                <Col>{icon}</Col>
                <Col>{value}</Col>
            </Row>
            <div className={styles.label}>{label}</div>
        </div>
    );
}
