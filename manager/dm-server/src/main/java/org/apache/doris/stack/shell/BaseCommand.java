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

package org.apache.doris.stack.shell;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * base command
 **/
@Slf4j
public abstract class BaseCommand {

    protected String[] resultCommand;
    protected String stdoutResponse;
    protected String errorResponse;
    protected int exitCode;

    protected abstract void buildCommand();

    public String getStdoutResponse() {
        return this.stdoutResponse;
    }

    public String getErrorResponse() {
        return this.errorResponse;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    public boolean run() {
        return run(0);
    }

    public boolean run(long timeoutMs) {
        buildCommand();
        log.info("run command: {} ,timeout time: {}ms", StringUtils.join(resultCommand, " "), timeoutMs);
        ProcessBuilder pb = new ProcessBuilder(resultCommand);
        Process process = null;
        BufferedReader stdoutBufferedReader = null;
        BufferedReader errorBufferedReader = null;
        try {
            process = pb.start();
            stdoutBufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            stdoutResponse = stdoutBufferedReader.lines().parallel().collect(Collectors.joining(System.lineSeparator()));
            errorResponse = errorBufferedReader.lines().parallel().collect(Collectors.joining(System.lineSeparator()));

            if (timeoutMs <= 0) {
                exitCode = process.waitFor();
            } else {
                boolean isExit = process.waitFor(timeoutMs, TimeUnit.MICROSECONDS);
                if (!isExit) {
                    exitCode = 124; // the same as timeout command
                    log.error("command run timeout in {}ms", timeoutMs);
                    return false;
                }
                exitCode = process.exitValue();
            }

            if (exitCode == 0) {
                return true;
            } else {
                log.error("shell command error, exit with {}, response:{}", exitCode, errorResponse);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            log.error("command execute fail", e);
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
            try {
                if (stdoutBufferedReader != null) {
                    stdoutBufferedReader.close();
                }
                if (errorBufferedReader != null) {
                    errorBufferedReader.close();
                }
            } catch (IOException e) {
                log.error("close buffered reader fail");
            }
        }
    }
}
