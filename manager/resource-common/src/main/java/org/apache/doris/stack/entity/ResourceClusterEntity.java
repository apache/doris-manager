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
import org.apache.doris.stack.control.ResourceClusterType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Description：The physical resource cluster definition of Doris cluster deployment
 * can be either a physical machine cluster or a k8s cluster
 */
@Entity
@Table(name = "resource_cluster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceClusterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // type of resource cluster(TODO:Currently, the default is physical machine deployment)
    @Column(name = "type", length = 20)
    private String type = ResourceClusterType.PM.name();

    // ID of the user who created the resource cluster
    @Column(name = "user_id")
    private String userId;

    // Resource cluster target state(ModelControlState)
    @Column(name = "desired_state")
    private int desiredState;

    // Resource cluster current state(ModelControlState)
    @Column(name = "current_state")
    private int currentState;

    // The installation package of Doris application obtains information.
    // The physical machine cluster is the download address and the k8s cluster is the address of the image warehouse
    @Column(name = "registry_info", columnDefinition = "TEXT")
    private String registryInfo;

    // Resource cluster access information, such as authentication information of k8s,
    // SSH information of physical machine, etc
    @Column(name = "access_info", columnDefinition = "MEDIUMTEXT")
    private String accessInfo;

    // Resource cluster extension information, such as k8s some network or additional information
    @Column(name = "extra_info", columnDefinition = "MEDIUMTEXT")
    private String extraInfo;

    public ResourceClusterEntity(String userId, String accessInfo) {
        this.userId = userId;
        this.accessInfo = accessInfo;
        // TODO:Currently only physical machines are supported
        this.type = ResourceClusterType.PM.name();
    }
}
