import { Typography, Row } from 'antd';
import React, { PropsWithChildren } from 'react';

interface SettingItemLayoutProps {
    title?: string;
    description?: string;
}

export default function SettingItemLayout(props: PropsWithChildren<SettingItemLayoutProps>) {
    const { title = '', description = '', children } = props;

    return (
        <div style={{ marginBottom: 20 }}>
            {title && (
                <Typography.Title level={5} style={{ fontSize: 14 }}>
                    {title}
                </Typography.Title>
            )}
            {description && <Typography.Text style={{ fontSize: 14 }}>{description}</Typography.Text>}
            <Row style={{ marginTop: 10 }}>{children}</Row>
        </div>
    );
}
