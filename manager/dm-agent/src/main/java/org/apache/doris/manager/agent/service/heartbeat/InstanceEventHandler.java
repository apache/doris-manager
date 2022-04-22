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

package org.apache.doris.manager.agent.service.heartbeat;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventInfo;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResult;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResultType;
import org.apache.doris.manager.common.heartbeat.config.InstanceDeployCheckEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceInstallEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceRestartEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceStartEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.config.InstanceStopEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.stage.InstanceDeployEventStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InstanceEventHandler {
    @Autowired
    private DorisInstanceOperator instanceOperator;

    public HeartBeatEventResult handleInstanceHeartBeat(HeartBeatEventInfo eventInfo) {
        switch (eventInfo.getEventType()) {
            case INSTANCE_DEPLOY_CHECK:
                return handleCheckDeployEvent(eventInfo);
            case INSTANCE_INSTALL:
                return handleInstallEvent(eventInfo);
            case INSTANCE_START:
                return handleStartEvent(eventInfo);
            case INSTANCE_STOP:
                return handleStopEvent(eventInfo);
            case INSTANCE_RESTART:
                return handleRestartEvent(eventInfo);
            default:
                return null;
        }
    }

    private HeartBeatEventResult handleCheckDeployEvent(HeartBeatEventInfo eventInfo) {
        String jsonConfigStr = JSON.toJSONString(eventInfo.getConfigInfo());
        InstanceDeployCheckEventConfigInfo configInfo = JSON.parseObject(jsonConfigStr,
                InstanceDeployCheckEventConfigInfo.class);

        boolean isDeploy = true;
        try {
            // here we do not check http service is ready or not
            // because it is ready after a few seconds of the process starts
            // which may cause `Doris Cluster Creation Request` failed and blocked
            instanceOperator.checkInstanceProcessState(configInfo.getModuleName(),
                    configInfo.getInstallInfo(), 0);
        } catch (Exception e) {
            log.error("check instance {} deploy error: {}", configInfo.getModuleName(), e.getMessage());
            isDeploy = false;
        }

        HeartBeatEventResult result = new HeartBeatEventResult(eventInfo);
        // There is only one step
        setOperationResult(isDeploy, result, "Instance deployment information succeeded",
                "Instance deployment information exception", eventInfo.getEventStage(), true);
        return result;
    }

    private HeartBeatEventResult handleInstallEvent(HeartBeatEventInfo eventInfo) {
        String jsonConfigStr = JSON.toJSONString(eventInfo.getConfigInfo());
        InstanceInstallEventConfigInfo configInfo = JSON.parseObject(jsonConfigStr,
                InstanceInstallEventConfigInfo.class);
        HeartBeatEventResult result = new HeartBeatEventResult(eventInfo);

        if (eventInfo.getEventStage() == InstanceDeployEventStage.PACKAGE_DEPLOY.getStage()) {
            boolean isDownload = instanceOperator.downloadInstancePackage(configInfo.getModuleName(),
                    configInfo.getInstallInfo(), configInfo.getPackageDir());
            setOperationResult(isDownload, result, InstanceDeployEventStage.PACKAGE_DEPLOY.getMessage(),
                    InstanceDeployEventStage.PACKAGE_DEPLOY.getError(), eventInfo.getEventStage(),
                    InstanceDeployEventStage.PACKAGE_DEPLOY.isLast());

        } else if (eventInfo.getEventStage() == InstanceDeployEventStage.INSTANCE_CONFIG.getStage()) {
            boolean isConfig = instanceOperator.configInstance(configInfo.getModuleName(), configInfo.getInstallInfo(),
                    configInfo.getParms());
            setOperationResult(isConfig, result, InstanceDeployEventStage.INSTANCE_CONFIG.getMessage(),
                    InstanceDeployEventStage.INSTANCE_CONFIG.getError(), eventInfo.getEventStage(),
                    InstanceDeployEventStage.INSTANCE_CONFIG.isLast());
        } else {
            boolean isStart = instanceOperator.startInstance(configInfo.getModuleName(), configInfo.getInstallInfo(),
                    configInfo.getFollowerEndpoint());
            setOperationResult(isStart, result, InstanceDeployEventStage.INSTANCE_START.getMessage(),
                    InstanceDeployEventStage.INSTANCE_START.getError(), eventInfo.getEventStage(),
                    InstanceDeployEventStage.INSTANCE_START.isLast());
        }

        return result;
    }

    private HeartBeatEventResult handleStartEvent(HeartBeatEventInfo eventInfo) {
        String jsonConfigStr = JSON.toJSONString(eventInfo.getConfigInfo());
        InstanceStartEventConfigInfo configInfo = JSON.parseObject(jsonConfigStr,
                InstanceStartEventConfigInfo.class);
        HeartBeatEventResult result = new HeartBeatEventResult(eventInfo);

        boolean isStart = instanceOperator.startInstance(configInfo.getModuleName(), configInfo.getInstallInfo(), null);
        setOperationResult(isStart, result, "Instance start succeeded",
                "Instance start failed", eventInfo.getEventStage(), true);
        return result;
    }

    private HeartBeatEventResult handleStopEvent(HeartBeatEventInfo eventInfo) {
        String jsonConfigStr = JSON.toJSONString(eventInfo.getConfigInfo());
        InstanceStopEventConfigInfo configInfo = JSON.parseObject(jsonConfigStr,
                InstanceStopEventConfigInfo.class);
        HeartBeatEventResult result = new HeartBeatEventResult(eventInfo);

        boolean isStop = instanceOperator.stopInstance(configInfo.getModuleName(), configInfo.getInstallInfo());
        setOperationResult(isStop, result, "Instance stop succeeded",
                "Instance stop failed", eventInfo.getEventStage(), true);
        return result;
    }

    private HeartBeatEventResult handleRestartEvent(HeartBeatEventInfo eventInfo) {
        String jsonConfigStr = JSON.toJSONString(eventInfo.getConfigInfo());
        InstanceRestartEventConfigInfo configInfo = JSON.parseObject(jsonConfigStr,
                InstanceRestartEventConfigInfo.class);
        HeartBeatEventResult result = new HeartBeatEventResult(eventInfo);

        boolean isStop = instanceOperator.restartInstance(configInfo.getModuleName(), configInfo.getInstallInfo());
        setOperationResult(isStop, result, "Instance restart succeeded",
                "Instance restart failed", eventInfo.getEventStage(), true);
        return result;
    }

    private void setOperationResult(boolean isSuccess, HeartBeatEventResult result, String successInfo,
                                    String errorInfo, int currentStage, boolean isLastStage) {

        if (isLastStage) {
            result.setEventStage(currentStage);
            result.setCompleted(true);
            if (isSuccess) {
                result.setResultInfo(successInfo);
                result.setResultType(HeartBeatEventResultType.SUCCESS);
            } else {
                result.setResultType(HeartBeatEventResultType.FAIL);
                result.setResultInfo(errorInfo);
            }
        } else {
            if (isSuccess) {
                result.setResultInfo(successInfo);
                result.setResultType(HeartBeatEventResultType.PROCESSING);
                result.setEventStage(currentStage + 1);
                result.setCompleted(false);
            } else {
                result.setResultType(HeartBeatEventResultType.FAIL);
                result.setResultInfo(errorInfo);
                result.setEventStage(currentStage);
                result.setCompleted(true);
            }
        }
    }

}
