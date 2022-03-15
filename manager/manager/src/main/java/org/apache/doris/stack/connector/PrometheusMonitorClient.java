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

package org.apache.doris.stack.connector;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class PrometheusMonitorClient {
    protected HttpClientPoolManager poolManager;

    @Autowired
    public PrometheusMonitorClient(HttpClientPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    public String doPost(String url, Map<String, String> paras) throws IOException {
        Preconditions.checkNotNull(paras);
        return poolManager.doPostUrlEncode(url, paras);
    }
}
