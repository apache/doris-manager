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

package org.apache.doris.stack.model.request.control;

import lombok.Data;
import org.apache.doris.stack.model.request.space.NewUserSpaceCreateReq;

import java.util.List;

@Data
public class DorisClusterCreationReq extends ModelControlReq {
    // Step 1: create space information
    private NewUserSpaceCreateReq spaceInfo;

    // Step 2: Set up physical cluster
    private PMResourceClusterAccessInfo authInfo;

    private List<String> hosts;

    // Step 3: Installation options
    private String packageInfo;

    private String installInfo;

    private int agentPort;

    // Step 4: Install agent

    // Step 5:Planning resource node
    private List<DorisClusterModuleResourceConfig> nodeConfig;

    // Step 6:configuration module deploy parameter and deploy doris cluster
    private List<DorisClusterModuleDeployConfig> deployConfigs;

    // Step 7: check doris cluster deploy
    // Step 8: Set cluster custom password and access doris cluster
    private String clusterPassword;

}
