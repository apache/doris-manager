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
@Table(name = "cluster_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterInstanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", length = 100)
    private String name = "";

    @Column(name = "cluster_id")
    private long clusterId;

    @Column(name = "module_id")
    private long moduleId;

    @Column(name = "node_id")
    private long nodeId;

    @Column(name = "address", length = 50)
    private String address;

    // TODO:target state (ModelControlState)
    @Column(name = "desired_state")
    private int desiredState;

    // TODO:current state (ModelControlState)
    @Column(name = "current_state")
    private int currentState;

    @Column(name = "current_event_id")
    private long currentEventId;

    /**
     * json format install information for doris module instance
     *
     * TODO：It will only be used when subsequent physical clusters
     *  support the deployment of multiple Doris application instances
     */
    // Here is the user configured installation path, such as /root/doris
    // The actual isntance installation path is /root/doris/be or /root/doris/fe or /root/doris/borker
    @Column(name = "install_info", columnDefinition = "TEXT")
    private String installInfo;

    // timestamp
    @Column(name = "create_timestamp")
    private Timestamp createTimestamp;

    @Column(name = "lastupdate_timestamp")
    private Timestamp lastUpdateTimestamp;

    @Column(name = "extra_info")
    private String extraInfo;

    public ClusterInstanceEntity(long clusterId, long moduleId, long nodeId, String installInfo, String address) {
        this.clusterId = clusterId;
        this.moduleId = moduleId;
        this.nodeId = nodeId;
        this.installInfo = installInfo;
        this.address = address;
        this.createTimestamp = new Timestamp(System.currentTimeMillis());
    }

}
