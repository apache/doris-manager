import { Input, Row, Space, Switch } from "antd";
import React, { useState } from "react";
import { CustomConfigProps } from "./custom-config.interface";

export function CustomConfig(props: CustomConfigProps) {
    const [showCustomConfig, setShowCustomConfig] = useState(false);
    return (
        <>
            <Row style={{ margin: '20px 0' }}>
                <Space>
                    <h3>自定义配置</h3>
                    <Switch
                        style={{ marginLeft: 40 }}
                        checkedChildren="开"
                        unCheckedChildren="关"
                        defaultChecked={false}
                        onChange={(checked: boolean) => {
                            setShowCustomConfig(checked);
                        }}
                    />
                </Space>
            </Row>
            {
                showCustomConfig && (
                    <Input.TextArea onChange={e => {
                        props.onChange && props.onChange({
                            value: e.target.value,
                            showCustomConfig,
                        });
                    }} rows={5} placeholder="请输入您的自定义配置项，若存在冲突，自定义配置将覆盖上方默认配置 格式: xxx=xxx" />
                )
            }
            
        </>
    );
}
