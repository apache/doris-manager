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

import { isSuccess } from '@src/utils/http';
import { Form, Input, message, Modal } from 'antd';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { RoleAPI } from '../role.api';

export function CreateOrEditRoleModal(props: any) {
    const { t } = useTranslation();
    const { onCancel, role } = props;
    const [loading, setLoading] = useState(false);
    const title = role ? t`editRole` : t`createRole`;

    useEffect(() => {
        props.form.setFieldsValue({ name: props.role ? props.role.name : '' });
    }, [props.role]);

    async function handleCreate(values: any) {
        setLoading(true);
        try {
            const res = await RoleAPI.createRole(values);
            setLoading(false);
            if (isSuccess(res)) {
                message.success(t`createSuccess`);
                props.onSuccess && props.onSuccess();
            } else {
                message.error(res.msg);
            }
        } catch (err) {
            setLoading(false);
        }
    }
    async function handleEdit(values: any) {
        setLoading(true);
        const res = await RoleAPI.updateRole({
            id: (role as any).id,
            name: values.name,
        });
        setLoading(false);
        if (isSuccess(res)) {
            message.success(t`SuccessfullyModified`);
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
                        if (!role) {
                            handleCreate(values);
                        } else {
                            console.log(values);
                            handleEdit(values);
                        }
                    })
                    .catch((info: any) => {
                        console.log('Validate Failed:', info);
                    });
            }}
        >
            <Form form={props.form} name="form_in_modal">
                <Form.Item
                    name="name"
                    label="名称"
                    rules={[
                        { required: true, message: t`pleaseInputRoleName` },
                        { min: 1, max: 20, message: t`roleNameLengthMessage` },
                        {
                            pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
                            message: t`roleNamePatternMessage`,
                        },
                    ]}
                >
                    <Input placeholder={t`pleaseInputRoleName`} />
                </Form.Item>
            </Form>
        </Modal>
    );
}
