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

package org.apache.doris.stack.component;

import org.apache.doris.stack.dao.ClusterUserMembershipRepository;
import org.apache.doris.stack.entity.ClusterUserMembershipEntity;
import org.apache.doris.stack.exception.UserNoSelectClusterException;
import org.apache.doris.stack.model.request.user.UserGroupRole;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.CoreUserRepository;
import org.apache.doris.stack.dao.PermissionsGroupMembershipRepository;
import org.apache.doris.stack.dao.PermissionsGroupRoleRepository;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.PermissionsGroupMembershipEntity;
import org.apache.doris.stack.entity.PermissionsGroupRoleEntity;
import org.apache.doris.stack.exception.NoPermissionException;
import org.apache.doris.stack.model.response.user.GroupMember;
import org.apache.doris.stack.service.BaseService;
import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.util.CredsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Description：The engine cluster management tool class is mainly responsible for
 * verifying whether the user has the permission of cluster space
 */
@Component
@Slf4j
public class ClusterUserComponent extends BaseService {

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Autowired
    private PermissionsGroupRoleRepository groupRoleRepository;

    @Autowired
    private PermissionsGroupMembershipRepository membershipRepository;

    @Autowired
    private CoreUserRepository userRepository;

    @Autowired
    private ClusterUserMembershipRepository clusterUserMembershipRepository;

    /**
     * Obtain the user's current space information.
     * All API accesses in the space need to obtain the current space ID
     * @param user
     * @return
     * @throws Exception
     */
    public ClusterInfoEntity getUserCurrentCluster(CoreUserEntity user) throws Exception {
        int clusterId = user.getClusterId();
        if (clusterId < 1) {
            log.error("The user do not have current cluster");
            throw new UserNoSelectClusterException();
        }

        Optional<ClusterInfoEntity> clusterInfoEntityOp = clusterInfoRepository.findById((long) clusterId);
        if (clusterInfoEntityOp.equals(Optional.empty())) {
            log.error("The user current cluster {} has been deleted", clusterId);
            throw new UserNoSelectClusterException();
        }
        ClusterInfoEntity clusterInfoEntity = clusterInfoEntityOp.get();
        try {
            clusterInfoEntity.setPasswd(CredsUtil.aesDecrypt(clusterInfoEntity.getPasswd()));
        } catch (Exception e) {
            log.warn("execute more than once select in one thread, the password was cached. msg {}", e.getMessage());
        }

        return clusterInfoEntity;
    }

    // add a user for all space which doesn't have default user
    public void addDefaultUserForSpace() throws Exception {
        log.debug("add default user");
        List<ClusterInfoEntity> clusterInfoEntities = clusterInfoRepository.findAll();
        for (ClusterInfoEntity clusterInfo : clusterInfoEntities) {
            int adminGroupUserId = clusterInfo.getAdminGroupId();
            int allUserGroupId = clusterInfo.getAllUserGroupId();
            int clusterId = (int) clusterInfo.getId();
            if (membershipRepository.getByUserId(-clusterId).size() < 2) {

                addDefaultUserForSpace(clusterId, adminGroupUserId, allUserGroupId);
            }
        }
    }

    public void addDefaultUserForSpace(int clusterId, int adminGroupUserId, int allUserGroupId) throws Exception {
        log.debug("add default user for space");
        // add into admin group
        PermissionsGroupMembershipEntity adminMembershipEntity = new PermissionsGroupMembershipEntity();
        adminMembershipEntity.setUserId(-clusterId);
        adminMembershipEntity.setGroupId(adminGroupUserId);
        membershipRepository.save(adminMembershipEntity);
        // add into all user group
        PermissionsGroupMembershipEntity allUserMembershipEntity = new PermissionsGroupMembershipEntity();
        allUserMembershipEntity.setUserId(-clusterId);
        allUserMembershipEntity.setGroupId(allUserGroupId);
        membershipRepository.save(allUserMembershipEntity);
    }

    /**
     * Judge whether the user is in the space
     * @param userId
     * @return
     * @throws Exception
     */
    public boolean checkUserBelongToCluster(int userId, int clusterId) throws Exception {
        // built in user
        if (userId == BuiltInUserComponent.BUILT_USER_ID) {
            return true;
        }
        List<ClusterUserMembershipEntity> clusterUserMembershipEntities =
                clusterUserMembershipRepository.getByUserIdAndClusterId(userId, clusterId);
        if (clusterUserMembershipEntities.isEmpty()) {
            log.error("The user {} is not a space {} user.", userId, clusterId);
            throw new NoPermissionException();
        }
        return true;
    }

