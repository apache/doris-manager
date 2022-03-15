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

import org.apache.doris.stack.entity.CoreUserEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CoreUserRepository extends JpaRepository<CoreUserEntity, Integer> {
    @Query("select c from CoreUserEntity c where c.email = :email")
    List<CoreUserEntity> getByEmail(@Param("email") String email);

    @Query("select c from CoreUserEntity c where c.firstName = :firstName")
    List<CoreUserEntity> getByFirstName(@Param("firstName") String firstName);

    @Query("select c from CoreUserEntity c where c.entryUUID = :entryUUID")
    List<CoreUserEntity> getByEntryUUID(@Param("entryUUID") String entryUUID);

    @Query("select c.id from CoreUserEntity c where c.entryUUID = ''")
    List<Integer> getByEmptyEntryUUID();

    @Query("select c from CoreUserEntity c where c.entryUUID is null")
    List<CoreUserEntity> getByNullEntryUUID();

    @Query("select c from CoreUserEntity c where c.email = :email and c.ldapAuth = :ldapAuth")
    List<CoreUserEntity> getByEmailAndLdapAuth(@Param("email") String email, @Param("ldapAuth") boolean ldapAuth);

    @Query("select c from CoreUserEntity c where c.firstName = :firstName and c.ldapAuth = :ldapAuth")
    List<CoreUserEntity> getByFirstNameAndLdapAuth(@Param("firstName") String firstName,
                                                   @Param("ldapAuth") boolean ldapAuth);

    @Query("select c from CoreUserEntity c where c.isSuperuser = :isSuperuser and c.isActive = :isActive")
    List<CoreUserEntity> getActiveAdminUser(@Param("isSuperuser") boolean isSuperuser,
                                            @Param("isActive") boolean isActive);

    @Query("select c from CoreUserEntity c where c.isActive = :isActive")
    List<CoreUserEntity> getByActive(@Param("isActive") boolean isActive);

    @Query("select c from CoreUserEntity c where c.clusterId = :clusterId")
    List<CoreUserEntity> getByClusterId(@Param("clusterId") long clusterId);

    @Query("select c.id from CoreUserEntity c where c.ldapAuth = false and c.id in (:userIds)")
    List<Integer> getAllStudioUser(@Param("userIds") List<Integer> userIds);

    @Query("select c.id from CoreUserEntity c where c.ldapAuth = true and c.id in (:userIds)")
    List<Integer> getAllLdapUser(@Param("userIds") List<Integer> userIds);

    @Query("select c from CoreUserEntity c where c.firstName like %?1%")
    List<CoreUserEntity> getAllUsers(@Param("q") String q);

    @Transactional
    @Modifying
    @Query("delete from CoreUserEntity c where c.id in (:userIds)")
    void deleteByUserIds(@Param("userIds") List<Integer> userIds);

    @Query("select c from CoreUserEntity c where c.email = :email and c.idaasAuth = :idaasAuth")
    List<CoreUserEntity> getByEmailAndIdaasAuth(@Param("email") String email, @Param("idaasAuth") boolean idaasAuth);

    @Transactional
    @Modifying
    @Query("delete from CoreUserEntity c")
    void deleteAllUsers();

    @Override
    // Cache user information
    @Cacheable(value = "sessions", key = "#p0")
    Optional<CoreUserEntity> findById(Integer id);

    @Override
    // Delete cache and delete data at the same time
    @CacheEvict(value = "sessions", key = "#p0")
    void deleteById(Integer id);

    // Delete cache, data has been deleted in batch
    @CacheEvict(value = "sessions", key = "#p0")
    void deleteUserById(Integer id);

    @Override
    // Update user information, update cache, and the addition will not affect
    @CachePut(value = "sessions", key = "#userEntity.id")
    CoreUserEntity save(CoreUserEntity userEntity);
}
