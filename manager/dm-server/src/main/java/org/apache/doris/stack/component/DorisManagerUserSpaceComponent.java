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

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.connector.PaloLoginClient;
import org.apache.doris.stack.connector.PaloQueryClient;
import org.apache.doris.stack.constant.ConstantDef;
import org.apache.doris.stack.control.ModelControlLevel;
import org.apache.doris.stack.control.ModelControlRequestType;
import org.apache.doris.stack.control.manager.DorisClusterManager;
import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.ClusterUserMembershipRepository;
import org.apache.doris.stack.dao.CoreUserRepository;
import org.apache.doris.stack.dao.ModelControlRequestRepository;
import org.apache.doris.stack.dao.PermissionsGroupMembershipRepository;
import org.apache.doris.stack.dao.PermissionsGroupRoleRepository;
import org.apache.doris.stack.driver.JdbcSampleClient;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.ClusterUserMembershipEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.ModelControlRequestEntity;
import org.apache.doris.stack.entity.PermissionsGroupRoleEntity;
import org.apache.doris.stack.entity.SettingEntity;
import org.apache.doris.stack.exception.DorisConnectionException;
import org.apache.doris.stack.exception.DorisHttpPortErrorException;
import org.apache.doris.stack.exception.DorisIpErrorException;
import org.apache.doris.stack.exception.DorisJdbcPortErrorException;
import org.apache.doris.stack.exception.DorisSpaceDuplicatedException;
import org.apache.doris.stack.exception.DorisUerOrPassErrorException;
import org.apache.doris.stack.exception.DorisUserNoPermissionException;
import org.apache.doris.stack.exception.NameDuplicatedException;
import org.apache.doris.stack.exception.NoAdminPermissionException;
import org.apache.doris.stack.exception.RequestFieldNullException;
import org.apache.doris.stack.exception.StudioNotInitException;
import org.apache.doris.stack.exception.UserNotExistException;
import org.apache.doris.stack.model.request.space.ClusterCreateReq;
import org.apache.doris.stack.model.request.space.NewUserSpaceCreateReq;
import org.apache.doris.stack.model.request.user.UserGroupRole;
import org.apache.doris.stack.model.response.space.NewUserSpaceInfo;
import org.apache.doris.stack.service.BaseService;
import org.apache.doris.stack.service.config.ConfigConstant;
import org.apache.doris.stack.service.construct.MetadataService;
import org.apache.doris.stack.util.ListUtil;
import org.apache.doris.stack.util.UuidUtil;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @Description：doris Manager Create user Doris cluster space when servicing
 */
@Component
@Slf4j
public class DorisManagerUserSpaceComponent extends BaseService {

    public static final String ADMIN_USER_NAME = "Administrators_";

    public static final String ALL_USER_NAME = "All Users_";

    private static final String PALO_ANALYZER_USER_NAME = "Analyzer";

    @Autowired
    private CoreUserRepository userRepository;

    @Autowired
    private PermissionsGroupRoleRepository groupRoleRepository;

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Autowired
    private PaloLoginClient paloLoginClient;

    @Autowired
    private ClusterUserComponent clusterUserComponent;

    @Autowired
    private SettingComponent settingComponent;

    @Autowired
    private PermissionsGroupMembershipRepository membershipRepository;

    @Autowired
    private MetadataService managerMetadataService;

    @Autowired
    private ManagerMetaSyncComponent managerMetaSyncComponent;

    @Autowired
    private JdbcSampleClient jdbcClient;

    @Autowired
    private PaloQueryClient queryClient;

    @Autowired
    private ModelControlRequestRepository requestRepository;

    @Autowired
    private ClusterUserMembershipRepository clusterUserMembershipRepository;

    // TODO:It will be removed later
    @Autowired
    private DorisClusterManager clusterManager;

