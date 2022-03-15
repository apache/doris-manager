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

package org.apache.doris.stack.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * telnet util
 **/
@Slf4j
public class TelnetUtil {

    /**
     * Test the connectivity of the host port
     */
    public static boolean telnet(String host, int port) {
        Socket socket = new Socket();
        boolean isConnected = false;
        long str = System.currentTimeMillis();
        try {
            socket.connect(new InetSocketAddress(host, port), 1000);
            isConnected = socket.isConnected();
            System.out.println(isConnected);
        } catch (IOException e) {
            log.error("can not telnet {}:{}", host, port);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("can not telnet {}:{}", host, port);
            }
        }
        System.out.println(System.currentTimeMillis() - str);
        return isConnected;
    }
}
