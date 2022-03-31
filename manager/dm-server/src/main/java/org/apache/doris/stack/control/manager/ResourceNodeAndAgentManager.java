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

package org.apache.doris.stack.control.manager;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventResultType;
import org.apache.doris.manager.common.heartbeat.HeartBeatEventType;
import org.apache.doris.manager.common.heartbeat.config.AgentInstallEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.stage.AgentInstallEventStage;
import org.apache.doris.stack.constant.EnvironmentDefine;
import org.apache.doris.stack.dao.HeartBeatEventRepository;
import org.apache.doris.stack.dao.ResourceNodeRepository;
import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.apache.doris.stack.exceptions.ServerException;
import org.apache.doris.stack.shell.SCP;
import org.apache.doris.stack.shell.SSH;
import org.apache.doris.stack.util.Constants;
import org.apache.doris.stack.util.TelnetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class ResourceNodeAndAgentManager {
    private static final String AGENT_START_SCRIPT = Constants.KEY_DORIS_AGENT_START_SCRIPT;
    private static final String AGENT_CONFIG_PATH = Constants.KEY_DORIS_AGENT_CONFIG_PATH;

    @Autowired
    private ResourceNodeRepository nodeRepository;

    @Autowired
    private HeartBeatEventRepository heartBeatEventRepository;

    public long initOperation(long resourceClusterId, String host) {
        log.info("create a new node {} for resource cluster {}", host, resourceClusterId);
        ResourceNodeEntity nodeEntity = new ResourceNodeEntity(resourceClusterId, host);
        ResourceNodeEntity newNodeEntity = nodeRepository.save(nodeEntity);
        return newNodeEntity.getId();
    }

    // TODO:Uninstall agent
    public void deleteOperation(long resourceClusterId, String host) {
        log.info("delete node {} for resource cluster {}", host, resourceClusterId);
        nodeRepository.deleteByResourceClusterIdAndHost(resourceClusterId, host);
    }

    public void installAgentOperation(ResourceNodeEntity node, AgentInstallEventConfigInfo configInfo, long requestId) {
        log.info("install node {} agent for request {}", node.getId(), requestId);
        configInfo.setAgentNodeId(node.getId());
        configInfo.setInstallDir(node.getAgentInstallDir());
        configInfo.setHost(node.getHost());

        long eventId = node.getCurrentEventId();

        // Check whether the current node is already installing agent or agent installation has failed
        HeartBeatEventEntity agentInstallAgentEntity;
        if (eventId < 1L) {
            log.debug("first install agent for node {}", node.getId());
            // fisrt time install agent
            // create HeartBeatEvent
            HeartBeatEventEntity eventEntity = new HeartBeatEventEntity(HeartBeatEventType.AGENT_INSTALL.name(),
                    HeartBeatEventResultType.INIT.name(), JSON.toJSONString(configInfo), requestId);

            agentInstallAgentEntity = heartBeatEventRepository.save(eventEntity);
            eventId = agentInstallAgentEntity.getId();

            node.setCurrentEventId(eventId);
            nodeRepository.save(node);
        } else {
            log.debug("install agent for node {} heart beat event {} exist", node.getId(), eventId);
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();
            // If the agent has been successfully installed and a new agent request operation has been performed,
            // the installation cannot be performed again
            // TODO:exception
            if (!eventEntity.getType().equals(HeartBeatEventType.AGENT_INSTALL.name())) {
                return;
            }

            // If the agent has been successfully installed, it cannot be installed again
            if (eventEntity.isCompleted() && eventEntity.getStatus().equals(HeartBeatEventResultType.SUCCESS.name())) {
                return;
            }

            eventEntity.setStatus(HeartBeatEventResultType.INIT.name());
            eventEntity.setConfigInfo(JSON.toJSONString(configInfo));

            agentInstallAgentEntity = heartBeatEventRepository.save(eventEntity);
        }

        // AGENT_INSTALL heartbeat handle
        // TODO: Before each Stage operation, it is necessary to judge whether the event has been cancelled
        // ACCESS_AUTH stage
        String sshkey = "sshkey-" + agentInstallAgentEntity.getId();
        File sshKeyFile = SSH.buildSshKeyFile(sshkey);
        SSH.writeSshKeyFile(configInfo.getSshKey(), sshKeyFile);
        //check telnet
        if (!TelnetUtil.telnet(configInfo.getHost(), configInfo.getSshPort())) {
            log.error("can not telnet host {} port {}", configInfo.getHost(), configInfo.getSshPort());
            // TODO:The result content will be defined later
            updateFailResult(AgentInstallEventStage.ACCESS_AUTH.getError(), AgentInstallEventStage.ACCESS_AUTH.getStage(),
                    agentInstallAgentEntity);
            return;
        }
        log.info("telnet host {} port {} success", configInfo.getHost(), configInfo.getSshPort());
        //check ssh
        SSH ssh = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), configInfo.getHost(), "echo ok");
        if (!ssh.run()) {
            log.error("ssh is not available: {}", ssh.getErrorResponse());
            updateFailResult(AgentInstallEventStage.ACCESS_AUTH.getError(), AgentInstallEventStage.ACCESS_AUTH.getStage(),
                    agentInstallAgentEntity);
            return;
        }

        log.info("ssh is available");
        updateProcessingResult(AgentInstallEventStage.ACCESS_AUTH.getMessage(), AgentInstallEventStage.ACCESS_AUTH.getStage(),
                agentInstallAgentEntity);

        // check installDir exist
        // INSTALL_DIR_CHECK stage
        String checkFileExistCmd = "if test -e " + configInfo.getInstallDir() + "; then echo ok; else mkdir -p " + configInfo.getInstallDir() + " ;fi";
        ssh.setCommand(checkFileExistCmd);
        if (!ssh.run()) {
            log.error("installation path is not available:{}", ssh.getErrorResponse());
            updateFailResult(AgentInstallEventStage.INSTALL_DIR_CHECK.getError(),
                    AgentInstallEventStage.INSTALL_DIR_CHECK.getStage(), agentInstallAgentEntity);
            return;
        }
        log.info("check installDir exist");
        updateProcessingResult(AgentInstallEventStage.INSTALL_DIR_CHECK.getMessage(),
                AgentInstallEventStage.INSTALL_DIR_CHECK.getStage(), agentInstallAgentEntity);

        //check jdk
        // JDK_CHECK stage
        final String checkJavaHome = "source /etc/profile && source ~/.bash_profile && java -version && echo $JAVA_HOME";
        ssh.setCommand(checkJavaHome);
        if (!ssh.run()) {
            log.error("jdk is not available: {}", ssh.getErrorResponse());
            updateFailResult(AgentInstallEventStage.JDK_CHECK.getError(),
                    AgentInstallEventStage.JDK_CHECK.getStage(), agentInstallAgentEntity);
            return;
        }
        log.info("check jdk success");
        updateProcessingResult(AgentInstallEventStage.JDK_CHECK.getMessage(),
                AgentInstallEventStage.JDK_CHECK.getStage(), agentInstallAgentEntity);

        // agent install
        // AGENT_DEPLOY stage
        // TODO: How to get the agent installation package
        ApplicationHome applicationHome = new ApplicationHome();
        String dorisManagerHome = applicationHome.getSource().getParentFile().getParentFile().getParentFile().toString();
        log.info("doris manager home : {}", dorisManagerHome);
        String agentPackageHome = dorisManagerHome + File.separator + "agent";
        Preconditions.checkNotNull(configInfo.getHost(), "host is empty");
        SCP scp = new SCP(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), configInfo.getHost(), agentPackageHome, configInfo.getInstallDir());
        if (!scp.run()) {
            log.error("scp agent package failed:{} to {}", agentPackageHome, configInfo.getInstallDir());
            updateFailResult(AgentInstallEventStage.AGENT_DEPLOY.getError(),
                    AgentInstallEventStage.AGENT_DEPLOY.getStage(), agentInstallAgentEntity);
            return;
        }
        log.info("agent install success");
        updateProcessingResult(AgentInstallEventStage.AGENT_DEPLOY.getMessage(),
                AgentInstallEventStage.AGENT_DEPLOY.getStage(), agentInstallAgentEntity);

        // agent start
        // AGENT_START stage
        String agentInstallHome = configInfo.getInstallDir() + File.separator + "agent";

        // 1 port check, eg: server.port=8008
        // grep = application.properties | grep  -w server.port  | awk -F '=' '{print $2}'
        String confFile = agentInstallHome + File.separator + AGENT_CONFIG_PATH;
        String portGetFormat = "grep = %s | grep  -w server.port  | awk -F '=' '{print $2}'";
        String portGetCmd = String.format(portGetFormat, confFile);

        SSH portGetSSH = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), configInfo.getHost(), portGetCmd);

        int agentPort = -1;
        if (portGetSSH.run()) {
            String portStr = portGetSSH.getStdoutResponse();
            log.info("agent {} port get return output: {}", configInfo.getAgentNodeId(), portStr);

            if (portStr == null || portStr.isEmpty()) {
                log.warn("agent {} server.port is not set", configInfo.getAgentNodeId());
            } else {
                try {
                    agentPort = Integer.parseInt(portStr.trim());
                } catch (NumberFormatException e) {
                    log.warn("agent port format is not Integer");
                }
            }

        } else {
            log.warn("run agent port get cmd failed:{}, skip the check and use default port",
                    portGetSSH.getErrorResponse());
        }

        if (agentPort > 0) {
            log.info("agent start port is {}", agentPort);
            // only check listen port
            String checkPortCmd = String.format("netstat -tunlp | grep  -w %s", agentPort);
            SSH checkPortSSH = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                    sshKeyFile.getAbsolutePath(), configInfo.getHost(), checkPortCmd);
            if (checkPortSSH.run()) {
                String netInfo = checkPortSSH.getStdoutResponse();
                log.info("agent {} port check return output: {}", configInfo.getAgentNodeId(), netInfo);

                if (netInfo != null && !netInfo.trim().isEmpty()) {
                    log.error("port {} already in use, {}", agentPort, netInfo);
                    updateFailResult("port already in use",
                            AgentInstallEventStage.AGENT_START.getStage(), agentInstallAgentEntity);
                    return;
                }
            } else {
                log.warn("run check port cmd failed");
            }
        }

        // 2 run start shell
        String command = "cd %s && sh %s  --server %s --agent %s";
        String cmd = String.format(command, agentInstallHome, AGENT_START_SCRIPT, getServerAddr(), configInfo.getAgentNodeId());
        SSH startSsh = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), configInfo.getHost(), cmd);
        if (!startSsh.run()) {
            log.error("agent start failed:{}", ssh.getErrorResponse());
            updateFailResult(AgentInstallEventStage.AGENT_START.getError(),
                    AgentInstallEventStage.AGENT_START.getStage(), agentInstallAgentEntity);
            return;
        }
        log.info("agent start success");
        updateProcessingResult(AgentInstallEventStage.AGENT_START.getMessage(),
                AgentInstallEventStage.AGENT_START.getStage(), agentInstallAgentEntity);
    }

    public boolean checkAgentOperation(ResourceNodeEntity node) {
        long eventId = node.getCurrentEventId();

        if (eventId < 1L) {
            log.warn("The node no have agent");
            return false;
        } else {
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();
            // If the agent has been successfully installed and a new agent request operation has been performed,
            // the installation cannot be performed again
            if (!eventEntity.getType().equals(HeartBeatEventType.AGENT_INSTALL.name())) {
                return true;
            }

            if (eventEntity.isCompleted() && eventEntity.getStatus().equals(HeartBeatEventResultType.SUCCESS.name())) {
                return true;
            }

            log.warn("Agent has not been installed successfully");
            return false;
        }
    }

    private void updateProcessingResult(String result, int stage, HeartBeatEventEntity eventEntity) {
        eventEntity.setStatus(HeartBeatEventResultType.PROCESSING.name());
        eventEntity.setOperateResult(result);
        eventEntity.setCompleted(false);
        eventEntity.setStage(stage + 1); // Modify to next stage
        heartBeatEventRepository.save(eventEntity);
    }

    private void updateFailResult(String result, int stage, HeartBeatEventEntity eventEntity) {
        eventEntity.setStatus(HeartBeatEventResultType.FAIL.name());
        eventEntity.setOperateResult(result);
        eventEntity.setCompleted(true);
        eventEntity.setStage(stage);
        heartBeatEventRepository.save(eventEntity);
    }

    private boolean checkEventBeCancelled(long eventId) {
        // TODO: There is currently no cancel function
        return false;
//        HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();
//        return eventEntity.isCompleted() && eventEntity.getStatus().equals(HeartBeatEventResultType.CANCEL.name());
    }

    /**
     * get server address
     */
    private String getServerAddr() {
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new ServerException("get server ip fail");
        }
        String port = System.getenv(EnvironmentDefine.STUDIO_PORT_ENV);
        return host + ":" + port;
    }

}
