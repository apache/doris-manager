import { PageContainer } from '@ant-design/pro-layout';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { Result, Button, Checkbox, Form, Input, Row, Space } from 'antd';
import React, { useContext } from 'react';
import { useHistory } from 'react-router';

export function SpaceCreateFinish(props: any) {
    const { form } = useContext(NewSpaceInfoContext);
    const history = useHistory();
    const onFinish = (values: any) => {
        console.log('Success:', values);
    };
    return (
        <PageContainer
            header={{
                title: <h2>完成创建</h2>,
            }}
        >
            <Result
                status="success"
                title={
                    <div>
                        <div>空间创建成功</div>
                        <div>请设定集群root密码</div>
                    </div>
                }
                extra={[
                    <>
                        <Form
                            form={form}
                            name="basic"
                            labelCol={{ span: 10 }}
                            wrapperCol={{ span: 5 }}
                            initialValues={{ remember: true }}
                            onFinish={onFinish}
                            autoComplete="off"
                        >
                            <Form.Item
                                label="集群密码"
                                name="clusterPassword"
                                rules={[{ required: true, message: '用于集群初始root与admin用户' }]}
                            >
                                <Input.Password />
                            </Form.Item>
                        </Form>
                    </>,
                ]}
            />
            ,
        </PageContainer>
    );
}
