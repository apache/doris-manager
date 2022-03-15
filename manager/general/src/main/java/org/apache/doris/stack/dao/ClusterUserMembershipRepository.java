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

import org.apache.doris.stack.entity.ClusterUserMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClusterUserMembershipRepository extends JpaRepository<ClusterUserMembershipEntity, Integer> {

    @Query("select s from ClusterUserMembershipEntity s where s.userId = :userId")
    List<ClusterUserMembershipEntity> getByUserId(@Param("userId") int userId);

    @Query("select s from ClusterUserMembershipEntity s where s.clusterId = :clusterId")
    List<ClusterUserMembershipEntity> getByClusterId(@Param("clusterId") long clusterId);

    @Query("select s.userId from ClusterUserMembershipEntity s where s.clusterId = :clusterId")
    List<Integer> getUserIdsByClusterId(@Param("clusterId") long clusterId);

    @Query("select s from ClusterUserMembershipEntity s where s.userId = :userId and s.clusterId = :clusterId")
    List<ClusterUserMembershipEntity> getByUserIdAndClusterId(@Param("userId") int userId,
                                                               @Param("clusterId") long clusterId);

    @Modifying
    @Query("delete from ClusterUserMembershipEntity p where p.userId = :userId and p.clusterId = :clusterId")
    void deleteByUserIdAndClusterId(@Param("userId") int userId, @Param("clusterId") long clusterId);

    @Modifying
    @Query("delete from ClusterUserMembershipEntity p where p.clusterId = :clusterId")
    void deleteByClusterId(@Param("clusterId") long clusterId);

    @Modifying
    @Query("delete from ClusterUserMembershipEntity p where p.userId = :userId")
    void deleteByUserId(@Param("userId") int userId);
}
