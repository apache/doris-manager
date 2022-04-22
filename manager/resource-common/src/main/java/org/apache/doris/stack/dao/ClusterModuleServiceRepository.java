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

package org.apache.doris.stack.dao;

import org.apache.doris.stack.entity.ClusterModuleServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClusterModuleServiceRepository extends JpaRepository<ClusterModuleServiceEntity, Long> {

    @Query("select c from ClusterModuleServiceEntity c where c.clusterId = :clusterId and c.name = :name")
    List<ClusterModuleServiceEntity> getByClusterIdAndName(@Param("clusterId") long clusterId,
                                                           @Param("name") String name);

    @Query("select c from ClusterModuleServiceEntity c where c.clusterId = :clusterId")
    List<ClusterModuleServiceEntity> getByClusterId(@Param("clusterId") long clusterId);

    @Transactional
    @Modifying
    @Query("delete from ClusterModuleServiceEntity c where c.moduleId = :moduleId")
    void deleteByModuleId(@Param("moduleId") long moduleId);
}
