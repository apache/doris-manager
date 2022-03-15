import React, { useCallback, useEffect, useState } from 'react';
import { Input, Row, Col, Table, message } from 'antd';
import { useTranslation } from 'react-i18next';
import CheckModal from './check-modal';
import EditModal from './edit-modal';
import { useAsync } from '@src/hooks/use-async';
import * as ClusterAPI from '../cluster.api';
import { FlatBtn, FlatBtnGroup } from '@src/components/flatbtn';

export interface ConfigurationItem {
    name: string;
    nodes: string[];
    type: 'Frontend' | 'Backend';
    valueType: string;
    values: string[];
    hot: boolean;
}

const resolveConfigurationList = (configurationList: { rows: string[][] }) => {
    return configurationList?.rows?.reduce((memo, current) => {
        const index = memo.findIndex(item => item.name === current[0])
        if(index < 0) {
            memo.push({
              name: current[0],
              nodes: [current[1]],
              type: current[2] === 'FE' ? 'Frontend' : 'Backend',
              valueType: current[3],
              values: [current[current.length - 2]],
              hot: current[current.length - 1] === 'true'
            })
        }else {
            memo[index].nodes.push(current[1])
            memo[index].values.push(current[current.length - 2])
        }
        return memo
    }, [] as ConfigurationItem[]) || []
};

export default function Configuration() {
    const { t } = useTranslation();

    const {
        data: configurationList,
        loading: configurationListLoading,
        run: runGetConfigurationList,
    } = useAsync<ConfigurationItem[]>({ loading: true, data: [] });

    const [filteredConfigurationList, setFilteredConfigurationList] = useState<ConfigurationItem[]>();

    const getConfigurationList = useCallback(
        (setStartLoading: boolean = false) => {
            return runGetConfigurationList(
                Promise.all([ClusterAPI.getConfigurationList('fe'), ClusterAPI.getConfigurationList('be')]).then(
                    res => {
                        return [...resolveConfigurationList(res[0]), ...resolveConfigurationList(res[1])];
                    },
                ),
                { setStartLoading },
            ).catch(res => {
                message.error(res.msg);
            });
        },
        [runGetConfigurationList],
    );

    useEffect(() => {
        getConfigurationList();
    }, [getConfigurationList]);

    const [currentParameter, setCurrentParameter] = useState({} as ConfigurationItem);
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [checkModalVisible, setCheckModalVisible] = useState(false);
    const [searchString, setSearchString] = useState('');

    const columns = [
        {
            title: t`paramName`,
            dataIndex: 'name',
        },
        {
            title: t`paramType`,
            dataIndex: 'type',
            filters: [
                {
                    text: 'Frontend',
                    value: 'Frontend',
                },
                {
                    text: 'Backend',
                    value: 'Backend',
                },
            ],
            onFilter: (value: any, record: ConfigurationItem) => record.type === value,
        },
        {
            title: t`paramValueType`,
            dataIndex: 'valueType',
        },
        {
            title: t`hot`,
            dataIndex: 'hot',
            render: (hot: boolean) => (hot ? t`yes` : t`no`),
        },
        {
            title: t`operation`,
            key: 'operation',
            render: (record: ConfigurationItem) => (
                <FlatBtnGroup>
                    <FlatBtn onClick={handleCheck(record)}>{t`viewCurrentValue`}</FlatBtn>
                    <FlatBtn onClick={handleEdit(record)} disabled={!record.hot}>
                        {t`edit`}
                    </FlatBtn>
                </FlatBtnGroup>
            ),
        },
    ];

    const handleCheck = (record: ConfigurationItem) => () => {
        setCurrentParameter(record);
        setCheckModalVisible(true);
    };

    const handleEdit = (record: ConfigurationItem) => () => {
        setCurrentParameter(record);
        setEditModalVisible(true);
    };

    const handleSearch = () => {
        setFilteredConfigurationList(
            configurationList?.filter(item => item.name.toLowerCase().includes(searchString.toLowerCase())),
        );
    };

    return (
        <>
            <Row align="middle" gutter={20} style={{ marginBottom: 20 }}>
                <Col>{t`paramSearch`}</Col>
                <Col>
                    <Input.Search onChange={e => setSearchString(e.target.value)} onSearch={handleSearch} />
                </Col>
            </Row>
            <Table
                rowKey="name"
                loading={configurationListLoading}
                dataSource={filteredConfigurationList || configurationList}
                columns={columns}
            />
            <CheckModal
                visible={checkModalVisible}
                currentParameter={currentParameter}
                onCancel={() => setCheckModalVisible(false)}
            />
            <EditModal
                visible={editModalVisible}
                currentParameter={currentParameter}
                onOk={() => {
                    getConfigurationList(true).then(res => {
                        setFilteredConfigurationList(
                            res?.filter(item => item.name.toLowerCase().includes(searchString.toLowerCase())),
                        );
                    });
                }}
                onCancel={() => setEditModalVisible(false)}
            />
        </>
    );
}
