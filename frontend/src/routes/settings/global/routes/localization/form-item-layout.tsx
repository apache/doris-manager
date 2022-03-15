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
