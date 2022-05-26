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

package org.apache.doris.manager.agent.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Slf4j
public class ShellUtil {
    private ShellUtil() {
        throw new UnsupportedOperationException();
    }

    public static int cmdExecute(String cmd) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"/bin/bash", "-c", ""};
        commands[2] = cmd;
        Process proc = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        log.info("scriptCmd:{}", cmd);

        String s = null;
        while ((s = stdInput.readLine()) != null) {
            log.info(s);
        }

        while ((s = stdError.readLine()) != null) {
            log.error(s);
        }

        int exitVal = proc.waitFor();
        return exitVal;
    }

    public static int executeShellWithoutOutput(String shellCmd, int[] succExitValue, long shellTimeout,
                                         Map<String, String> environment) throws Exception {
        log.info("begin to execute cmd {}", shellCmd);

        CommandLine cmdLine = CommandLine.parse(shellCmd);
        DefaultExecutor executor = new DefaultExecutor();
        // If the running time exceeds 1 minute, it will be judged as execution failure
        ExecuteWatchdog watchdog = new ExecuteWatchdog(shellTimeout * 60 * 1000);
        executor.setWatchdog(watchdog);
        executor.setExitValues(succExitValue);
        int exitValue = 0;
        try {
            exitValue = executor.execute(cmdLine, environment);
            return exitValue;
        } catch (IOException e) {
            log.error("execute shell Exception ", e);
            throw e;
        } finally {
            log.info("shell_cmd {} exit_value {}", shellCmd, exitValue);
        }
    }

    public int executeShell(String shellCmd, int succExitValue, long shellTimeout) throws IOException {
        return executeShell(shellCmd, new int[] {succExitValue}, shellTimeout);
    }

    public int executeShell(String shellCmd, int[] succExitValue, long shellTimeout) throws IOException {
        log.info("begin to execute cmd {}", shellCmd);

        CommandLine cmdLine = CommandLine.parse(shellCmd);
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        executor.setStreamHandler(streamHandler);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(shellTimeout * 60 * 1000);
        executor.setWatchdog(watchdog);
        executor.setExitValues(succExitValue);
        int exitValue = 0;
        try {
            exitValue = executor.execute(cmdLine);
            return exitValue;
        } catch (IOException e) {
            log.info("ioexception", e);
            throw e;
        } finally {
            log.info("shell_cmd {} exit_value {} stdout {} stderr {}", shellCmd, exitValue,
                    outputStream.toString("UTF-8"), errorStream.toString("UTF-8"));
        }
    }
}
