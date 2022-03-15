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
import ReactEChartsCore from 'echarts-for-react/lib/core';
import * as echarts from 'echarts/core';
import 'echarts-liquidfill';
import styles from './index.module.less'

interface LiquidFillChartProps {
    label?: string;
    value: number;
}

export default function LiquidFillChart(props: LiquidFillChartProps) {
    const { label = '', value } = props;
    return (
        <div className={styles.container}>
            <div className={styles.label}>{label}</div>
            <ReactEChartsCore
                style={{ height: 200, width: '100%', background: '#fff' }}
                echarts={echarts}
                notMerge
                option={{
                    series: [
                        {
                            type: 'liquidFill',
                            data: [value],
                            radius: '150vw',
                            backgroundStyle: {
                                color: '#fff',
                            },
                            outline: {
                                show: true,
                                borderDistance: 8,
                                itemStyle: {
                                    color: 'none',
                                    borderColor: '#294D99',
                                    borderWidth: 8,
                                    shadowBlur: 0,
                                    shadowColor: '#fff',
                                },
                            },
                            itemStyle: {
                                opacity: 0.95,
                                shadowBlur: 0,
                                shadowColor: '#fff',
                            },
                            waveAnimation: false,
                            label: {
                                formatter: (param: any) => {
                                    return (param.value * 100).toFixed(2) + '%';
                                },
                                fontSize: 25,
                            },
                        },
                    ],
                }}
            />
        </div>
    );
}