    public long create(NewUserSpaceCreateReq createReq, String userName) throws Exception {
        log.debug("Super user create palo user space.");
        // Verify whether the initialization of the space authentication method is completed
        SettingEntity enabled = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        if (enabled == null || StringUtils.isEmpty(enabled.getValue())) {
            log.debug("The auth type not be inited.");
            throw new StudioNotInitException();
        }

        checkRequestBody(createReq.hasEmptyFieldNoCluster());

        // check space name
        nameCheck(createReq.getName());

        checkAdminUserList(createReq.getSpaceAdminUsers());

        // create space information
        ClusterInfoEntity clusterInfoInit = null;

        if (createReq.getCluster() != null) {
            log.debug("Add doris cluster info.");
            clusterInfoInit = validateCluster(createReq.getCluster());
        } else {
            log.debug("Skip add palo cluster info.");
            clusterInfoInit = new ClusterInfoEntity();
        }
        clusterInfoInit.setName(createReq.getName());
        clusterInfoInit.setDescription(createReq.getDescribe());
        clusterInfoInit.setCreateTime(new Timestamp(System.currentTimeMillis()));
        clusterInfoInit.setCreator(userName);

        ClusterInfoEntity clusterInfoSave = clusterInfoRepository.save(clusterInfoInit);

        // Initialize the permission group management information of the space
        initSpaceAdminPermisssion(createReq.getSpaceAdminUsers(), clusterInfoSave);

        // Initialize the correspondence between permission group and Doris virtual user
        if (createReq.getCluster() != null) {
            initGroupPaloUser(clusterInfoSave);
        }

        // Synchronize Doris metadata
        managerMetadataService.syncMetadataByCluster(clusterInfoSave);

        // Send invitation mail
        String joinUrl = "";
//        mailComponent.sendInvitationMail(createReq.getUser().getEmail(),
//                AuthenticationService.SUPER_USER_NAME_VALUE, joinUrl);

        return clusterInfoSave.getId();
    }

    public ClusterInfoEntity validateCluster(ClusterCreateReq createReq) throws Exception {
        log.debug("validate palo cluster info.");
        checkRequestBody(createReq.hasEmptyField());
        log.info("Verify that the Palo cluster already exists.");
        List<ClusterInfoEntity> exsitEntities =
                clusterInfoRepository.getByAddressAndPort(createReq.getAddress(), createReq.getHttpPort());
        if (exsitEntities != null && exsitEntities.size() != 0) {
            log.error("The palo cluster {} is already associated with space.", createReq.getAddress() + ":"
                    + createReq.getHttpPort());
            throw new DorisSpaceDuplicatedException();
        }

        log.info("Verify that the Palo cluster is available");
        ClusterInfoEntity entity = new ClusterInfoEntity();
        entity.updateByClusterInfo(createReq);
        // Just verify whether the Doris HTTP interface can be accessed
        try {
            paloLoginClient.loginPalo(entity);
        } catch (Exception e) {
            log.error("Doris cluster http access error.");
            if (e.getMessage().contains("nodename nor servname provided, or not known")) {
                throw new DorisIpErrorException();
            } else if (e.getMessage().contains("failed: Connection refused (Connection refused)")) {
                throw new DorisHttpPortErrorException();
            } else if (e.getMessage().contains("Login palo error:Access denied for default_cluster")) {
                throw new DorisUserNoPermissionException();
            } else if (e.getMessage().contains("Login palo error:Access denied")) {
                throw new DorisUerOrPassErrorException();
            }
            throw new DorisConnectionException(e.getMessage());
        }

        // Just verify whether the Doris JDBC protocol can be accessed
        try {
            jdbcClient.testConnetion(createReq.getAddress(), createReq.getQueryPort(),
                    ConstantDef.MYSQL_DEFAULT_SCHEMA, createReq.getUser(), createReq.getPasswd());
            log.debug("Doris cluster jdbc access success.");
        } catch (Exception e) {
            log.error("Doris cluster jdbc access error.");
            throw new DorisJdbcPortErrorException();
        }

        // The manager function is enabled by default
        entity.setManagerEnable(true);
        return entity;
    }

