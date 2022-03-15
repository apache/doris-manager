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

import { EditableProTable, ProColumns } from '@ant-design/pro-table';
import { Row, Button, Input, message, Modal } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import React, { useEffect, useRef, useState } from 'react';
type DataSourceType = {
    id: React.Key;
    ip: string;
    order: number;
};
const reg =
    /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;

export function NodeList(props: { value?: any; onChange?: any }) {
    const [dataSource, setDataSource] = useState<DataSourceType[]>([]);
    const [position, setPosition] = useState<'top' | 'bottom' | 'hidden'>('bottom');
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editableKeys, setEditableRowKeys] = useState<React.Key[]>([]);
    const [ipList, setIpList] = useState('');

    const columns: ProColumns<DataSourceType>[] = [
        {
            title: '序号',
            dataIndex: 'order',
            editable: false,
            width: '30%',
            render: (dom, rowData, index) => {
                return <span>{`${index}`}</span>;
            },
        },
        {
            title: 'IP地址',
            dataIndex: 'ip',
            renderFormItem: (dom, rowData, index) => <IpInput value={rowData.ip} />,
            render: (dom, rowData, index) => {
                return <span className="customRender">{`${rowData.ip}`}</span>;
            },
        },
        {
            title: '操作',
            valueType: 'option',
            width: 200,
            render: (text, record, _, action) => [
                <a
                    key="editable"
                    onClick={() => {
                        action?.startEditable?.(record.id);
                    }}
                >
                    编辑
                </a>,
                <a
                    key="delete"
                    onClick={() => {
                        setDataSource(dataSource.filter(item => item.id !== record.id));
                    }}
                >
                    删除
                </a>,
            ],
        },
    ];

    const IpInput: React.FC<{
        value?: string;
        onChange?: (value: string) => void;
    }> = ({ value, onChange }) => {
        const ref = useRef<Input | null>(null);
        const handleInputConfirm = (val: string) => {
            if (reg.test(val)) {
                if (dataSource.filter(item => item.ip === val).length) {
                    message.error('此ip已存在!');
                } else {
                    onChange?.(val);
                }
            } else {
                message.error('请确认ip输入是否正确');
            }
        };

        return (
            <Input
                ref={ref}
                style={{ width: 150 }}
                defaultValue={value}
                onBlur={e => handleInputConfirm(e.target.value)}
            />
        );
    };

    useEffect(() => {
        props?.onChange(dataSource.map(item => item.ip));
    }, [dataSource.length]);

    function handleOk() {
        let baseLength = dataSource.length || 0;
        let hosts = [];
        let ips = [...new Set(ipList.split(/[,，\s\n]/))]
            .filter(item => reg.test(item))
            .map((item, index) => ({
                id: (Math.random() * 1000000).toFixed(0),
                order: baseLength + index + 1,
                ip: item,
            }));
        if (baseLength) {
            let tmp = ips.reduce((cur: { id: string; order: number; ip: string }[], next) => {
                if (!dataSource.filter(item => item.ip === next.ip).length) {
                    return [...cur, next];
                }
                return [...cur];
            }, []);
            hosts = [...dataSource, ...tmp];
            setDataSource(hosts);
        } else {
            hosts = [...ips];
            setDataSource(hosts);
        }
        setIpList('');
        setIsModalVisible(false);
    }
    return (
        <div>
            <Row justify="end" style={{ marginRight: 100, marginBottom: 20 }}>
                <Button
                    type="primary"
                    onClick={() => {
                        setIsModalVisible(true);
                    }}
                >
                    批量添加
                </Button>
            </Row>
            <EditableProTable<DataSourceType>
                rowKey="id"
                maxLength={5}
                recordCreatorProps={{
                    position: position as 'top',
                    record: () => ({
                        id: (Math.random() * 1000000).toFixed(0),
                        order: dataSource.length + 1,
                        ip: '',
                    }),
                }}
                columns={columns}
                value={dataSource}
                onChange={value => {
                    const hosts = value.map(item => item.ip);
                    setDataSource(value.filter(item => item.ip));
                }}
                editable={{
                    type: 'multiple',
                    editableKeys,
                    onSave: async (rowKey, data, row) => {
                        console.log(rowKey, data, row);
                    },
                    onChange: setEditableRowKeys,
                }}
            />
            <Modal title="批量添加" visible={isModalVisible} onOk={handleOk} onCancel={() => setIsModalVisible(false)}>
                <TextArea
                    placeholder="请输入IP地址列表，支持逗号与换行符分割"
                    rows={5}
                    value={ipList}
                    onChange={e => {
                        setIpList(e.target.value);
                    }}
                />
            </Modal>
        </div>
    );
}
