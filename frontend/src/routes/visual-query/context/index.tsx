import React, { PropsWithChildren } from 'react';
import DatabasesContextProvider from './databases-context';
import DataContextProvider from './data-context';
import EditorContextProvider from './editor-context';

const providers = [
    DatabasesContextProvider,
    DataContextProvider,
    EditorContextProvider,
];

export default function VisualQueryProvider(props: PropsWithChildren<{}>) {
    return (
        <>
            {providers.reduce((memo, Provider) => {
                return <Provider>{memo}</Provider>;
            }, props.children)}
        </>
    );
}

export * from './databases-context';
export * from './data-context';
export * from './editor-context';
