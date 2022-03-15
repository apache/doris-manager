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

package org.apache.doris.stack.service.user;

import static java.util.Comparator.comparing;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.component.BuiltInUserComponent;
import org.apache.doris.stack.component.ClusterUserComponent;
import org.apache.doris.stack.component.LdapComponent;
import org.apache.doris.stack.component.MailComponent;
import org.apache.doris.stack.component.SettingComponent;
import org.apache.doris.stack.component.UserActivityComponent;
import org.apache.doris.stack.constant.PropertyDefine;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.ClusterUserMembershipRepository;
import org.apache.doris.stack.dao.CoreSessionRepository;
import org.apache.doris.stack.dao.CoreUserRepository;
import org.apache.doris.stack.dao.PermissionsGroupMembershipRepository;
import org.apache.doris.stack.dao.PermissionsGroupRoleRepository;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.PermissionsGroupMembershipEntity;
import org.apache.doris.stack.entity.SettingEntity;
import org.apache.doris.stack.exception.NoPermissionException;
import org.apache.doris.stack.exception.RequestFieldNullException;
import org.apache.doris.stack.exception.ResetPasswordException;
import org.apache.doris.stack.exception.UserActiveException;
import org.apache.doris.stack.exception.UserEmailDuplicatedException;
import org.apache.doris.stack.exception.UserNameDuplicatedException;
import org.apache.doris.stack.exception.UserNoSelectClusterException;
import org.apache.doris.stack.exception.UserOperationSelfException;
import org.apache.doris.stack.model.activity.Topic;
import org.apache.doris.stack.model.request.config.InitStudioReq;
import org.apache.doris.stack.model.request.user.AdminUpdateReq;
import org.apache.doris.stack.model.request.user.NewUserAddReq;
import org.apache.doris.stack.model.request.user.PasswordUpdateReq;
import org.apache.doris.stack.model.request.user.UserUpdateReq;
import org.apache.doris.stack.model.response.user.UserInfo;
import org.apache.doris.stack.service.BaseService;
import org.apache.doris.stack.service.UtilService;
import org.apache.doris.stack.service.config.ConfigConstant;
import org.apache.doris.stack.util.DeployType;
import org.apache.doris.stack.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NewUserService extends BaseService {

    @Autowired
    private CoreSessionRepository sessionRepository;

    @Autowired
    private CoreUserRepository userRepository;

    @Autowired
    private PermissionsGroupMembershipRepository membershipRepository;

    @Autowired
    private UtilService utilService;

    @Autowired
    private MailComponent mailComponent;

    @Autowired
    private ClusterUserComponent clusterUserComponent;

    @Autowired
    private LdapComponent ldapComponent;

    @Autowired
    private SettingComponent settingComponent;

    @Autowired
    private PermissionsGroupRoleRepository permissionsGroupRoleRepository;

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private ClusterUserMembershipRepository clusterUserMembershipRepository;

    @Autowired
    private UserActivityComponent activityComponent;

    /**
     * Admin user get all users
     * @param requestUser
     * @param queryStr
     * @param includeDeactivated
     * @return
     * @throws Exception
     */
    public List<UserInfo> getAllUser(CoreUserEntity requestUser, boolean includeDeactivated, String queryStr) throws Exception {
        log.debug("Get all users {} include not active user", includeDeactivated);

        long clusterId = requestUser.getClusterId();
        clusterUserComponent.checkUserSpuerAdminOrClusterAdmin(requestUser, clusterId);

        List<CoreUserEntity> userEntities;
        if (includeDeactivated) {
            // get all user
            userEntities = userRepository.findAll();
        } else {
            // get active user
            userEntities = userRepository.getByActive(true);
        }
        // filter sync stop ldap user
        userEntities =
                userEntities.stream().filter(e -> (e.getEntryUUID() == null || !e.getEntryUUID().equals(""))).collect(Collectors.toList());

        // Search filtering
        boolean isSearch = !queryStr.isEmpty();

        List<UserInfo> userInfos = Lists.newArrayList();
        for (CoreUserEntity userEntity : userEntities) {
            if (isSearch && !userEntity.getFirstName().contains(queryStr)) {
                continue;
            }
            UserInfo userInfo = userEntity.castToUserInfoWithoutClusterInfo();
            userInfo.setIsSpaceUser(clusterUserComponent.userBelongToCluster(userEntity.getId(), clusterId));
            userInfos.add(userInfo);
        }

        userInfos.sort(comparing(UserInfo::isSuperAdmin).reversed());

        return userInfos;
    }

    /**
     * The user information is obtained according to the ID,
     * which can only be obtained by the user itself or the admin user
     *
     * @param userId
     * @param requestUser
     * @return
     * @throws Exception
     */
    public UserInfo getUserById(int userId, CoreUserEntity requestUser) throws Exception {
        int requestId = requestUser.getId();
        log.debug("{} get user {} information", requestId, userId);

        // If the requesting user is an admin user, or the user is viewing the user itself,
        // the result is returned directly
        if (userId == requestId) {
            return requestUser.castToUserInfo();
        }

        // Super Admin users can view the information of any user
        if (requestUser.isSuperuser()) {
            CoreUserEntity userEntity = userRepository.findById(userId).get();
            return userEntity.castToUserInfo();
        }

        // If you are operating in a space, the space administrator role can view all users in the space
        long clusterId = requestUser.getClusterId();
        if (clusterId > 0) {
            ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById((long) clusterId).get();
            List<PermissionsGroupMembershipEntity> groupMembershipEntities =
                    membershipRepository.getByUserIdAndGroupId(requestId, clusterInfoEntity.getAdminGroupId());
            if (groupMembershipEntities.isEmpty()) {
                log.error("The user {} is not a space {} admin user.", requestId, clusterId);
                throw new NoPermissionException();
            }
            clusterUserComponent.checkUserBelongToCluster(userId, clusterId);
            CoreUserEntity userEntity = userRepository.findById(userId).get();
            return userEntity.castToUserInfo();
        }

        log.error("No permission error");
        throw new NoPermissionException();
    }

    /**
     * Get current user information
     *
     * @param user
     * @return
     * @throws Exception
     */
    public UserInfo getCurrentUser(CoreUserEntity user) throws Exception {
        log.debug("Get current user by id {}.", user.getId());

        UserInfo userInfo = user.castToUserInfo();

        long clusterId = userInfo.getSpaceId();

        boolean managerEnable = false;
        if (clusterId > 0) {
            Optional<ClusterInfoEntity> clusterInfoOp = clusterInfoRepository.findById((long) clusterId);
            if (!clusterInfoOp.equals(Optional.empty())) {
                ClusterInfoEntity clusterInfo = clusterInfoOp.get();
                userInfo.setCollectionId(clusterInfo.getCollectionId());
                userInfo.setSpaceName(clusterInfo.getName());
                managerEnable = clusterInfo.isManagerEnable();
            } else {
                log.error("The user current cluster {} has been deleted", clusterId);
                throw new UserNoSelectClusterException();
            }
        }

        SettingEntity authType = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);
        if (!authType.equals(Optional.empty()) && !StringUtils.isEmpty(authType.getValue())) {
            userInfo.setAuthType(InitStudioReq.AuthType.valueOf(authType.getValue()));
        }

        String deployName = environment.getProperty(PropertyDefine.DEPLOY_TYPE_PROPERTY);
        userInfo.setDeployType(deployName);
        if (deployName.equals(DeployType.manager.getName())) {
            log.debug("manager deploy manager enable true.");
            managerEnable = true;
        }

        userInfo.setManagerEnable(managerEnable);

        return userInfo;
    }

    /**
     * update user current cluster id
     *
     * @param user
     * @return
     * @throws Exception
     */
    @Transactional
    public UserInfo updateUserCurrentCluster(CoreUserEntity user, long clusterId) throws Exception {
        log.debug("Update current user cluster id to {}.", clusterId);
        // Cluster ID before switching
        long oldClusterId = user.getClusterId();

        // check cluster Id
        Optional<ClusterInfoEntity> clusterInfoEntityOp = clusterInfoRepository.findById((long) clusterId);
        if (clusterInfoEntityOp.equals(Optional.empty())) {
            log.error("The cluster {} not exist", clusterId);
            throw new RequestFieldNullException();
        }

        // built in user
        if (user.getId() == BuiltInUserComponent.BUILT_USER_ID) {
            BuiltInUserComponent.builtInUser.setClusterId(clusterId);
            BuiltInUserComponent.builtInUser.setIsClusterAdmin(true);
            return getCurrentUser(BuiltInUserComponent.builtInUser);
        }

        if (user.isSuperuser()) {
            log.debug("Admin user change current cluster");
        } else {
            log.debug("User change current cluster");
            if (!clusterUserComponent.userBelongToCluster(user.getId(), clusterId)) {
                log.error("The user {} not belong to cluster {}", user.getId(), clusterId);
                throw new NoPermissionException();
            }
        }
        user.setClusterId(clusterId);
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        user.setIsClusterAdmin(isUserClusterAdmin(user, clusterInfoEntityOp.get()));
        userRepository.save(user);

        if (oldClusterId > 0) {
            activityComponent.userSwitchSpace(user.getId(), oldClusterId, Topic.USE_EXIT);
        }
        activityComponent.userSwitchSpace(user.getId(), clusterId, Topic.USE_ACCESS);

        return getCurrentUser(user);
    }

    /**
     * Add a new user. Please reset the password
     *
     * @param userAddReq
     * @param invitorUser
     * @throws Exception
     */
    public UserInfo addUser(NewUserAddReq userAddReq, CoreUserEntity invitorUser) throws Exception {
        if (ldapComponent.enabled()) {
            log.error("LDAP authentication does not support adding new users independently");
            throw new Exception("LDAP authentication does not support adding new users independently");
        }

        int invitorId = invitorUser.getId();
        log.debug("Invitor {} add a new user", invitorId);
        checkRequestBody(userAddReq.hasEmptyField());

        utilService.userNameCheck(userAddReq.getName());
        checkNameDuplicate(userAddReq.getName());

        // Is the mailbox empty
        if (!StringUtils.isEmpty(userAddReq.getEmail())) {
            // Check whether the mailbox format meets the specification
            utilService.emailCheck(userAddReq.getEmail());
            checkEmailDuplicate(userAddReq.getEmail());
        }

        utilService.newPasswordCheck(userAddReq.getPassword());

        CoreUserEntity newUser = new CoreUserEntity(userAddReq);

        utilService.setPassword(newUser, userAddReq.getPassword());

        // Set password reset token
        String resetTokenStr = utilService.resetUserToken(newUser, true);

        CoreUserEntity saveNewUser = userRepository.save(newUser);

        UserInfo userInfo = saveNewUser.castToUserInfo();

        log.debug("Add user success.");

        // TODO:Invite to send mail
        if (StringUtils.isEmpty(userAddReq.getEmail())) {
            // send the reset password link
            String joinUrl = utilService.getUserJoinUrl(saveNewUser.getId(), resetTokenStr);

            // Send user invitation email
            mailComponent.sendInvitationMail(userAddReq.getEmail(), invitorUser.getFirstName(), joinUrl);

            log.debug("Send invitation mail.");
        }

        return userInfo;
    }

    /**
     * Admin user update other users information,or users update their own information
     * @param userUpdateReq
     * @param requestUser
     * @param userId
     * @return
     * @throws Exception
     */
    public UserInfo updateUser(UserUpdateReq userUpdateReq, CoreUserEntity requestUser, int userId) throws Exception {
        int requestId = requestUser.getId();
        log.debug("user {} update user {} info.", requestId, userId);

        checkRequestBody(userUpdateReq.hasEmptyField());

        checUserSelfOrSuperUser(requestUser, userId);

        // TODO：Deep copy or Shallow copy
        CoreUserEntity userEntity = requestId == userId ? requestUser : userRepository.findById(userId).get();

        utilService.checkUserActive(userEntity);

        // update email
        if (!StringUtils.isEmpty(userUpdateReq.getEmail()) && !userUpdateReq.getEmail().equals(userEntity.getEmail())) {
            // check new email duplicate
            checkEmailDuplicate(userUpdateReq.getEmail());

            // Check whether the mailbox format meets the specification
            utilService.emailCheck(userUpdateReq.getEmail());
            log.debug("The mail changed.");

            userEntity.setEmail(userUpdateReq.getEmail());
        }

        // update name
        if (!userUpdateReq.getName().equals(userEntity.getFirstName())) {
            // check new name format
            utilService.userNameCheck(userUpdateReq.getName());
            // check new name duplicate
            checkNameDuplicate(userUpdateReq.getName());
            userEntity.setFirstName(userUpdateReq.getName());
        }

        userEntity.setLocale(userUpdateReq.getLocale());
        userEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        CoreUserEntity updateUser = userRepository.save(userEntity);
        log.debug("update user into database.");

        return updateUser.castToUserInfo();
    }

    public UserInfo reactivateUser(int userId, CoreUserEntity requestUser) throws Exception {
        int requestId = requestUser.getId();
        log.debug("Admin user {} reactivate not active user {}.", requestId, userId);

        checkUserSlefException(requestId, userId);

        CoreUserEntity user = userRepository.findById(userId).get();
        if (user.isActive()) {
            log.error("Reactivate an active user");
            throw new UserActiveException();
        }

        log.debug("Update user to active.");
        user.setActive(true);
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        CoreUserEntity updateUser = userRepository.save(user);
        return updateUser.castToUserInfo();
    }

    public boolean stopUser(int userId, CoreUserEntity requestUser) throws Exception {
        int requestId = requestUser.getId();
        log.debug("Admin user {} stop active user {}.", requestId, userId);
        CoreUserEntity user = userRepository.findById(userId).get();
        utilService.checkUserActive(user);
        checkUserSlefException(requestId, userId);

        log.debug("Update user to not active.");
        user.setActive(false);
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
        return true;
    }

    /**
     * Modify the user password and delete all old sessions of the user
     *
     * @param updateReq
     * @param userId
     * @param requestUser
     * @return
     * @throws Exception
     */
    @Transactional
    public UserInfo updatePassword(PasswordUpdateReq updateReq, int userId,
                                   CoreUserEntity requestUser) throws Exception {
        int requestId = requestUser.getId();
        log.debug("user {} update user {} password", requestId, userId);
        checUserSelfOrSuperUser(requestUser, userId);

        checkRequestBody(updateReq.hasEmptyField());

        // Users modify their own passwords
        if (requestId == userId) {
            log.debug("user {} update his password", requestId);
            if (updateReq.getOldPassword() == null) {
                log.error("old password null");
                throw new RequestFieldNullException();
            }

            if (updateReq.checkPasswdSame()) {
                log.error("The new password is the same as the old one.");
                throw new ResetPasswordException();
            }

            // Verify that the old password is correct
            utilService.verifyPassword(requestUser.getPasswordSalt(), updateReq.getOldPassword(), requestUser.getPassword());

            // Check whether the new password meets the specification
            utilService.newPasswordCheck(updateReq.getPassword());

            // update password
            utilService.setPassword(requestUser, updateReq.getPassword());

            // save new userInfo
            CoreUserEntity updateUser = userRepository.save(requestUser);

            // The password has been changed.
            // You want to delete the previous authentication session information of the user
            log.debug("Delete user {} old session.", requestId);
            sessionRepository.deleteByUserId(requestId);
            return updateUser.castToUserInfo();
        } else {
            log.debug("Admin user {} reset user {} password", requestId, userId);
            CoreUserEntity userEntity = userRepository.findById(userId).get();
            utilService.checkUserActive(userEntity);

            // Check whether the new password meets the specification
            utilService.newPasswordCheck(updateReq.getPassword());
            utilService.setPassword(userEntity, updateReq.getPassword());

            // save new userInfo
            CoreUserEntity updateUser = userRepository.save(userEntity);

            log.debug("Delete user {} old session.", userId);
            sessionRepository.deleteByUserId(userId);

            return updateUser.castToUserInfo();
        }
    }

    public UserInfo updateUserAdmin(AdminUpdateReq updateReq, int userId, CoreUserEntity requestUser) throws Exception {
        int requestId = requestUser.getId();
        log.debug("user {} change user {} admin {}", requestId, userId, updateReq.isAdmin());

        checkUserSlefException(requestId, userId);

        CoreUserEntity userEntity = userRepository.findById(userId).get();
        utilService.checkUserActive(userEntity);

        if (userEntity.isSuperuser() == updateReq.isAdmin()) {
            log.error("The request error, the admin attribute of the user is the same as the request");
            throw new Exception("The request error, the admin attribute of the user is the same as the request");
        } else {
            userEntity.setSuperuser(updateReq.isAdmin());
            CoreUserEntity updateUser = userRepository.save(userEntity);
            return updateUser.castToUserInfo();
        }
    }

    public boolean setQbnewb(CoreUserEntity requestUser, int userId) throws Exception {
        log.debug("Set qpnewb false.");
        int requestId = requestUser.getId();

        checUserSelfOrSuperUser(requestUser, userId);

        // TODO：Deep copy or Shallow copy
        CoreUserEntity userEntity = requestId == userId ? requestUser : userRepository.findById(userId).get();

        utilService.checkUserActive(userEntity);

        boolean isQbnewb = userEntity.isQbnewb();

        userEntity.setQbnewb(!isQbnewb);
        userEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        log.debug("update user info.");

        userRepository.save(userEntity);

        log.debug("Set qpnewb false success.");

        return true;
    }

    public boolean sendInvite(CoreUserEntity invitor, int userId) throws Exception {
        int requestId = invitor.getId();
        log.debug("Invitor {} resend the user invite email for a given use {}", requestId, userId);

        checkUserSlefException(requestId, userId);

        CoreUserEntity user = userRepository.findById(userId).get();

        utilService.checkUserActive(user);

        if (StringUtils.isEmpty(user.getEmail())) {
            log.error("The user {} no have email", userId);
            throw new Exception("The user no have email");
        }

        //Set password reset token
        String resetTokenStr = utilService.resetUserToken(user, true);
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        CoreUserEntity newUser = userRepository.save(user);

        String joinUrl = utilService.getUserJoinUrl(newUser.getId(), resetTokenStr);

        // Send user invitation email
        mailComponent.sendInvitationMail(newUser.getEmail(), invitor.getFirstName(), joinUrl);

        return true;
    }

    @Transactional
    public void deleteUser(CoreUserEntity requestUser, int userId) throws Exception {
        int requestId = requestUser.getId();
        log.debug("delete user, userId: {}", userId);

        checkUserSlefException(requestId, userId);

        sessionRepository.deleteByUserId(userId);

        membershipRepository.deleteByUserId(userId);

        clusterUserMembershipRepository.deleteByUserId(userId);

        userRepository.deleteById(userId);

        log.debug("delete user {} successful.", userId);
    }

    public List<UserInfo> getSpaceUserList(CoreUserEntity user, String queryStr) throws Exception {
        int userId = user.getId();
        long clusterId = user.getClusterId();
        log.debug("User {} get current cluster {} user.", userId, user.getClusterId());

        clusterUserComponent.checkUserClusterAdminPermission(user, clusterId);
        List<Integer> stopLdapUsers = userRepository.getByEmptyEntryUUID();
        // empty hashset can not use in jpa : not in ()
        List<Integer> spaceUserIds = clusterUserMembershipRepository.getUserIdsByClusterId(clusterId);

        spaceUserIds = ListUtil.getAddList(spaceUserIds, stopLdapUsers);

        // Search filtering
        boolean isSearch = !queryStr.isEmpty();

        List<UserInfo> spaceUserInfos = Lists.newArrayList();
        for (Integer spaceUserId : spaceUserIds) {
            if (spaceUserId < 0) {
                continue;
            }
            CoreUserEntity userEntity = userRepository.findById(spaceUserId).get();
            if (isSearch && !userEntity.getFirstName().contains(queryStr)) {
                continue;
            }
            UserInfo userInfo = userEntity.castToUserInfo();
            spaceUserInfos.add(userInfo);
        }

        return spaceUserInfos;
    }

    public void addUserToSpace(CoreUserEntity requestUser, int userId) throws Exception {
        int requestId = requestUser.getId();
        long clusterId = requestUser.getClusterId();
        log.debug("User {} add user {} to current space {}", requestId, userId, clusterId);
        // admin user add self into space
        //checkUserSlefException(requestId, userId);

        ClusterInfoEntity clusterInfoEntity = clusterUserComponent.checkUserClusterAdminPermission(requestUser, clusterId);

        if (clusterUserComponent.userBelongToCluster(userId, clusterId)) {
            log.error("The user {} is already in space {}.", userId, clusterId);
            throw new Exception("The user is already in space");
        }

        // add user to cluster
        clusterUserComponent.addUserToCluster(userId, clusterInfoEntity);
    }

    @Transactional
    public void moveUser(int userId, CoreUserEntity requestUser) throws Exception {
        int requestId = requestUser.getId();
        long clusterId = requestUser.getClusterId();

        log.debug("user {} remove user {} from cluster.", requestId, userId, clusterId);

        checkUserSlefException(requestId, userId);

        clusterUserComponent.checkUserClusterAdminPermission(requestUser, clusterId);

        if (!clusterUserComponent.userBelongToCluster(userId, clusterId)) {
            log.error("The user {} is not in space {}.", userId, clusterId);
            throw new Exception("The user is not in space, can,t be remove.");
        }

        log.debug("Delete user cluster and cluster group membership");
        HashSet<Integer> groupIds = permissionsGroupRoleRepository.getGroupIdByClusterId(clusterId);
        for (int groupId : groupIds) {
            membershipRepository.deleteByUserIdAndGroupId(groupId, userId);
        }
        clusterUserMembershipRepository.deleteByUserIdAndClusterId(userId, clusterId);
    }

    /**
     * Check whether the user is an admin user to view other user information,
     * or the user to view their own information
     * @param requestUser
     * @param userId
     * @throws Exception
     */
    private void checUserSelfOrSuperUser(CoreUserEntity requestUser, int userId) throws Exception {
        log.debug("check request user {} is super or user {} itself.", requestUser.getId(), userId);
        if (!requestUser.isSuperuser() && userId != requestUser.getId()) {
            log.error("The request user {} neither a super user nor the user {} itself.", requestUser.getId(), userId);
            throw new NoPermissionException();
        }
    }

    private void checkEmailDuplicate(String email) throws Exception {
        log.debug("Check email {} is duplicate.", email);
        List<CoreUserEntity> userEntities = userRepository.getByEmail(email);
        if (userEntities != null && userEntities.size() != 0) {
            if (userEntities.stream().anyMatch(user -> user.getEmail().equals(email))) {
                log.error("The email address already in use.");
                throw new UserEmailDuplicatedException();
            }
        }
    }

    private void checkNameDuplicate(String name) throws Exception {
        log.debug("Check name {} is duplicate.", name);
        List<CoreUserEntity> userEntities = userRepository.getByFirstName(name);
        if (userEntities != null && userEntities.size() != 0) {
            // where query ignore letter case in mysql (add binary keyword), query distinguish letter case in h2
            if (userEntities.stream().anyMatch(user -> user.getFirstName().equals(name))) {

                log.error("The name address already in use.");
                throw new UserNameDuplicatedException();
            }

        }
    }

    private boolean isUserClusterAdmin(CoreUserEntity user, ClusterInfoEntity cluster) {
        if (user.isSuperuser()) {
            return true;
        } else {
            List<PermissionsGroupMembershipEntity> permissionsGroupMembershipEntities =
                    membershipRepository.getByUserIdAndGroupId(user.getId(), cluster.getAdminGroupId());
            if (permissionsGroupMembershipEntities.isEmpty()) {
                return false;
            } else {
                return true;
            }
        }
    }

    private void checkUserSlefException(int requestId, int userId) throws Exception {
        if (requestId == userId) {
            log.error("User {} cannot perform this operation on itself", userId);
            throw new UserOperationSelfException();
        }
    }

}
