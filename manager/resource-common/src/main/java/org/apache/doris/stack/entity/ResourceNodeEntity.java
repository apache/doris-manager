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
import java.util.List;

/**
 * @Description：The physical resource cluster node info
 */
@Entity
@Table(name = "resource_node")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "resource_cluster_id")
    private long resourceClusterId;

    @Column(name = "node_name", length = 100)
    private String name;

    @Column(name = "node_describe", columnDefinition = "TEXT")
    private String desc;

    @Column(name = "node_host")
    private String host;

    @Column(name = "agent_port")
    private int agentPort;

    // TODO:agent Version upgrade
    @Column(name = "desired_agent_package", columnDefinition = "TEXT")
    private String desiredAgentPackage;

    // TODO:agent Version upgrade
    @Column(name = "current_agent_package", columnDefinition = "TEXT")
    private String currentAgentPackage;

    // At present, only one Doris application can be deployed in a physical cluster.
    // The deployment path here will include the agent itself and service instances.
    // Later, multiple service instances can be deployed. All service instances are
    // in the deployment path of the instance itself
    // Here is the user configured installation path, such as /root/doris
    // The actual agent installation path is /root/doris/agent
    @Column(name = "agent_install_dir", columnDefinition = "TEXT")
    private String agentInstallDir;

    // TODO:Resource node agent target state (ModelControlState)
    @Column(name = "desired_state")
    private int desiredState;

    // TODO:Resource node agent current state (ModelControlState)
    @Column(name = "current_state")
    private int currentState;

    @Column(name = "current_event_id")
    private long currentEventId;

    // Node resource info，Information such as CPU, memory and disk
    @Column(name = "resource_info", columnDefinition = "MEDIUMTEXT")
    private String resourceInfo;

    @Column(name = "lastheartbeat_timestamp")
    private Timestamp lastHeartBeatTimestamp;

    @Column(name = "register_timestamp")
    private Timestamp registerTimestamp;

    // Node resource info
    @Data
    public static class ResourceInfo {
        // total resource
        // cpu unit: 1/1000 core
        // memory unit: 1Mb
        private int cpuTotal;

        private int memoryTotal;

        private int diskTotal;

        private List<DiskInfo> diskInfos;
    }

    // Node disk info
    @Data
    public static class DiskInfo {
        private String type;

        private int size;

        private String mountPath;

        private String puDiskId;

        private boolean share;
    }

    public ResourceNodeEntity(long resourceClusterId, String host) {
        this.resourceClusterId = resourceClusterId;
        this.host = host;
    }

}
