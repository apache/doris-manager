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
 * @Description：Doris cluster module(FE/BE) installation version information
 * TODO:It is convenient to define service and configuration information for the module later
 */
@Entity
@Table(name = "cluster_module_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterModuleTemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "template_id")
    private long templateId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "version", length = 100)
    private String version;

    // The module installation package information
    @Column(name = "package_info", columnDefinition = "TEXT")
    private String packageInfo;

    // The module extension information, such as k8s yml
    @Column(name = "extra_info", columnDefinition = "MEDIUMTEXT")
    private String extraInfo;

}
