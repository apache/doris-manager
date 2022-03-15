import { Typography } from 'antd';
import React, { PropsWithChildren } from 'react';

interface FormLayoutProps {
    title: string;
}

export default function FormLayout(props: PropsWithChildren<FormLayoutProps>) {
    const { title, children } = props;
    return (
        <div>
            <Typography.Title level={4} style={{ fontSize: 16, marginBottom: 24 }}>
                {title}
            </Typography.Title>
            {children}
        </div>
    );
}
