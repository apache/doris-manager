import React, {useEffect, useState} from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { useHistory, useParams } from 'react-router';
import API from '../new-cluster.api';
import { Button } from 'antd';

export function Logs(props: any) {
    const history = useHistory();
    const params = useParams<{taskId: string}>();
    const [logText, setlogText] = useState('');

    useEffect(() => {
        API.getTaskLog(params.taskId).then(res => {
            setlogText(res.data.log)
        })
    }, [])
    return (
        <PageContainer
            header={{
                title: '日志',
            }}
            extra={[
                <Button key="1" type="primary" onClick={() => {history.goBack()}}>返回</Button>,
              ]}
        >
            <div style={{width: '100%', background: '#000', color: '#fff', height:'80vh', overflow: "scroll"}}>
                <code>{logText}</code>
            </div>
        </PageContainer>
    );
}
