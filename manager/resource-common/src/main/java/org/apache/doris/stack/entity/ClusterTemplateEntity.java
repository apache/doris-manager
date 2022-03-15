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

/**
 * @Description：Doris cluster installation version information
 * TODO:Later, implement the Doris cluster version upgrade function
 */
@Entity
@Table(name = "cluster_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterTemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "version", length = 100)
    private String version;

    @Column(name = "description")
    private String description;

    // Installation package information. If it is a physical machine deployment, it can be an HTTP path.
    // If it is a k8s deployment, the Fe and be images are separated and can be empty here,
    // The module installation package is in the module template information
    @Column(name = "package_info", columnDefinition = "TEXT")
    private String packageInfo;
}
