import React, { PropsWithChildren } from 'react';
import ScreenLoading from './screen-loading';

interface LoadingLayoutProps {
    loading: boolean;
}

export default function LoadingLayout(props: PropsWithChildren<LoadingLayoutProps>) {
    const { loading, children } = props;
    return <>{loading ? <ScreenLoading /> : children}</>;
}
