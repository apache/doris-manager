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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.agent.util.ShellUtil;
import org.apache.doris.manager.common.util.ConfigDefault;
import org.apache.doris.manager.common.util.ServerAndAgentConstant;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Component
public class DorisInstanceOperator {

    // Download installation package
    public boolean downloadInstancePackage(String moudleName, String installInfo, String packageInfo) {
        File packageFile = Paths.get(installInfo, moudleName).toFile();
        if (packageFile.exists()) {
            log.info("The {} package {} is already exist", moudleName, packageFile.getAbsolutePath());
            return true;
        } else {
            // download package
            // Set the parameters, installation path and download link of the download script
            StringBuffer downloadScript = new StringBuffer();
            downloadScript.append(ServerAndAgentConstant.PACKAGE_DOWNLOAD_SCRIPT);
            downloadScript.append(" ");
            downloadScript.append(packageInfo);
            downloadScript.append(" ");
            downloadScript.append(installInfo);
            log.info("download Script is {}", downloadScript);
            try {
                executePkgShellScript(downloadScript.toString(), installInfo,
                        ServerAndAgentConstant.AGENT_NAME, Maps.newHashMap());
                return true;
            } catch (Exception e) {
                log.error("download doris package error." , e);
                return false;
            }
        }
    }

    // Create a data path and modify the configuration file
    public boolean configInstance(String moudleName, String installInfo, Map<String, String> parms) {
        log.info("begin to config {} instance", moudleName);
        try {
            File confFile;
            if (moudleName.equals(ServerAndAgentConstant.FE_NAME)) {
                createDataPath(parms.get(ConfigDefault.FE_META_CONFIG_NAME));
                confFile = Paths.get(installInfo, moudleName, "conf", ServerAndAgentConstant.FE_CONF_FILE).toFile();

            } else if (moudleName.equals(ServerAndAgentConstant.BE_NAME)) {
                String dataPathConfig = parms.get(ConfigDefault.BE_DATA_CONFIG_NAME);
                String[] dataPaths = dataPathConfig.split(";");
                for (String dataPath : dataPaths) {
                    createDataPath(dataPath);
                }
                confFile = Paths.get(installInfo, moudleName, "conf", ServerAndAgentConstant.BE_CONF_FILE).toFile();
            } else {
                confFile = Paths.get(installInfo, moudleName, "conf", ServerAndAgentConstant.BROKER_CONF_FILE).toFile();
            }

            // Create a new profile
            confFile.createNewFile();

            StringBuffer configFileContentBuffer = new StringBuffer();
            for (String key : parms.keySet()) {
                String configItem = key + "=" + parms.get(key) + "\n\n";
                configFileContentBuffer.append(configItem);
            }

            log.info("create new conf file {}", configFileContentBuffer.toString());
            Files.asCharSink(confFile, Charset.forName("UTF-8")).write(configFileContentBuffer.toString());

            log.info("config {} instance success");
            return true;
        } catch (Exception e) {
            log.error("config {} instance error", e);
            return false;
        }
    }

    public boolean startInstance(String moudleName, String installInfo, String followerEndpoint) {
        log.info("begin to start {} instance", moudleName);
        try {
            int mainProcPid = processIsRunning(moudleName, installInfo);
            if (mainProcPid == -1) {
                log.info("{} instance not running, start it", moudleName);
                String startScript = ServerAndAgentConstant.FE_START_SCRIPT;
                if (followerEndpoint != null && !followerEndpoint.isEmpty()) {
                    startScript += " --helper " + followerEndpoint;
                }

                if (moudleName.equals(ServerAndAgentConstant.BE_NAME)) {
                    startScript = ServerAndAgentConstant.BE_START_SCRIPT;
                } else if (moudleName.equals(ServerAndAgentConstant.BROKER_NAME)) {
                    startScript = ServerAndAgentConstant.BROKER_START_SCRIPT;
                }
                startScript += " --daemon";

                executePkgShellScriptWithBash(startScript, installInfo, moudleName, Maps.newHashMap());
            } else {
                log.info("{} instance is running", moudleName);
            }
            log.info("start {} instance success", moudleName);
            return true;
        } catch (Exception e) {
            log.error("Start " + moudleName + " instance error {}.", e);
            return false;
        }
    }

    public boolean stopInstance(String moudleName, String installInfo) {
        log.info("begin to stop {} instance", moudleName);
        try {
            int mainProcPid = processIsRunning(moudleName, installInfo);
            if (mainProcPid > -1) {
                log.info("{} instance is running, stop it", moudleName);
                String stopScript = ServerAndAgentConstant.FE_STOP_SCRIPT;
                if (moudleName.equals(ServerAndAgentConstant.BE_NAME)) {
                    stopScript = ServerAndAgentConstant.BE_STOP_SCRIPT;
                } else if (moudleName.equals(ServerAndAgentConstant.BROKER_NAME)) {
                    stopScript = ServerAndAgentConstant.BROKER_STOP_SCRIPT;
                }
                executePkgShellScript(stopScript, installInfo, moudleName, Maps.newHashMap());
            } else {
                log.info("{} instance has been stopped", moudleName);
            }
            log.info("stop {} instance success", moudleName);
            return true;
        } catch (Exception e) {
            log.error("stop " + moudleName + " instance error {}.", e);
            return false;
        }
    }

    public boolean restartInstance(String moudleName, String installInfo) {
        boolean isStopSuccess = stopInstance(moudleName, installInfo);
        if (!isStopSuccess) {
            return false;
        }
        return startInstance(moudleName, installInfo, null);
    }

