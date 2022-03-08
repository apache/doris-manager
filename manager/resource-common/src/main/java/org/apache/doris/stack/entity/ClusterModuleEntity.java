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

package org.apache.doris.stack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 *
 */
@Entity
@Table(name = "cluster_module")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterModuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "cluster_id")
    private long clusterId;

    @Column(name = "module_template_id")
    private long moduleTemplateId;

    @Column(name = "module_name")
    private String moduleName;

    // timestamp
    @Column(name = "create_timestamp")
    private Timestamp createTimestamp;

    @Column(name = "lastupdate_timestamp")
    private Timestamp lastUpdateTimestamp;

    // desired state(ModelControlState)
    @Column(name = "desired_state")
    private int desiredState;

    // current state (ModelControlState)
    @Column(name = "current_state")
    private int currentState;

    /**
     * json format install information for doris module,like install dir
     * TODO：It will only be used when subsequent physical clusters
     *  support the deployment of multiple Doris application instances
     */
    @Column(name = "install_info", columnDefinition = "TEXT")
    private String installInfo;

    /**
     * json format service config for doris module
     */
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    // json format resource config for doris module
    @Column(name = "resource_config", columnDefinition = "TEXT")
    private String resourceConfig = "";

    public ClusterModuleEntity(long clusterId, String moduleName) {
        this.clusterId = clusterId;
        this.moduleName = moduleName;
        this.createTimestamp = new Timestamp(System.currentTimeMillis());
    }
}
