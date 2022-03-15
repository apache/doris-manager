import { ExclamationCircleOutlined } from '@ant-design/icons';
import { UserInfoContext } from '@src/common/common.context';
import { FlatBtn } from '@src/components/flatbtn';
import { useRoleMember } from '@src/hooks/use-roles.hooks';
import { useSpaceUsers } from '@src/hooks/use-users.hooks';
import { isSuccess } from '@src/utils/http';
import { showName } from '@src/utils/utils';
import { Button, Table, Modal, Form, Select, message, Row } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { ColumnsType } from 'antd/lib/table';
import React, { useContext, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useRouteMatch } from 'react-router';
import { RoleAPI } from '../role.api';
import commonStyles from '../../people.less';

export function RoleMembers(props: any) {
    const { t } = useTranslation();
    const match = useRouteMatch<{ roleId: string }>();
    const { users } = useSpaceUsers();
    const { members, getRoleMembers, loading } = useRoleMember(match.params.roleId);
    const [visible, setVisible] = useState(false);
    const [confirmLoading, setConfirmLoading] = useState(false);
    const userInfo = useContext(UserInfoContext)!;
    const [form] = useForm();
    const isAllUser = members?.name.includes('All Users');
    const { confirm } = Modal;

    const filteredUsers = users?.filter(user => {
        const data = members.members.map(item => item.email);
        if (data.includes(user.email)) {
            return false;
        }
        return true;
    });

    const columns: ColumnsType<any> = [
        {
            title: t`members`,
            key: 'name',
            dataIndex: 'name',
        },
        {
            title: t`Mail`,
            dataIndex: 'email',
            key: 'email',
        },
    ];

    if (!isAllUser) {
        columns.push({
            title: t`operation`,
            key: 'actions',
            render: (record: any) => {
                const forbiddenEditRole =
                    members.name === 'All Users_1' ||
                    (members.name === 'Administrators_1' && members?.members.length <= 1) ||
                    userInfo.name === record.name;
                return (
                    <FlatBtn disabled={forbiddenEditRole} onClick={() => handleDelete(record)}>
                        {t`remove`}
                    </FlatBtn>
                );
            },
        });
    }

    async function handleCreate(values: any) {
        setConfirmLoading(true);
        const res = await RoleAPI.addMember({
            user_ids: values.user,
            group_id: +match.params.roleId,
        });
        setConfirmLoading(false);
        if (isSuccess(res)) {
            message.success(t`addSuccess`);
            setVisible(false);
            form.resetFields();
            getRoleMembers();
        } else {
            message.error(res.msg);
        }
    }

    function handleDelete(record: any) {
        confirm({
            title: t`removeFromRoleMembers`,
            icon: <ExclamationCircleOutlined />,
            content: t`removeFromRoleMembersMessage`,
            onOk() {
                return RoleAPI.deleteMember({ membership_id: record.membership_id }).then(res => {
                    if (isSuccess(res)) {
                        message.success(t`DeleteSuccessTips`);
                        getRoleMembers();
                    } else {
                        message.error(res.msg);
                    }
                });
            },
            okType: 'danger',
            onCancel() {
                console.log('Cancel');
            },
        });
    }

    return (
        <>
            <Row justify="space-between" style={{ marginBottom: 20 }}>
                <span style={{ fontWeight: 700, fontSize: 20 }}>{showName(members?.name)}</span>
                <Button
                    key="1"
                    type="primary"
                    onClick={() => {
                        setVisible(true);
                    }}
                >
                    {t`addMembers`}
                </Button>
            </Row>
            <Table
                pagination={false}
                rowKey="membership_id"
                bordered={true}
                columns={columns}
                size="middle"
                dataSource={members?.members}
                loading={loading}
            />
            {visible && (
                <Modal
                    title={t`addMembers`}
                    visible={true}
                    onCancel={() => setVisible(false)}
                    confirmLoading={confirmLoading}
                    onOk={() => {
                        form.validateFields()
                            .then(values => {
                                handleCreate(values);
                            })
                            .catch(info => {
                                console.log('Validate Failed:', info);
                            });
                    }}
                >
                    <Form form={form} name="form_in_modal">
                        <Form.Item
                            name="user"
                            label={t`users`}
                            rules={[{ required: true, message: t`pleaseSelectUsers` }]}
                        >
                            <Select
                                showSearch
                                mode="multiple"
                                // style={{ width: 200 }}
                                placeholder={t`pleaseSelectUsers`}
                                optionFilterProp="title"
                                optionLabelProp="label"
                                filterOption={(input, option) => {
                                    return (option?.title as string).toLowerCase().indexOf(input.toLowerCase()) >= 0;
                                }}
                            >
                                {filteredUsers?.map((user, index) => {
                                    return (
                                        <Select.Option
                                            key={user.id}
                                            label={user.common_name}
                                            value={user.id}
                                            title={user.common_name}
                                        >
                                            <Row justify="space-between">
                                                <div className={commonStyles.optionContent}>
                                                    <strong>{user.common_name}</strong>
                                                    <span>{user.email ? `(${user.email})` : ''}</span>
                                                </div>
                                            </Row>
                                        </Select.Option>
                                    );
                                })}
                            </Select>
                        </Form.Item>
                    </Form>
                </Modal>
            )}
        </>
    );
}