    // Check whether the instance has been installed and started
    public boolean checkInstanceDeploy(String moudleName, String installInfo) {
        try {
            int bePid = processIsRunning(moudleName, installInfo);
            if (bePid < 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("Check " + moudleName + " instance running error {}.", e);
            return false;
        }
    }

    /*
     * 1. use `ps ux | grep process |grep -v grep |awk '{print $2}'` to get the real pid of the process
     * 2. if process is not running, delete the pid file if exist.
     * 3. if process is running, get and check pid in pid file,
     *      if pid file does not exist or pid is incorrect, recreate the pid file
     *
     * return -1 if process is not running, or pid otherwise
     */
    private int processIsRunning(String moduleName, String runningDir) throws Exception {

        String processName = ServerAndAgentConstant.FE_PID_NAME;
        String pidFileName = ServerAndAgentConstant.FE_PID_FILE;
        if (moduleName.equals(ServerAndAgentConstant.BE_NAME)) {
            processName = ServerAndAgentConstant.BE_PID_NAME;
            pidFileName = ServerAndAgentConstant.BE_PID_FILE;
        } else if (moduleName.equals(ServerAndAgentConstant.BROKER_NAME)) {
            processName = ServerAndAgentConstant.BROKER_PID_NAME;
            pidFileName = ServerAndAgentConstant.BROKER_PID_FILE;
        }

        int pid = getPid(processName);
        if (pid == -1) {
            if (pidFileName != null) {
                // process is not running, if pid file exist, delete it
                File pidFile = Paths.get(runningDir, moduleName, "bin", pidFileName).toFile();
                if (pidFile.exists()) {
                    pidFile.delete();
                }
                log.info("process {} is not running, delete pid file if exist: {}", processName, pidFile.toString());
            }
            return -1;
        }

        // process is running, check pid file
        // no need to check if pid file does not exist
        if (pidFileName == null) {
            return pid;
        }

        File pidFile = Paths.get(runningDir, moduleName, "bin", pidFileName).toFile();
        if (!pidFile.exists()) {
            // pid file does not exist, create one and write pid file in it
            log.info("process {} is running at {} but pid file is missing, create it", processName, pid);
            try {
                Files.asCharSink(pidFile, Charset.forName("UTF-8")).write(String.valueOf(pid));
            } catch (IOException e) {
                throw new Exception("failed to rewrite pid file: " + pidFileName, e);
            }
        } else {
            // check if pid is same as pid in pid file
            String content = null;
            try {
                content = Files.asCharSource(pidFile, Charset.forName("UTF-8")).readFirstLine();
            } catch (IOException e) {
                throw new Exception("failed to read pid file: " + pidFileName, e);
            }

            try {
                int existPid = Integer.valueOf(content);
                if (existPid != pid) {
                    throw new Exception("pid saved: " + existPid + ", but expected: " + pid);
                }
            } catch (Exception e) {
                log.warn("invalid pid: " + e.getMessage() + ", delete it and create new one");
                pidFile.delete();
                try {
                    pidFile.createNewFile();
                    Files.asCharSink(pidFile, Charset.forName("UTF-8")).write(String.valueOf(pid));
                } catch (IOException e1) {
                    throw new Exception("failed to rewrite pid file: " + pidFileName, e);
                }
            }
        }

        return pid;
    }

    private int getPid(String processName) throws Exception {
        // even if process with this name does not exist, this cmd will still return with code 0, but empty stdout.
        final String cmd = "ps xu | grep " + processName + " | grep -v grep |awk '{print $2}'";
        String result = "";
        Process pro = null;
        BufferedReader bufferedReader = null;
        try {
            pro = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            bufferedReader = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
                // should be only one line
                break;
            }

            int exitValue = pro.waitFor();
            log.info("doris {} check exit with {}, stdout: {}", processName, exitValue, result);
            if (exitValue == 1) {
                throw new Exception("failed to get pid of process " + processName + ", return 1");
            }

            if (Strings.isNullOrEmpty(result)) {
                // process does not exist
                return -1;
            }
            int pid = Integer.valueOf(result);
            return pid;
        } catch (IOException | InterruptedException e) {
            throw new Exception("failed to get pid of " + processName, e);
        } catch (NumberFormatException e) {
            throw new Exception("invalid pid format: " + result, e);
        }
    }

    private void executePkgShellScript(String scriptName, String runningDir,
                                       String moduleName, Map<String, String> environment) throws Exception {
        String scripts = Paths.get(runningDir, moduleName, "bin", scriptName).toFile().getAbsolutePath();
        int index = scripts.indexOf(":") + 1;
        scripts = scripts.substring(0, index) + "//" + scripts.substring(index + 1);
        final String shellCmd = "sh " + scripts;
        log.info("begin to execute: `" + shellCmd + "`");
        executeShell(shellCmd, environment);
    }

    private void executePkgShellScriptWithBash(String scriptName, String runningDir,
                                       String moduleName, Map<String, String> environment) throws Exception {
        String scripts = Paths.get(runningDir, moduleName, "bin", scriptName).toFile().getAbsolutePath();
        final String shellCmd = "sh " + scripts;
        log.info("begin to execute: `" + shellCmd + "`");
        ShellUtil.cmdExecute(shellCmd);
    }

    private int executeShell(String shellCmd, Map<String, String> environment) throws Exception {
        return ShellUtil.executeShellWithoutOutput(shellCmd, new int[]{0}, 1, environment);
    }

    private void createDataPath(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new Exception("failed to create dir: " + path);
            }
        }
    }
}