    /**
     * Judge whether the user is in the space
     * @param userId
     * @return
     * @throws Exception
     */
    public boolean userBelongToCluster(int userId, int clusterId) {
        List<ClusterUserMembershipEntity> clusterUserMembershipEntities =
                clusterUserMembershipRepository.getByUserIdAndClusterId(userId, clusterId);
        if (clusterUserMembershipEntities.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Determine whether the user is the administrator role of a space
     * @param userId
     * @param clusterInfo
     * @return
     */
    public boolean userIsClusterAdminRole(int userId, ClusterInfoEntity clusterInfo) {
        int adminGroupRoleId = clusterInfo.getAdminGroupId();
        List<PermissionsGroupMembershipEntity> membershipEntities =
                membershipRepository.getByUserIdAndGroupId(userId, adminGroupRoleId);
        if (membershipEntities == null || membershipEntities.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Add a normal user to the space
     * @param userId
     * @param clusterInfo
     */
    public void addUserToCluster(int userId, ClusterInfoEntity clusterInfo) {
        ClusterUserMembershipEntity membershipEntity =
                new ClusterUserMembershipEntity(userId, (int) clusterInfo.getId());
        clusterUserMembershipRepository.save(membershipEntity);

        addGroupUserMembership(userId, clusterInfo.getAllUserGroupId());
    }

    /**
     * Add an administrator user to the space
     * @param userId
     * @param clusterInfo
     */
    public void addAdminUserToCluster(int userId, ClusterInfoEntity clusterInfo) {
        addUserToCluster(userId, clusterInfo);
        addGroupUserMembership(userId, clusterInfo.getAdminGroupId());
    }

    /**
     * Check whether the user is the administrator role user of the space
     * Available to space level APIs
     * @param user
     * @return
     * @throws Exception
     */
    public int getUserCurrentClusterIdAndCheckAdmin(CoreUserEntity user) throws Exception {
        ClusterInfoEntity cluster = getUserCurrentCluster(user);
        int clusterId = (int) cluster.getId();
        if (!user.isSuperuser() && !user.getIsClusterAdmin()) {
            log.error("The user {} not cluster {} space admin", user.getId(), clusterId);
            throw new NoPermissionException();
        }
        return clusterId;
    }

    /**
     * Check whether the user is the administrator role user of the space
     * Available to space level APIs
     * @param user
     * @return
     * @throws Exception
     */
    public ClusterInfoEntity getUserCurrentClusterAndCheckAdmin(CoreUserEntity user) throws Exception {
        ClusterInfoEntity cluster = getUserCurrentCluster(user);
        if (!user.isSuperuser() && !user.getIsClusterAdmin()) {
            log.error("The user {} not cluster {} space admin", user.getId(), cluster.getId());
            throw new NoPermissionException();
        }
        return cluster;
    }

    /**
     * Check whether the user has the operation permission of the administrator role of the space
     * Space level API usage
     * @param user
     * @param clusterId
     * @return
     * @throws Exception
     */
    public ClusterInfoEntity checkUserClusterAdminPermission(CoreUserEntity user, int clusterId) throws Exception {
        // The super admin user has all space administrator permissions by default and can operate directly
        if (user.isSuperuser()) {
            return clusterInfoRepository.findById((long) clusterId).get();
        } else {
            int userId = user.getId();
            ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById((long) clusterId).get();

            boolean isAdmin = userIsClusterAdminRole(userId, clusterInfoEntity);

            if (!isAdmin) {
                log.error("The user {} is not a space {} user.", userId, clusterId);
                throw new NoPermissionException();
            }
            return clusterInfoEntity;
        }
    }

    /**
     * Check whether the user has the operation permission of the administrator role of the space
     * APIs that can be accessed at both platform and space levels are used
     * @param user
     * @param clusterId
     * @return
     * @throws Exception
     */
    public void checkUserSpuerAdminOrClusterAdmin(CoreUserEntity user, int clusterId) throws Exception {
        // The super admin user has all space administrator permissions by default and can operate directly
        if (user.isSuperuser()) {
            return;
        } else {
            int userId = user.getId();
            ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById((long) clusterId).get();

            boolean isAdmin = userIsClusterAdminRole(userId, clusterInfoEntity);

            if (!isAdmin) {
                log.error("The user {} is not a space {} user.", userId, clusterId);
                throw new NoPermissionException();
            }
            return;
        }
    }

    /**
     * Initialize the correspondence between permission groups and users
     *
     * @param userId
     * @param groupId
     * @return
     */
    public int addGroupUserMembership(int userId, int groupId) {
        PermissionsGroupMembershipEntity amdinMembershipEntity =
                new PermissionsGroupMembershipEntity(userId, groupId);
        return membershipRepository.save(amdinMembershipEntity).getId();
    }

    /**
     * Create a user group and return the user group ID
     *
     * @return
     */
    public int addPermissionsGroup(String name, int clusterId, UserGroupRole role) {
        // Create a user group and bind the relationship with the Doris cluster and user information.
        PermissionsGroupRoleEntity groupRoleEntity =
                new PermissionsGroupRoleEntity(name, role.name(), clusterId);
        int groupId = groupRoleRepository.save(groupRoleEntity).getGroupId();
        log.debug("create group {}.", groupId);

        return groupId;
    }

    /**
     * Create a user group and return the user group ID
     *
     * @return
     */
    public int addPermissionsGroup(String name, int clusterId, UserGroupRole role, String dorisUserName, String passwd) {
        // Create a user group and bind the relationship with the Doris cluster and user information.
        PermissionsGroupRoleEntity groupRoleEntity =
                new PermissionsGroupRoleEntity(name, role.name(), clusterId, dorisUserName, passwd);
        int groupId = groupRoleRepository.save(groupRoleEntity).getGroupId();
        log.debug("create group {}.", groupId);

        return groupId;
    }

    /**
     * Save user group password information
     * @Param
     * @return
     */
    public PermissionsGroupRoleEntity addPermissionsGroupWithPaloUser(String name, int clusterId,
                                                                      UserGroupRole role, String password,
                                                                      String userName) throws Exception {
        // Create a user group and bind the relationship with the Doris cluster and user information.
        password = CredsUtil.aesEncrypt(password);
        PermissionsGroupRoleEntity groupRoleEntity =
                new PermissionsGroupRoleEntity(name, role.name(), clusterId, userName, password);
        int groupId = groupRoleRepository.save(groupRoleEntity).getGroupId();
        log.debug("create group {}.", groupId);

        return groupRoleEntity;
    }

    /**
     * Get all members of a permission group
     *
     * @param groupId
     * @return
     */
    public List<GroupMember> getGroupMembers(int groupId) {
        log.debug("get group {} all members.", groupId);
        List<Integer> stopLdapUsers = userRepository.getByEmptyEntryUUID();
        List<PermissionsGroupMembershipEntity> users = membershipRepository.getByGroupId(groupId);

        List<GroupMember> members = new ArrayList<>();
        for (PermissionsGroupMembershipEntity membershipEntity : users) {
            // get user
            if (membershipEntity.getUserId() < 0 || stopLdapUsers.contains(membershipEntity.getUserId())) {
                continue;
            }
            CoreUserEntity userEntity = userRepository.findById(membershipEntity.getUserId()).get();

            // construct Member
            GroupMember member = new GroupMember();
            member.setMembershipId(membershipEntity.getId());
            member.setUserId(membershipEntity.getUserId());
            member.setName(userEntity.getFirstName());
            member.setEmail(userEntity.getEmail());

            // add list
            members.add(member);
        }
        return members;
    }

    // 对集群未加密密码进行加密
    public void encryptClusterPassword() throws Exception {
        log.debug("encrypt password for cluster");
        List<ClusterInfoEntity> clusterInfoEntities = clusterInfoRepository.findAll();
        log.debug("encrypt password for cluster, size is {}", clusterInfoEntities.size());
        for (ClusterInfoEntity clusterInfo : clusterInfoEntities) {
            if (clusterInfo.getPasswd() == null) {
                log.debug("cluster password is null");
                continue;
            }
            if (!clusterInfo.getPasswd().trim().equals("")) {
                try {
                    CredsUtil.aesDecrypt(clusterInfo.getPasswd());
                    log.debug("password has been encrypted");
                    continue;
                } catch (Exception e) {
                    log.debug("password has not been encrypted");
                }
            }
            // 空字符串加密后还是空
            clusterInfo.setPasswd(CredsUtil.aesEncrypt(clusterInfo.getPasswd()));
            clusterInfoRepository.save(clusterInfo);
        }
    }
}
