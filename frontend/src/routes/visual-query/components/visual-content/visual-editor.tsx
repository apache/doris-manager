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

import React, { useContext, useState } from 'react';
import { Select, Row, Col, Button, message } from 'antd';
import { Resizable } from 're-resizable';
import Editor from '@monaco-editor/react';
import { DatabasesContext, DataContext, EditorContext } from '../../context';
import { THEME_NAME, RESIZEABLE_ENABLE } from './constants';
import { useMonacoEditor } from './hooks';
import { fetchData } from '../../visual-query.api';

const { Option } = Select;

interface VisualEditorProps {
    onResize: () => void;
}

export default function VisualEditor(props: VisualEditorProps) {
    const { onResize } = props;
    const { selectedDatabaseId, setSelectedDatabaseId, databases } = useContext(DatabasesContext);
    const { runFetchData } = useContext(DataContext);
    const { editorValue, setEditorValue } = useContext(EditorContext);
    useMonacoEditor();

    const runSqlQuery = () => {
        if (selectedDatabaseId == null) {
            message.error('请选择一个数据库');
            return;
        }
        if (editorValue === '') {
            message.error('请输入查询语句');
            return;
        }
        runFetchData(fetchData({ database: selectedDatabaseId, query: editorValue })).catch(
            () => message.error('查询失败'),
        );
    };

    return (
        <>
            <Select
                value={selectedDatabaseId || undefined}
                style={{ width: 200, margin: '20px 0 0 20px' }}
                placeholder="请选择一个数据库"
                onChange={v => setSelectedDatabaseId(v)}
            >
                {databases?.map(database => (
                    <Option value={database.id} key={database.id}>
                        {database.name}
                    </Option>
                ))}
            </Select>
            <Resizable
                style={{ border: '1px solid #ccc', margin: 20 }}
                enable={RESIZEABLE_ENABLE}
                defaultSize={{
                    width: '95%',
                    height: 150,
                }}
                minHeight={150}
                onResize={onResize}
            >
                <Editor
                    height="100%"
                    language="sql"
                    theme={THEME_NAME}
                    options={{
                        minimap: {
                            enabled: false,
                        },
                    }}
                    value={editorValue}
                    onChange={v => setEditorValue(v || '')}
                />
            </Resizable>
            <Row justify="end">
                <Col style={{ marginRight: '4%' }}>
                    <Button
                        type="primary"
                        disabled={selectedDatabaseId == null || editorValue === ''}
                        onClick={runSqlQuery}
                    >
                        运行
                    </Button>
                </Col>
            </Row>
        </>
    );
}
