import React, { useEffect, useState } from 'react';
import { Form, Input, message, Modal } from 'antd';
import { CopyOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { isSuccess } from '@src/utils/http';
import { UserAPI } from '../user.api';
import { copyText, generatePassword } from '../user.utils';
import styles from './list.module.less';

export function CreateOrEditRoleModal(props: any) {
    const { t, i18n } = useTranslation();
    const { onCancel, user } = props;
    const [loading, setLoading] = useState(false);
    const title = user ? t`editUser` : t`addUser`;

    useEffect(() => {
        props.form.setFieldsValue(user ? user : { name: '', email: '' });
    }, [user]);

    async function handleCreate(values: any) {
        setLoading(true);
        try {
            const password = generatePassword();
            const res = await UserAPI.createUser({ ...values, password });
            setLoading(false);
            if (isSuccess(res)) {
                message.success(t`createSuccess`);
                props.onSuccess && props.onSuccess();
                Modal.confirm({
                    title: t`pleaseSaveYourPassword`,
                    content: (
                        <div className={styles.clipInput}>
                            <Input.Password
                                readOnly
                                value={password}
                                addonAfter={
                                    <CopyOutlined
                                        onClick={() => {
                                            copyText(password);
                                            message.success(t`copySuccess`);
                                        }}
                                    />
                                }
                            />
                        </div>
                    ),
                });
            } else {
                message.error(res.msg);
            }
        } catch (err) {
            setLoading(false);
        }
    }
    async function handleEdit(values: any) {
        setLoading(true);
        const res = await UserAPI.updateUser({
            user_id: user.id,
            name: values.name,
            email: values.email,
        });
        setLoading(false);
        if (isSuccess(res)) {
            message.success(t`editSuccess`);
            props.onSuccess && props.onSuccess();
        } else {
            message.error(res.msg);
        }
    }
    return (
        <Modal
            title={title}
            visible={true}
            onCancel={onCancel}
            confirmLoading={loading}
            onOk={() => {
                props.form
                    .validateFields()
                    .then((values: any) => {
                        if (!user) {
                            handleCreate(values);
                        } else {
                            handleEdit(values);
                        }
                    })
                    .catch((info: any) => {
                        console.log('Validate Failed:', info);
                    });
            }}
        >
            <Form
                labelCol={{ span: i18n.language === 'zh' ? 4 : 5 }}
                wrapperCol={{ span: 18 }}
                form={props.form}
                name="form_in_modal"
                autoComplete="off"
            >
                <Form.Item
                    name="name"
                    label={t`username`}
                    rules={[
                        { required: true, message: t`pleaseInputUsername` },
                        { max: 19, message: t`usernameLengthMessage` },
                        {
                            pattern: /^[a-zA-Z0-9]+$/,
                            message: t`usernamePatternMessage`,
                        },
                    ]}
                >
                    <Input placeholder={t`pleaseInputUsername`} />
                </Form.Item>
                <Form.Item
                    name="email"
                    label={t`Mail`}
                    rules={[
                        { required: true, message: t`pleaseInputEmail` },
                        {
                            type: 'email',
                            message: t`emailTypeMessage`,
                        },
                    ]}
                >
                    <Input placeholder={t`pleaseInputEmail`} />
                </Form.Item>
            </Form>
        </Modal>
    );
}
