import React, { useContext, useState } from 'react';
import { Button, Table, message, Modal, Row } from 'antd';
import moment from 'moment';
import { useTranslation } from 'react-i18next';
import StatusMark from '@src/components/status-mark';
import { UserInfoContext } from '@src/common/common.context';
import CreateModal from './create-modal';
import { removeMemberFromSpaceAPI } from './user.api';
import { useSpaceMembers } from './user.hooks';
import { FlatBtn } from '@src/components/flatbtn';

export interface IUser {
    id: number;
    name: string;
    email: string;
    last_login: string;
    is_active: boolean;
}

export function User() {
    const {t} = useTranslation()
    const userInfo = useContext(UserInfoContext)!;
    const { users = [], spaceMembers = [], getSpaceMembers, loading } = useSpaceMembers(userInfo);
    const [modalVisible, setModalVisible] = useState(false);

    const filteredUsers = users.filter(user => !spaceMembers.find(member => member.id === user.id));

    const columns = [
        {
            title: t`username`,
            key: 'name',
            dataIndex: 'name',
        },
        {
            title: t`Mail`,
            dataIndex: 'email',
            key: 'email',
        },
        {
            title: t`status`,
            dataIndex: 'is_active',
            filters: [
                { text: t`activated`, value: true },
                { text: t`deactivated`, value: false },
            ],
            render: (is_active: boolean) => (
                <StatusMark status={is_active ? 'success' : 'deactivated'}>{is_active ? t`activated` : t`deactivated`}</StatusMark>
            ),
            onFilter: (value: any, record: any) => record.is_active === value,
        },
        {
            title: t`lastLogin`,
            dataIndex: 'last_login',
            key: 'last_login',
            render: (last_login: string) => {
                return (
                    <span>{last_login == null ? t`neverLoggedIn` : moment(last_login).format('YYYY-MM-DD HH:mm:ss')}</span>
                );
            },
        },
        {
            title: t`operation`,
            key: 'actions',
            render: (record: IUser) => {
                const disabled = userInfo.id === record.id;
                return (
                    <FlatBtn disabled={disabled} onClick={handleRemove(record.id)}>
                        {t`removeMember`}
                    </FlatBtn>
                );
            },
        },
    ];

    const handleRemove = (userId: number) => () => {
        Modal.confirm({
            title: t`removeMemberModalTitle`,
            onOk: () => {
                return removeMemberFromSpaceAPI(userId)
                    .then(() => {
                        message.success(t`removeSuccess`);
                        getSpaceMembers();
                    })
                    .catch(() => {
                        message.error('removeFailed');
                    });
            },
        });
    };

    return (
        <>
            <Row justify="end" style={{ marginBottom: 20 }}>
                <Button type="primary" onClick={() => setModalVisible(true)}>
                    {t`addMembers`}
                </Button>
            </Row>
            <Table
                pagination={false}
                dataSource={spaceMembers}
                columns={columns}
                rowKey="id"
                bordered={true}
                loading={loading}
            />
            <CreateModal
                visible={modalVisible}
                users={filteredUsers}
                onCancel={() => setModalVisible(false)}
                getSpaceMembers={getSpaceMembers}
            />
        </>
    );
}
