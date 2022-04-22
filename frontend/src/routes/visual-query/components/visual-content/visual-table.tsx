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

import React, { useContext, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { Table } from 'antd';
import { DataContext } from '../../context';

function VisualTable(props: any, ref: any) {
    const { columns, dataSource } = useContext(DataContext);

    const tableWrapperRef = useRef<HTMLDivElement | null>(null);

    const [tableWidth, setTableWidth] = useState(1100);
    const [scrollY, setScrollY] = useState(200);

    useEffect(() => {
        if (tableWrapperRef.current) {
            setTableWidth(tableWrapperRef.current.clientWidth);
            setScrollY(tableWrapperRef.current.clientHeight - 120);
        }
    }, []);

    useImperativeHandle(ref, () => ({
        refreshTableHeight: setScrollY,
        refreshTableWidth: setTableWidth,
    }));

    return (
        <div style={{ height: '100%', margin: '20px 0 0 20px' }} ref={tableWrapperRef}>
            <div style={{ width: tableWidth, height: '100%' }}>
                <Table
                    style={{ width: '100%', height: '100%' }}
                    columns={columns}
                    dataSource={dataSource}
                    scroll={{ y: scrollY, x: tableWidth - 20 }}
                    pagination={{ size: 'small', pageSize: 50, showSizeChanger: false }}
                    size="small"
                />
            </div>
        </div>
    );
}

export default React.forwardRef(VisualTable);
