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

import org.apache.doris.stack.entity.ResourceNodeEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceNodeRepository extends JpaRepository<ResourceNodeEntity, Long> {
    @Query("select c.host from ResourceNodeEntity c where c.resourceClusterId = :resourceClusterId")
    List<String> getHostsByResourceClusterId(@Param("resourceClusterId") long resourceClusterId);

    @Query("select c from ResourceNodeEntity c where c.resourceClusterId = :resourceClusterId")
    List<ResourceNodeEntity> getByResourceClusterId(@Param("resourceClusterId") long resourceClusterId);

    @Modifying
    @Query("delete from ResourceNodeEntity c where c.resourceClusterId = :resourceClusterId and c.host = :host")
    @CacheEvict(value = "node_agent", allEntries = true)
    void deleteByResourceClusterIdAndHost(@Param("resourceClusterId") long resourceClusterId,
                                          @Param("host") String host);

    @Override
    @CachePut(value = "node_agent", key = "#result.id")
    ResourceNodeEntity save(ResourceNodeEntity entity);

    @Override
    @Cacheable(value = "node_agent", key = "#p0")
    Optional<ResourceNodeEntity> findById(Long id);

    @Override
    @CacheEvict(value = "node_agent", key = "#p0")
    void deleteById(Long id);

    @Override
    @CacheEvict(value = "node_agent", key = "#entity.id")
    void delete(ResourceNodeEntity entity);
}
