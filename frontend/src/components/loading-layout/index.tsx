import React, { CSSProperties, PropsWithChildren } from 'react';
import { Spin } from 'antd';

interface LoadingLayoutProps {
    loading?: boolean;
    wrapperStyle?: CSSProperties;
    tip?: string;
}

export default function LoadingLayout(props: PropsWithChildren<LoadingLayoutProps>) {
    const { loading = false, wrapperStyle = {}, children, tip } = props;
    return (
        <div>
            {loading ? (
                <div style={wrapperStyle}>
                    <Spin tip={tip} />
                </div>
            ) : (
                children
            )}
        </div>
    );
}
