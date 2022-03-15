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

import { Typography } from 'antd';
import React, { PropsWithChildren } from 'react';

interface FormItemLayoutProps {
    title: string;
}

export default function FormItemLayout(props: PropsWithChildren<FormItemLayoutProps>) {
    const { title, children } = props;
    return (
        <div style={{ marginBottom: 24 }}>
            <Typography.Title level={5} style={{ fontSize: 14, marginBottom: 8 }}>
                {title}
            </Typography.Title>
            {children}
        </div>
    );
}
