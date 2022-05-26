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
import org.apache.doris.manager.common.heartbeat.config.AgentUnInstallEventConfigInfo;
import org.apache.doris.manager.common.heartbeat.stage.AgentInstallEventStage;
import org.apache.doris.manager.common.heartbeat.stage.AgentUnInstallEventStage;
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
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ResourceNodeAndAgentManager {
    private static final String AGENT_START_SCRIPT = Constants.KEY_DORIS_AGENT_START_SCRIPT;
    private static final String AGENT_STOP_SCRIPT = Constants.KEY_DORIS_AGENT_STOP_SCRIPT;
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

    public void deleteOperation(long resourceClusterId, String host) {
        log.info("delete node {} for resource cluster {}", host, resourceClusterId);
        nodeRepository.deleteByResourceClusterIdAndHost(resourceClusterId, host);
    }

    public void deleteOperation(long nodeId) {
        log.info("delete node {}", nodeId);
        nodeRepository.deleteById(nodeId);
    }

    // only responsible to stop agent
    public void deleteAgentOperation(ResourceNodeEntity node, AgentUnInstallEventConfigInfo configInfo)
            throws Exception {

        // check agent has been installed or not
        if (!isAgentInstalled(node)) {
            log.warn("node[{}]:{} does not install agent, no need to uninstall", node.getId(), node.getHost());
            return;
        }

        HeartBeatEventEntity uninstallEvent = new HeartBeatEventEntity(HeartBeatEventType.AGENT_STOP.name(),
                HeartBeatEventResultType.INIT.name(), JSON.toJSONString(configInfo), 0);

        uninstallEvent = heartBeatEventRepository.save(uninstallEvent);

        log.info("create event of deleting agent , event id {}", uninstallEvent.getId());
        node.setCurrentEventId(uninstallEvent.getId());
        nodeRepository.save(node);

        log.info("to stop node[{}] agent for resource cluster {}", node.getHost(),
                node.getResourceClusterId());

        HeartBeatEventEntity finalUninstallEvent = uninstallEvent;
        CompletableFuture<Void> uninstallFuture = CompletableFuture.runAsync(() -> {
            log.info("start to handle uninstall agent event on {} node {}", node.getId(), node.getHost());
            uninstallEventProcess(node, configInfo, finalUninstallEvent);
            log.info("async uninstall agent on {} node {} success", node.getId(), node.getHost());
        });
    }

    public void installAgentOperation(ResourceNodeEntity node, AgentInstallEventConfigInfo configInfo, long requestId) {
        log.info("install node {} agent for request {}", node.getId(), requestId);
        configInfo.setAgentNodeId(node.getId());
        configInfo.setInstallDir(node.getAgentInstallDir());
        configInfo.setHost(node.getHost());
        configInfo.setAgentPort(node.getAgentPort());

        long eventId = node.getCurrentEventId();
        log.info("event {}: to install and start {} node agent {}:{} in {}", eventId, node.getId(),
                node.getHost(), node.getAgentPort(), node.getAgentInstallDir());
        // Check whether the current node is already installing agent or agent installation has failed
        HeartBeatEventEntity agentInstallAgentEntity;
        if (eventId < 1L) {
            log.debug("first install agent for node {}", node.getId());
            // first time install agent
            // create HeartBeatEvent
            HeartBeatEventEntity eventEntity = new HeartBeatEventEntity(HeartBeatEventType.AGENT_INSTALL.name(),
                    HeartBeatEventResultType.INIT.name(), JSON.toJSONString(configInfo), requestId);

            agentInstallAgentEntity = heartBeatEventRepository.save(eventEntity);
            eventId = agentInstallAgentEntity.getId();
            log.info("first time to install agent, create heart beat event {}", eventId);
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

        // handle install agent event async
        CompletableFuture<Void> installFuture = CompletableFuture.runAsync(() -> {
            log.info("start to handle install agent event to {} node {}", node.getId(), node.getHost());
            installEventProcess(node, configInfo, agentInstallAgentEntity);
            log.info("async install agent to {} node {} success", node.getId(), node.getHost());
        });
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

    public boolean isAgentInstalled(ResourceNodeEntity node) {
        long eventId = node.getCurrentEventId();

        if (eventId < 1L) {
            log.warn("The node no have agent");
            return false;
        } else {
            HeartBeatEventEntity eventEntity = heartBeatEventRepository.findById(eventId).get();

            // handling other event, agent has been installed
            if (!eventEntity.getType().equals(HeartBeatEventType.AGENT_INSTALL.name())) {
                return true;
            }

            // AGENT_INSTALL event
            if (eventEntity.isCompleted() && eventEntity.getStatus().equals(HeartBeatEventResultType.SUCCESS.name())) {
                return true;
            }

            if (eventEntity.getStage() >= AgentInstallEventStage.AGENT_START.getStage()) {
                return true;
            }

            log.warn("Agent has not been installed successfully");
            return false;
        }
    }

    public boolean isAvailableAgentPort(ResourceNodeEntity node, AgentInstallEventConfigInfo configInfo)
            throws Exception {
        // agent port check, eg: Spring Boot Param server.port=8008
        log.info("check {} node port {}:{}", node.getId(), node.getHost(), node.getAgentPort());
        String sshkey = String.format("sshkey-%d-%d", node.getId(), node.getAgentPort());
        File sshKeyFile = SSH.buildSshKeyFile(sshkey);
        SSH.writeSshKeyFile(configInfo.getSshKey(), sshKeyFile);

        // only check listen port
        String checkPortCmd = String.format("netstat -tunlp | grep  -w %d", node.getAgentPort());
        SSH checkPortSSH = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), node.getHost(), checkPortCmd);
        if (checkPortSSH.run(3000)) {
            String netInfo = checkPortSSH.getStdoutResponse();
            log.info("agent node {} port check return output\n: {}", node.getId(), netInfo);

            if (netInfo != null && !netInfo.trim().isEmpty()) {
                log.error("agent node {} port {} already in use:\n {}", node.getId(), node.getAgentPort(), netInfo);
                return false;
            }
        } else if (checkPortSSH.getExitCode() != 1) { //exit 1 when grep failed, other exit code is exception
            log.warn("run check port cmd failed");
            throw new Exception("check agent port scrpit execution exception:" +  checkPortSSH.getErrorResponse());
        }
        log.info("{} node {}:{} is available", node.getId(), node.getHost(), node.getAgentPort());
        return true;
    }

    public void checkSshConnect(ResourceNodeEntity node, AgentInstallEventConfigInfo configInfo)
            throws Exception {

        log.info("check {} node {} ssh connect", node.getId(), node.getHost());
        String sshkey = String.format("sshkey-%d", node.getId());
        File sshKeyFile = SSH.buildSshKeyFile(sshkey);
        SSH.writeSshKeyFile(configInfo.getSshKey(), sshKeyFile);

        SSH checkSSH = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), node.getHost(), "echo ok");

        // if ssh-key is invalid, ssh commond will enter interactive mode that will block the process
        // user not exists(255), user error(need input password), ssh port error(1), host error(1)
        if (checkSSH.run(3000)) {
            log.info("ssh connect {} node {} success", node.getId(), node.getHost());
        } else {
            throw new Exception(String.format("ssh connect node %s failed: error code %d, error msg: %s",
                    node.getHost(), checkSSH.getExitCode(),
                    checkSSH.getErrorResponse()));
        }
    }

    public void checkStopScriptExist(ResourceNodeEntity node, AgentInstallEventConfigInfo configInfo)
            throws Exception {
        log.info("check node {} agent stop script", node.getId());
        checkPathExist(node, configInfo, Paths.get(node.getAgentInstallDir(), "agent", AGENT_STOP_SCRIPT).toString());
    }

    public void checkPathExist(ResourceNodeEntity node, AgentInstallEventConfigInfo configInfo,
                               String path) throws Exception {

        String sshkey = String.format("sshkey-%d", node.getId());
        File sshKeyFile = SSH.buildSshKeyFile(sshkey);
        SSH.writeSshKeyFile(configInfo.getSshKey(), sshKeyFile);

        String checkPathExistCmd = "if test -e " + path + "; then echo ok; else exit 1; fi";
        SSH checkPathSSH = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), node.getHost(), checkPathExistCmd);
        if (!checkPathSSH.run()) {
            if (checkPathSSH.getExitCode() == 1) {
                log.error("path {} is not exist on node {}", path, node.getId());
                throw new Exception(String.format("node %s does not have path: %s", node.getHost(), path));
            } else {
                throw new Exception("check path exception:" +  checkPathSSH.getErrorResponse());
            }
        }
    }

    private void installEventProcess(ResourceNodeEntity node, AgentInstallEventConfigInfo configInfo,
                                     HeartBeatEventEntity agentInstallAgentEntity) {
        if (!agentInstallAgentEntity.getType().equals(HeartBeatEventType.AGENT_INSTALL.name())) {
            log.warn("agent has been installed on {} node {}", node.getId(), node.getHost());
            return;
        }

        if (!agentInstallAgentEntity.getStatus().equals(HeartBeatEventResultType.INIT.name())) {
            log.warn("agent is being installed on {} node {}", node.getId(), node.getHost());
            return;
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
        final String checkSystem = "cat /etc/os-release";
        ssh.setCommand(checkSystem);
        if (!ssh.run()) {
            log.error("View system failures", ssh.getErrorResponse());
            return;
        }
        if (ssh.getStdoutResponse().toLowerCase(Locale.ROOT).contains("ubuntu")) {
            final String mkdirBashProfile = "if test -f ~/.bash_profile;then echo ok;else touch ~/.bash_profile;fi";
            ssh.setCommand(mkdirBashProfile);
            if (!ssh.run()) {
                log.error("Create file fail", ssh.getErrorResponse());
                return;
            }
        }
        log.info("Create .bash_profile successfully");
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

        log.info("to start agent with port {}", configInfo.getAgentPort());
        String command = "cd %s && sh %s  --server %s --agent %d --port %d";
        String cmd = String.format(command, agentInstallHome, AGENT_START_SCRIPT,
                getServerAddr(), configInfo.getAgentNodeId(), configInfo.getAgentPort());
        SSH startSsh = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), configInfo.getHost(), cmd);
        if (!startSsh.run()) {
            log.error("agent start failed:{}", startSsh.getErrorResponse());
            updateFailResult(AgentInstallEventStage.AGENT_START.getError(),
                    AgentInstallEventStage.AGENT_START.getStage(), agentInstallAgentEntity);
            return;
        }
        log.info("agent start success");
        updateProcessingResult(AgentInstallEventStage.AGENT_START.getMessage(),
                AgentInstallEventStage.AGENT_START.getStage(), agentInstallAgentEntity);
    }

    private void uninstallEventProcess(ResourceNodeEntity node, AgentUnInstallEventConfigInfo configInfo,
                                     HeartBeatEventEntity agentUninstallAgentEntity) {
        if (!agentUninstallAgentEntity.getType().equals(HeartBeatEventType.AGENT_STOP.name())) {
            log.warn("agent no need to stop on {} node {}", node.getId(), node.getHost());
            return;
        }

        if (!agentUninstallAgentEntity.getStatus().equals(HeartBeatEventResultType.INIT.name())) {
            log.warn("agent is being stopped on {} node {}", node.getId(), node.getHost());
            return;
        }
        // warning: multithreaded write operation
        String sshkey = String.format("sshkey-%d", node.getId());
        File sshKeyFile = SSH.buildSshKeyFile(sshkey);
        SSH.writeSshKeyFile(configInfo.getSshKey(), sshKeyFile);

        String agentInstallHome = configInfo.getInstallDir() + File.separator + "agent";

        log.info("to uninstall {} agent", configInfo.getHost());
//        String command = "cd %s && sh %s && rm -rf %s";
//        String cmd = String.format(command, agentInstallHome, AGENT_STOP_SCRIPT, agentInstallHome);
        String command = "cd %s && sh %s";
        String cmd = String.format(command, agentInstallHome, AGENT_STOP_SCRIPT);
        SSH stopSsh = new SSH(configInfo.getSshUser(), configInfo.getSshPort(),
                sshKeyFile.getAbsolutePath(), configInfo.getHost(), cmd);
        if (!stopSsh.run()) {
            log.error("agent stop failed:{}", stopSsh.getErrorResponse());
            agentUninstallAgentEntity.setCompleted(true);
            agentUninstallAgentEntity.setStatus(HeartBeatEventResultType.FAIL.name());
            heartBeatEventRepository.save(agentUninstallAgentEntity);

            updateFailResult(AgentUnInstallEventStage.AGENT_STOP.getError() + stopSsh.getErrorResponse(),
                    AgentUnInstallEventStage.AGENT_STOP.getStage(), agentUninstallAgentEntity);
        }

        log.info("node {} success to uninstall agent", node.getHost());

        agentUninstallAgentEntity.setCompleted(true);
        agentUninstallAgentEntity.setStatus(HeartBeatEventResultType.SUCCESS.name());
        heartBeatEventRepository.save(agentUninstallAgentEntity);
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
        String host = System.getenv(EnvironmentDefine.STUDIO_IP_ENV);
        String port = System.getenv(EnvironmentDefine.STUDIO_PORT_ENV);

        if (host == null || host.isEmpty()) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new ServerException("get server ip fail");
            }
        }
        return host + ":" + port;
    }

}
