import { PageContainer } from '@ant-design/pro-layout';
import { Result, Button, Checkbox, Form, Input, Row, Space } from 'antd';
import React from 'react';
import { useHistory } from 'react-router';

export function SpaceAccessFinish(props: any) {
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
                        <div>空间接管成功</div>
                    </div>
                }
            />
            ,
        </PageContainer>
    );
}