    /**
     * Change cluster space information
     * 1. Only modify the space name and other information;
     * 2. Add new cluster connection information;
     * TODO: Currently, modifying cluster information is not supported
     * @param user
     * @param spaceId
     * @param updateReq
     * @return
     * @throws Exception
     */
    public NewUserSpaceInfo update(CoreUserEntity user, long spaceId, NewUserSpaceCreateReq updateReq) throws Exception {
        checkRequestBody(updateReq.hasEmptyFieldNoCluster());

        log.debug("update space {} information.", spaceId);

        ClusterInfoEntity clusterInfo = clusterUserComponent.checkUserClusterAdminPermission(user, spaceId);

        if (!clusterInfo.getName().equals(updateReq.getName())) {
            nameCheck(updateReq.getName());
        }
        log.debug("The name not be changed");
        clusterInfo.setName(updateReq.getName());
        clusterInfo.setDescription(updateReq.getDescribe());
        clusterInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        log.debug("update space info.");

        // Determine whether the cluster information needs to be changed
        if (updateReq.getCluster() != null) {
            // TODO: if the user name and password of Doris cluster already exist, they cannot be modified
            if (clusterInfo.getAddress() != null) {
                log.error("The space palo cluster information already exists.");
                throw new DorisSpaceDuplicatedException();
            }
            clusterAccess(updateReq.getCluster(), clusterInfo);
        }

        // admin user update admin userList
        if (user.isSuperuser()) {
            checkAdminUserList(updateReq.getSpaceAdminUsers());
            log.debug("Admin user update cluster admin user list.");
            // Get the original space administrator list
            List<Integer> oldAdminUsers = membershipRepository.getUserIdsByGroupId(clusterInfo.getAdminGroupId());
            log.debug("Old admin users {}", oldAdminUsers);

            log.debug("New admin users {}", updateReq.getSpaceAdminUsers());

            // Get the new administrator role user in the space
            List<Integer> addAdminUsers = ListUtil.getAddList(updateReq.getSpaceAdminUsers(), oldAdminUsers);
            log.debug("Add admin users {}", addAdminUsers);
            for (Integer adminUserId : addAdminUsers) {
                if (adminUserId < 0) {
                    continue;
                }
                // Judge whether the user is in the current space
                List<ClusterUserMembershipEntity> membershipEntities =
                        clusterUserMembershipRepository.getByUserIdAndClusterId(adminUserId, spaceId);
                if (membershipEntities.isEmpty()) {
                    // Users are not in the space yet. They are added to the space
                    // and also to the administrator and alluser user groups
                    clusterUserComponent.addAdminUserToCluster(adminUserId, clusterInfo);
                } else {
                    // The user is already in the space, but not an administrator,
                    // and is added to the administrator group
                    clusterUserComponent.addGroupUserMembership(adminUserId, clusterInfo.getAdminGroupId());
                }
            }

            // Get the administrator role user for space deletion
            List<Integer> reduceAdminUsers = ListUtil.getReduceList(updateReq.getSpaceAdminUsers(), oldAdminUsers);
            log.debug("Reduce admin users {}", reduceAdminUsers);
            for (Integer adminUserId : reduceAdminUsers) {
                if (adminUserId < 0) {
                    continue;
                }
                // Delete the user from the space administrator role
                membershipRepository.deleteByUserIdAndGroupId(clusterInfo.getAdminGroupId(), adminUserId);

                // also delete the user form all user role and cluster space
                membershipRepository.deleteByUserIdAndGroupId(clusterInfo.getAllUserGroupId(), adminUserId);
                clusterUserMembershipRepository.deleteByUserIdAndClusterId(adminUserId, clusterInfo.getId());
            }

        }
        ClusterInfoEntity clusterInfoSave = clusterInfoRepository.save(clusterInfo);
        NewUserSpaceInfo userSpaceInfo = clusterInfoSave.transToNewModel();
        getAdminGroupUserList(userSpaceInfo, clusterInfo.getAdminGroupId());
        return userSpaceInfo;
    }

    public void clusterAccess(ClusterCreateReq clusterAccessInfo, ClusterInfoEntity clusterInfo) throws Exception {
        checkRequestBody(clusterAccessInfo.hasEmptyField());

        String newAddress = clusterAccessInfo.getAddress();
        String oldAddress = clusterInfo.getAddress();

        if (oldAddress == null) {
            log.debug("First access to doris cluster information in space");
        } else if (newAddress.equals(oldAddress)) {
            log.debug("The space has been connected to the cluster information, "
                    + "and the cluster information has not changed");
            return;
        } else {
            log.debug("The space has been connected to the cluster information, "
                    + "and the cluster information has changed");
            deleteClusterPermissionInfo(clusterInfo);
        }

        updateAccessInfo(clusterAccessInfo, clusterInfo);
    }

