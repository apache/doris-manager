import React, { useContext, useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Button, Form, Input, Modal, Space } from 'antd';
import { NewSpaceInfoContext } from '@src/common/common.context';
import { SpaceAPI } from '@src/routes/space/space.api';
import styles from '../../../space.less';
import { stepDisabledState } from '../../access-cluster.recoil';
import { useRecoilState } from 'recoil';

const tip = {
    default: '请进行链接测试',
    fault: '链接测试未通过'
}

export function ConnectCluster(props: any) {
    const { form, reqInfo, step } = useContext(NewSpaceInfoContext);
    const [testFlag, setTestFlag] = useState<any>('none');
    const [stepDisabled, setStepDisabled] = useRecoilState(stepDisabledState);

    useEffect(() => {
        form.setFieldsValue({...reqInfo.clusterAccessInfo});
        setStepDisabled({...stepDisabled, next: true});
    }, [reqInfo.cluster_id, step]);


    const handleLinkTest = () => {
        const values = form.getFieldsValue();
        SpaceAPI.spaceValidate({
            address: values.address.trim(),
            httpPort: values.httpPort,
            passwd: values.passwd || '',
            queryPort: values.queryPort,
            user: values.user.trim(),
        }).then(res => {
            const { msg, data, code } = res;
            if (code === 0) {
                Modal.success({
                    title: "集群连接成功",
                    content: msg,
                });
                setStepDisabled({...stepDisabled, next: false});
                setTestFlag('success')
            } else {
                Modal.error({
                    title: "集群连接失败",
                    content: msg,
                });
                setStepDisabled({...stepDisabled, next: true});
                setTestFlag('failed');
            }
        });
    }

    return (
        <PageContainer
            header={{
                title: '连接集群',
            }}
        >
            <Form
                form={form}
                name="basic"
                layout="horizontal"
                labelCol={{ span: 2 }}
                wrapperCol={{ span: 8 }}
                autoComplete="off"
                // initialValues={{
                //     address: '10.138.64.225',
                //     httpPort: 8030,
                //     queryPort: 9030,
                //     user: 'root',
                //     passwd: 'palo_123'
                // }}
                initialValues={{
                    address: reqInfo.clusterAccessInfo?.address,
                    httpPort: reqInfo.clusterAccessInfo?.httpPort,
                    queryPort: reqInfo.clusterAccessInfo?.queryPort,
                    user: reqInfo.clusterAccessInfo?.user,
                    passwd: reqInfo.clusterAccessInfo?.passwd
                }}
            >
                <Form.Item name="address" label="集群地址" required >
                    <Input placeholder="please input cluster address" />
                </Form.Item>
                <Form.Item name="httpPort" label="HTTP端口" required>
                    <Input placeholder="please input cluster http port" />
                </Form.Item>
                <Form.Item name="queryPort" label="JDBC端口" required>
                    <Input placeholder="please input cluster jdbc port" />
                </Form.Item>
                <Form.Item name="user" label="集群用户名" required>
                    <Input placeholder="please input cluster user" />
                </Form.Item>
                <Form.Item name="passwd" label="集群密码">
                    <Form.Item name="passwd" noStyle>
                        <Input.Password  placeholder="please input cluster user password"/>
                    </Form.Item>
                </Form.Item>
            </Form>
            <Space style={{marginTop: 20, marginLeft: 82}}>
                <Button onClick={() => handleLinkTest()}>链接测试</Button>
                    &nbsp;  
                {
                    (testFlag !== 'success') && <Space>
                        <div className={styles['light']}></div>
                        <div className={styles['light-tip']}>{testFlag === 'failed' ? tip.fault: tip.default}</div> 
                    </Space>
                }
            </Space>
        </PageContainer>
    );
}

