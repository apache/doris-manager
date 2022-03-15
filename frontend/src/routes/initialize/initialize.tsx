import { AuthTypeEnum } from '@src/common/common.data';
import { isSuccess } from '@src/utils/http';
import { Button, Card, Input, message, Radio, Row, Space, Steps } from 'antd';
import React, { useEffect, useState } from 'react';
import { useHistory, useRouteMatch } from 'react-router';
import { InitializeAPI } from './initialize.api';
import { LDAPStepsEnum, StudioStepsEnum } from './initialize.data';
import styles from './initialize.less';

const { Step } = Steps;
export function InitializePage(props: any) {
    const [authType, setAuthType] = useState<AuthTypeEnum>(AuthTypeEnum.STUDIO);
    const match = useRouteMatch();
    const history = useHistory();
    async function handleSetAuthType() {
        const res = await InitializeAPI.setAuthType({authType});
        if (isSuccess(res)) {
            history.push(`${match.path}auth/${authType}`);
        } else {
            message.error(res.msg);
        }
    }

    return (
        <div className={styles['initialize']}>
            <div className={styles['initialize-steps-content']}>
                <Card type="inner" title="管理用户">
                    <Radio.Group onChange={e => setAuthType(e.target.value)} value={authType}>
                        <Space direction="vertical">
                            <Radio value={AuthTypeEnum.STUDIO}>本地认证</Radio>
                            {/* <Radio value={AuthTypeEnum.LDAP}>LDAP认证</Radio> */}
                        </Space>
                    </Radio.Group>
                    <p style={{marginTop: 10}}>注意，初始化选择好认证方式后不可再改变。</p>
                    <Row justify="end">
                        <Button type="primary" onClick={() => handleSetAuthType()}>去配置</Button>
                    </Row>
                </Card>
            </div>
        </div>
    );
}