    private void updateAccessInfo(ClusterCreateReq clusterAccessInfo,
                                  ClusterInfoEntity clusterInfo) throws Exception {
        validateCluster(clusterAccessInfo);

        clusterInfo.updateByClusterInfo(clusterAccessInfo);
        clusterInfo.setStatus(ClusterInfoEntity.AppClusterStatus.NORMAL.name());

        // Initialize the correspondence between permission group and Doris virtual user
        initGroupPaloUser(clusterInfo);

        // Synchronize Doris metadata
        managerMetadataService.syncMetadataByCluster(clusterInfo);
        clusterInfoRepository.save(clusterInfo);
    }

    public boolean nameCheck(String name) throws Exception {
        log.debug("Check cluster name {} is duplicate.", name);
        List<ClusterInfoEntity> clusterInfos = clusterInfoRepository.getByName(name);
        if (clusterInfos != null && clusterInfos.size() != 0) {
            log.error("The space name {} already exists.", name);
            throw new NameDuplicatedException();
        }
        return true;
    }

    /**
     * Get the list of spaces for which the user has permission
     * @param userEntity
     * @return
     */
    public List<NewUserSpaceInfo> getAllSpaceByUser(CoreUserEntity userEntity) {
        int userId = userEntity.getId();
        log.debug("User {} get space list", userId);

        if (userEntity.isSuperuser()) {
            log.debug("Admin user get all space list.");
            List<ClusterInfoEntity> clusterInfos = clusterInfoRepository.findAll(Sort.by("id").ascending());
            List<NewUserSpaceInfo> spaceInfos = Lists.newArrayList();
            for (ClusterInfoEntity clusterInfo : clusterInfos) {

                NewUserSpaceInfo spaceInfo = clusterInfo.transToNewModel();
                getClusterRequestInfo(spaceInfo, clusterInfo.getId());
                getAdminGroupUserList(spaceInfo, clusterInfo.getAdminGroupId());
                spaceInfos.add(spaceInfo);
            }
            return spaceInfos;
        } else {
            log.debug("user get all space list.");
            List<ClusterUserMembershipEntity> userMembershipEntities = clusterUserMembershipRepository.getByUserId(userId);
            List<NewUserSpaceInfo> spaceInfos = Lists.newArrayList();
            for (ClusterUserMembershipEntity clusterUserMembershipEntity : userMembershipEntities) {
                long clusterId = clusterUserMembershipEntity.getClusterId();
                ClusterInfoEntity clusterInfoEntity = clusterInfoRepository.findById(clusterId).get();
                NewUserSpaceInfo spaceInfo = clusterInfoEntity.transToNewModel();
                getClusterRequestInfo(spaceInfo, clusterInfoEntity.getId());
                if (!spaceInfo.isRequestCompleted()) {
                    continue;
                }
                spaceInfos.add(spaceInfo);
            }
            return spaceInfos;
        }
    }

    public NewUserSpaceInfo getById(CoreUserEntity user, int spaceId) throws Exception {
        log.debug("User {} get space {} info.", user.getId(), spaceId);
        ClusterInfoEntity clusterInfo = clusterUserComponent.checkUserClusterAdminPermission(user, spaceId);
        setClusterStatus(clusterInfo);

        NewUserSpaceInfo result = clusterInfo.transToNewModel();
        getClusterRequestInfo(result, spaceId);

        if (!result.isRequestCompleted() && !user.isSuperuser()) {
            log.error("Ordinary users do not have permission to view unfinished space");
            throw new NoAdminPermissionException();
        }

        getAdminGroupUserList(result, clusterInfo.getAdminGroupId());

        return result;
    }

