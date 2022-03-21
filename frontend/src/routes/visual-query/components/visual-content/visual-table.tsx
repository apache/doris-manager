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
