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

package org.apache.doris.manager.common.heartbeat;

public enum HeartBeatEventType {
    // At present, except for the registration of the last step of installation,
    // all agent type operations are handled on the server
    AGENT_START,
    AGENT_STOP,
    AGENT_INSTALL,
    // The operation of instance is in the short operation of agent
    INSTANCE_INSTALL,
    INSTANCE_START,
    INSTANCE_DEPLOY_CHECK,
    INSTANCE_STOP,
    INSTANCE_RESTART,
    INSTANCE_STATUS_CHANGE;
}