    private void setClusterStatus(ClusterInfoEntity clusterInfo) {
        try {
            jdbcClient.testConnetion(clusterInfo.getAddress(), clusterInfo.getQueryPort(),
                    ConstantDef.MYSQL_DEFAULT_SCHEMA, clusterInfo.getUser(), clusterInfo.getPasswd());
            clusterInfo.setStatus(ClusterInfoEntity.AppClusterStatus.NORMAL.name());
        } catch (Exception e) {
            clusterInfo.setStatus(ClusterInfoEntity.AppClusterStatus.ABNORMAL.name());
        }
    }

    /**
     * Delete a space's information
     * 1. Information of space itself
     * 2. Space permissions and user group information
     * 3. User  of space
     *
     * @param spaceId
     * @throws Exception
     */
    public void deleteSpace(long spaceId) throws Exception {
        ClusterInfoEntity clusterInfo = clusterInfoRepository.findById(spaceId).get();

        // delete cluster information
        clusterInfoRepository.deleteById(spaceId);

        try {
            // delete cluster configuration
            log.debug("delete cluster {} config infos.", spaceId);
            settingComponent.deleteAdminSetting(spaceId);

            deleteClusterPermissionInfo(clusterInfo);

            // delete user information
            log.debug("delete cluster {} all user membership.", spaceId);
            clusterUserMembershipRepository.deleteByClusterId(spaceId);

            // TODO: In order to be compatible with the deleted content of spatial information before, it is put here.
            //  If the interface that releases both cluster and physical resources is implemented later,
            //  it will be unified in the current doriscluster processing operation
            clusterManager.deleteClusterOperation(clusterInfo);
        } catch (Exception e) {
            log.warn("delete space {} related information failed", spaceId, e);
        }
    }

    private void deleteClusterPermissionInfo(ClusterInfoEntity clusterInfo) throws Exception {
        long spaceId = clusterInfo.getId();
        log.debug("delete cluster {} data permission.", spaceId);
        // Delete the doris jdbc agent account
        PermissionsGroupRoleEntity allUserGroup = groupRoleRepository.findById(clusterInfo.getAllUserGroupId()).get();

        // Get all user groups in the cluster
        HashSet<Integer> groupIds = groupRoleRepository.getGroupIdByClusterId(spaceId);

        // Delete permission user group and delete the mapping relationship between user and group
        log.debug("delete cluster {} all permission group and group user membership.", spaceId);
        for (int groupId : groupIds) {
            groupRoleRepository.deleteById(groupId);
            membershipRepository.deleteByGroupId(groupId);
        }

        // Only after the cluster accesses the access information
        if (clusterInfo.getAddress() != null && !clusterInfo.getAddress().isEmpty()) {
            log.debug("delete cluster {} manager metadata and data permission.", spaceId);
            managerMetaSyncComponent.deleteClusterMetadata(clusterInfo);

            log.debug("Delete cluster {} analyzer user {}.", spaceId, allUserGroup.getPaloUserName());
            queryClient.deleteUser(ConstantDef.DORIS_DEFAULT_NS, ConstantDef.MYSQL_DEFAULT_SCHEMA, clusterInfo, allUserGroup.getPaloUserName());
        }

        // After deleting the user's space, set clusterid to 0
        List<CoreUserEntity> users = userRepository.getByClusterId(spaceId);
        for (CoreUserEntity user : users) {
            user.setClusterId(0L);
            userRepository.save(user);
        }
    }

    private void getAdminGroupUserList(NewUserSpaceInfo userSpaceInfo, int adminGroup) {
        List<Integer> adminUsers = membershipRepository.getUserIdsByGroupId(adminGroup);
        List<NewUserSpaceInfo.SpaceAdminUserInfo> adminUserInfos = Lists.newArrayList();
        List<Integer> idList = Lists.newArrayList();
        for (Integer userId : adminUsers) {
            if (userId < 0) {
                continue;
            }
            CoreUserEntity userEntity = userRepository.findById(userId).get();
            adminUserInfos.add(userEntity.castToAdminUserInfo());
            idList.add(userId);
        }

        userSpaceInfo.setSpaceAdminUser(adminUserInfos);
        userSpaceInfo.setSpaceAdminUserId(idList);
    }

