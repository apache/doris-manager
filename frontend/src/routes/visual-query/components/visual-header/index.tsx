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
