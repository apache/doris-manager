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

import org.apache.doris.stack.entity.HeartBeatEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HeartBeatEventRepository extends JpaRepository<HeartBeatEventEntity, Long> {

    @Query("select c from HeartBeatEventEntity c where c.requestId = :requestId and c.completed = :completed")
    List<HeartBeatEventEntity> getByRequestIdAndCompleted(@Param("requestId") long requestId,
                                                          @Param("completed") boolean completed);

    @Query("select c.status from HeartBeatEventEntity c where c.requestId = :requestId")
    Set<String> getStatusByRequestId(@Param("requestId") long requestId);

    @Override
    @CachePut(value = "heart_beat", key = "#result.id")
    HeartBeatEventEntity save(HeartBeatEventEntity entity);

    @Override
    @Cacheable(value = "heart_beat", key = "#p0")
    Optional<HeartBeatEventEntity> findById(Long id);

    @Override
    @CacheEvict(value = "heart_beat", key = "#p0")
    void deleteById(Long id);

    @Override
    @CacheEvict(value = "heat_beat", key = "#entity.id")
    void delete(HeartBeatEventEntity entity);
}