    // Get the creation or takeover request of cluster space
    private void getClusterRequestInfo(NewUserSpaceInfo spaceInfo, long clusterId) {
        List<ModelControlRequestEntity> requestEntities =
                requestRepository.getByModelLevelAndIdAndCompleted(ModelControlLevel.DORIS_CLUSTER,
                        clusterId, false);
        if (requestEntities.isEmpty()) {
            spaceInfo.setRequestCompleted(true);
        } else {
            ModelControlRequestEntity requestEntity = requestEntities.get(0);
            // TODO: Get the creation or takeover request of cluster space
            if (ModelControlRequestType.CREATION.equals(requestEntity.getRequestType())
                    || ModelControlRequestType.TAKE_OVER.equals(requestEntity.getRequestType())) {
                spaceInfo.setRequestCompleted(false);
                spaceInfo.setRequestId(requestEntity.getId());
                spaceInfo.setRequestInfo(JSON.parse(requestEntity.getRequestInfo()));
                spaceInfo.setEventType(requestEntity.getCurrentEventType());
            } else {
                spaceInfo.setRequestCompleted(true);
            }
        }
    }

    private void checkAdminUserList(List<Integer> adminUsers) throws Exception {
        // If the user list does not exist, an error is returned
        if (adminUsers == null || adminUsers.isEmpty()) {
            log.error("The admin user list empty.");
            throw new RequestFieldNullException();
        }

        // check user exist
        for (Integer user : adminUsers) {
            if (userRepository.findById(user).equals(Optional.empty())) {
                log.error("The admin user {} not exist.", user);
                throw new UserNotExistException();
            }
        }
    }

    /**
     * Initialize the Palo cluster user and password corresponding to the space user group
     *
     * @param clusterInfo
     */
    private void initGroupPaloUser(ClusterInfoEntity clusterInfo) throws Exception {
        log.debug("Palo cluster validate，init group palo user.");

        // The administrator group directly initializes the admin privileged
        PermissionsGroupRoleEntity adminGroup = groupRoleRepository.findById(clusterInfo.getAdminGroupId()).get();
        adminGroup.setPaloUserName(clusterInfo.getUser());
        adminGroup.setPassword(clusterInfo.getPasswd());
        groupRoleRepository.save(adminGroup);

        // create select user in Doris for manager jdbc access
        PermissionsGroupRoleEntity allUserGroup = groupRoleRepository.findById(clusterInfo.getAllUserGroupId()).get();

        String userName = PALO_ANALYZER_USER_NAME + UuidUtil.newUuidString();
        String password = queryClient.createUser(ConstantDef.DORIS_DEFAULT_NS, ConstantDef.MYSQL_DEFAULT_SCHEMA,
                clusterInfo, userName);
        allUserGroup.setPaloUserName(userName);
        allUserGroup.setPassword(password);

        groupRoleRepository.save(allUserGroup);
        log.debug("save palo user for group");
    }

    /**
     * Initialize permission groups in space
     *
     * @param userIds
     * @param clusterInfoEntity
     */
    private void initSpaceAdminPermisssion(List<Integer> userIds, ClusterInfoEntity clusterInfoEntity) {
        long clusterId = clusterInfoEntity.getId();
        log.debug("Init palo user cluster {} admin user {} permissions.", clusterId, userIds);

        // Each space needs to create two user groups, admin and all user
        // Create space admin group
        int adminGroupId = clusterUserComponent.addPermissionsGroup(ADMIN_USER_NAME + clusterId,
                clusterId, UserGroupRole.Administrator);
        log.debug("Init palo user space {} admin group is {}.", clusterId, adminGroupId);

        // Create space all user group
        int allUserGroupId = clusterUserComponent.addPermissionsGroup(ALL_USER_NAME + clusterId,
                clusterId, UserGroupRole.Analyzer);
        log.debug("Init palo user space {} all user group is {}.", clusterId, allUserGroupId);

        clusterInfoEntity.setAdminGroupId(adminGroupId);
        clusterInfoEntity.setAllUserGroupId(allUserGroupId);

        clusterInfoRepository.save(clusterInfoEntity);

        // Initialize the relationship between permission groups and users in the space
        for (Integer userId : userIds) {
            clusterUserComponent.addAdminUserToCluster(userId, clusterInfoEntity);
        }

        log.debug("Save palo user space user and group.");
    }
}
