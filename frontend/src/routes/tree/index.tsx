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

import { useState, useEffect } from 'react';
import { Tree, message } from 'antd';
import { TableOutlined, HddOutlined, HomeOutlined, SyncOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { TreeAPI } from './tree.api';
import { DataNode } from './tree.interface';
import { updateTreeData } from './tree.service';
import { ContentRouteKeyEnum } from './tree.data';
import EventEmitter from '@src/utils/event-emitter';
import { useNavigate } from 'react-router';

import { HeaderAPI } from '@src/components/common-header/header.api';
import styles from './tree.module.less';
import { delay } from '@src/utils/utils';
import { isSuccess } from '@src/utils/http';

// import { LoadingWrapper } from '@src/components/loadingwrapper/loadingwrapper';
const initTreeDate: DataNode[] = [];
export function MetaBaseTree() {
    const [treeData, setTreeData] = useState(initTreeDate);
    const [selectedKeys, setSelectedKeys] = useState<string[]>([])
    const [loading, setLoading] = useState(true);
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [syncLoading, setSyncLoading] = useState(false);
    useEffect(() => {
        initTreeData();
        EventEmitter.on('refreshData', initTreeData);
        EventEmitter.on('refreshTreeData', initTreeData);
    }, []);

    function initTreeData() {
        TreeAPI.getDatabaseList({ nsId: '0' }).then(res => {
            if (res.code === 0) {
                const num = Math.random();
                const database = res.data;
                const treeData: Array<DataNode> = [];
                database.forEach(item => {
                    treeData.push({
                        title: `${item.name}`,
                        key: `1¥${num}¥name¥${item.id}¥${item.name}`,
                        icon: <HddOutlined />,
                    });
                });
                setTreeData(treeData);
                const firstItem = database[0] || {}
                setSelectedKeys([treeData[0].key])
                navigate(`/meta/database/${firstItem.id}`, { state: { id: firstItem.id, name: firstItem.name } });
            } else {
                setTreeData([]);
                message.error(res.msg);
            }
            setLoading(false);
        });
    }

    function onLoadData(node: any) {
        const [storey, id, name, db_id, db_name] = node.key.split('¥');
        return TreeAPI.getTables({ dbId: db_id }).then(res => {
            if (res.code === 0) {
                const tables = res.data;
                const children: Array<any> = [];
                if (tables.length) {
                    tables.forEach((item: any) => {
                        children.push({
                            title: `${item.name}`,
                            key: `2¥${db_id}¥${db_name}¥${item.id}¥${item.name}`,
                            icon: <TableOutlined />,
                            isLeaf: true,
                        });
                    });
                } else {
                    children.push({
                        title: '',
                        key: '',
                        icon: '',
                        isLeaf: true,
                        className: styles['display_none'],
                    });
                }

                const trData = updateTreeData(treeData, node.key, children);
                setTreeData(trData);
            } else {
                message.error(res.msg);
            }
        });
    }

    function handleTreeSelect(keys: any[]) {
        if (keys.length > 0) {
            const [storey, db_id, db_name, id, name] = keys[0].split('¥');
            setSelectedKeys(keys)
            if (storey === '1') {
                localStorage.setItem('database_id', id);
                localStorage.setItem('database_name', name);
                navigate(`/meta/${ContentRouteKeyEnum.Database}/${id}`, { state: { id, name: name } });
            } else {
                localStorage.setItem('database_id', db_id);
                localStorage.setItem('database_name', db_name);
                localStorage.setItem('table_id', id);
                localStorage.setItem('table_name', name);
                navigate(`/meta/${ContentRouteKeyEnum.Table}/${id}`, { state: { id, name: name } });
            }
        }
    }

    function goHome() {
        const firstTreeNode = treeData[0]
        const [id, name] = firstTreeNode.key.split('¥');
        setSelectedKeys([firstTreeNode.key])
        navigate(`/meta/database/${id}`, { state: { id: id, name: name } });
    }

    function syncData() {
        setSyncLoading(true);
        Promise.all([delay(500), HeaderAPI.refreshData()])
            .then(res => {
                if (isSuccess(res[1])) {
                    message.success(t`Sync successfully, please refresh the page`);
                    return;
                }
                message.error(res[1].msg);
            })
            .catch(() => {
                message.error(t`Sync Failed`);
            })
            .finally(() => setSyncLoading(false));
    }

    return (
        <div className={styles['palo-tree-container']}>
            <h2 className={styles['palo-tree-title']}>
                <HomeOutlined onClick={goHome} />
                <span>{t`DataTree`}</span>
                <SyncOutlined spin={syncLoading} title='同步数据' style={{ color: '#1890ff', padding: 0 }} onClick={syncData} />
            </h2>
            <div className={styles['palo-tree-wrapper']}>
                <Tree
                    showIcon={true}
                    loadData={onLoadData}
                    selectedKeys={selectedKeys}
                    treeData={treeData}
                    className={styles['palo-side-tree']}
                    onSelect={selectedKeys => handleTreeSelect(selectedKeys)}
                />
            </div>
        </div>
    );
}
