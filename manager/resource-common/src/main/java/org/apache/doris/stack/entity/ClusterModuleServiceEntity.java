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

@Entity
@Table(name = "cluster_module_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterModuleServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", length = 100)
    private String name = "";

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "service_template_id")
    private long serviceTemplateId;

    @Column(name = "cluster_id")
    private long clusterId;

    @Column(name = "module_id")
    private long moduleId;

    private int port;

    @Column(name = "address_info", columnDefinition = "TEXT")
    private String addressInfo;

    // The other info json information
    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    public ClusterModuleServiceEntity(String name, long clusterId, long moduleId, int port, String addressInfo) {
        this.name = name;
        this.clusterId = clusterId;
        this.moduleId = moduleId;
        this.port = port;
        this.addressInfo = addressInfo;
    }

}
