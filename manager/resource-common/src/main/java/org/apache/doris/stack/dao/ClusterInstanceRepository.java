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

import org.apache.doris.stack.entity.ClusterInstanceEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClusterInstanceRepository extends JpaRepository<ClusterInstanceEntity, Long> {
    @Query("select c from ClusterInstanceEntity c where c.nodeId = :nodeId")
    @Cacheable(value = "cluster_instance", key = "#p0")
    List<ClusterInstanceEntity> getByNodeId(@Param("nodeId") long nodeId);

    @Query("select c from ClusterInstanceEntity c where c.moduleId = :moduleId")
    List<ClusterInstanceEntity> getByModuleId(@Param("moduleId") long moduleId);

    @Query("select c.nodeId from ClusterInstanceEntity c where c.moduleId = :moduleId")
    List<Long> getNodeIdsByModuleId(@Param("moduleId") long moduleId);

    @Override
    @CacheEvict(value = "cluster_instance", key = "#result.nodeId")
    ClusterInstanceEntity save(ClusterInstanceEntity entity);

    @Override
    @CacheEvict(value = "cluster_instance", allEntries = true)
    void deleteById(Long id);

    @Override
    @Transactional
    @Modifying
    @CacheEvict(value = "cluster_instance", key = "#entity.nodeId")
    void delete(ClusterInstanceEntity entity);
}
