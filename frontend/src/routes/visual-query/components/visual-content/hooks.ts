// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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
        // const currentDatabase = databases?.find(database => database.id === selectedDatabaseId);
        const SUGGESTIONS = [
            ...DEFAULT_SUGGESTIONS,
            // ...currentDatabase.tables.map((table: any) => ({
            //     label: table.name,
            //     insertText: table.name,
            //     detail: '表',
            // })),
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
