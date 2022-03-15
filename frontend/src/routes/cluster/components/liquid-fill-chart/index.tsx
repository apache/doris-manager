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
