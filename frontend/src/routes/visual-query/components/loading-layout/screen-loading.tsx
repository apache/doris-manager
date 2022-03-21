import React from 'react';
import { Spin } from 'antd';
import styles from './style.module.less';

export default function ScreenLoading() {
    return (
        <div className={styles.loadingWrapper}>
            <Spin />
        </div>
    );
}
