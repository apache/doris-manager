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

/** @format */

import React, { useState, useEffect } from 'react';
import styles from './tabs.module.less';
import CSSModules from 'react-css-modules';
import { Form, message } from 'antd';
import { useHistory } from 'react-router-dom';
import { TableAPI } from '../table.api';
import { TableInfoResponse } from '../table.interface';
import EventEmitter from '@src/utils/event-emitter';
import { getShowTime } from '../../../utils/utils';
import { Helper } from '@src/components/helper/helper';
import { useTranslation } from 'react-i18next';
const layout = {
    labelCol: { span: 6 },
    wrapperCol: { span: 18 },
};

function Table(props: any) {
    const { t } = useTranslation()
    // const history = useHistory();
    const [tableInfo, setTableInfo] = useState<TableInfoResponse>({
        createTime: '',
        creator: '',
        describe: '',
        name: '',
        updateTime: '',
        dbId: '',
        dbName: '',
    });
    useEffect(() => {
        refresh(props.tableId);
        const unListen = EventEmitter.on('refreshData', () => {
            refresh(props.tableId);
        });
        return unListen;
    }, []);
    function refresh(id: string) {
        TableAPI.getTableInfo({ tableId: id }).then(res => {
            const { msg, code, data } = res;
            if (code === 0) {
                setTableInfo(res.data);
            } else {
                message.error(msg);
            }
        });
    }
    return (
        <div styleName="table-content-des">
            <Form {...layout} labelAlign="left">
                <Form.Item label={t`TableName`}>{tableInfo.name}</Form.Item>
                <Form.Item label={t`TableDescription`}>{tableInfo.describe ? tableInfo.describe : '-'}</Form.Item>
                <Form.Item label={t`CreateTime`}>{getShowTime(tableInfo.createTime)? getShowTime(tableInfo.createTime) : '-'}</Form.Item>
                <Form.Item label={t`UpdateTime`}>
                    {getShowTime(tableInfo.updateTime)}
                    {tableInfo.updateTime ? (
                        <Helper title={t`UpdateTimeHelper`} className={styles['table-content-tips']} />
                    ) : (
                        '-'
                    )}
                </Form.Item>
            </Form>
        </div>
    );
}

export default CSSModules(styles)(Table);
