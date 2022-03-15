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
