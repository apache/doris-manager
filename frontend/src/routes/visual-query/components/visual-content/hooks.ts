import { useEffect, useContext, useCallback, useRef } from 'react';
import { useMonaco } from '@monaco-editor/react';
import { THEME_NAME, MONACO_EDITOR_THEME, DEFAULT_SUGGESTIONS } from './constants';
import { DatabasesContext } from '../../context';
import { useCallbackRef } from '../../hooks';

export function useMonacoEditor() {
    const { selectedDatabaseId, databases } = useContext(DatabasesContext);
    const monaco = useMonaco();

    useEffect(() => {
        if (monaco) {
            monaco.editor.defineTheme(THEME_NAME, MONACO_EDITOR_THEME as any);
            monaco.editor.setTheme(THEME_NAME);
        }
    }, [monaco]);

    const provideCompletionItems = useCallbackRef((model: any, position: any) => {
        const textUntilPosition = model.getValueInRange({
            startLineNumber: position.lineNumber,
            startColumn: 1,
            endLineNumber: position.lineNumber,
            endColumn: position.column,
        });
        const textArr = textUntilPosition.split(' ');
        const lastText = textArr[textArr.length - 1];
        const currentDatabase = databases?.find(database => database.id === selectedDatabaseId);
        const SUGGESTIONS = [
            ...DEFAULT_SUGGESTIONS,
            ...currentDatabase.tables.map((table: any) => ({
                label: table.name,
                insertText: table.name,
                detail: '表',
            })),
        ];
        const match = SUGGESTIONS.some((item: any) => {
            // 循环判断是否包含在补全数组中
            return item.label.indexOf(lastText) > 0;
        });
        const suggestion = match ? SUGGESTIONS : [];
        return {
            suggestions: suggestion.map(item => ({
                ...item,
                kind: monaco?.languages.CompletionItemKind.Variable,
            })),
        } as any;
    });

    useEffect(() => {
        if (monaco) {
            monaco.languages.registerCompletionItemProvider('sql', {
                provideCompletionItems,
            });
        }
    }, [monaco, provideCompletionItems]);
}
