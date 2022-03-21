import React, { PropsWithChildren, useState } from 'react';

interface EditorContextProps {
    editorValue: string;
    setEditorValue: (v: string) => void;
}

export const EditorContext = React.createContext<EditorContextProps>({
    editorValue: '',
    setEditorValue: () => {},
});

export default function EditorContextProvider(props: PropsWithChildren<{}>) {
    const [editorValue, setEditorValue] = useState('');
    return <EditorContext.Provider value={{ editorValue, setEditorValue }}>{props.children}</EditorContext.Provider>;
}
