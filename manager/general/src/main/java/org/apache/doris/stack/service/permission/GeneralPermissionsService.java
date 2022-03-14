package org.apache.doris.stack.service.permission;

import org.apache.doris.stack.dao.ClusterInfoRepository;
import org.apache.doris.stack.dao.ClusterUserMembershipRepository;
import org.apache.doris.stack.model.request.permission.BatchPermissionMembershipReq;
import org.apache.doris.stack.model.request.permission.PermissionGroupAddReq;
import org.apache.doris.stack.model.request.permission.PermissionMembershipReq;
import org.apache.doris.stack.model.request.user.UserGroupRole;
import org.apache.doris.stack.model.request.permission.PermissionsGroupInfo;
import org.apache.doris.stack.model.response.user.GroupMember;
import org.apache.doris.stack.model.response.user.UserGroupMembership;
import org.apache.doris.stack.component.ClusterUserComponent;
import org.apache.doris.stack.component.SettingComponent;
import org.apache.doris.stack.dao.CoreUserRepository;
import org.apache.doris.stack.dao.PermissionsGroupMembershipRepository;
import org.apache.doris.stack.dao.PermissionsGroupRoleRepository;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.PermissionsGroupMembershipEntity;
import org.apache.doris.stack.entity.PermissionsGroupRoleEntity;
import org.apache.doris.stack.exception.NameDuplicatedException;
import org.apache.doris.stack.exception.NoPermissionException;
import org.apache.doris.stack.service.BaseService;
import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.service.UtilService;
import org.apache.doris.stack.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class GeneralPermissionsService extends BaseService {

    @Autowired
    private PermissionsGroupMembershipRepository membershipRepository;

    @Autowired
    private PermissionsGroupRoleRepository groupRoleRepository;

    @Autowired
    private CoreUserRepository userRepository;

    @Autowired
    private ClusterUserComponent clusterUserComponent;

    @Autowired
    private SettingComponent settingComponent;

    @Autowired
    private ClusterInfoRepository clusterInfoRepository;

    @Autowired
    private UtilService utilService;

    @Autowired
    private ClusterUserMembershipRepository clusterUserMembershipRepository;

    /**
     * get all roles info
     * @param user
     * @return
     * @throws Exception
     */
    public List<PermissionsGroupInfo> getAllPermissionGroup(CoreUserEntity user) throws Exception {
        int userId = user.getId();
        long clusterId = clusterUserComponent.getUserCurrentClusterIdAndCheckAdmin(user);

        log.debug("user {} get cluster {} all permissionGroup", userId, clusterId);
        // Get all user permission group IDs in the space
        List<PermissionsGroupRoleEntity> groups = groupRoleRepository.getByClusterId(clusterId);

        List<PermissionsGroupInfo> result = new ArrayList<>();

        for (PermissionsGroupRoleEntity group : groups) {
            int groupId = group.getGroupId();
            PermissionsGroupInfo groupInfo = new PermissionsGroupInfo(groupId, group.getGroupName());
            // filter sync stop ldap user
            List<Integer> users = userRepository.getByEmptyEntryUUID();
            List<Integer> userIds = membershipRepository.getFilterUserIdsByGroupId(groupId);

            userIds = ListUtil.getAddList(userIds, users);
            groupInfo.setMemberCount(userIds.size());
            result.add(groupInfo);
        }

        return result;
    }

    /**
     * get role info by id ,include users
     * @param user
     * @param groupId
     * @return
     * @throws Exception
     */
    public PermissionsGroupInfo getPermissionGroupById(CoreUserEntity user, int groupId) throws Exception {
        int userId = user.getId();
        long clusterId = clusterUserComponent.getUserCurrentClusterIdAndCheckAdmin(user);
        log.debug("user {} get cluster {} all permissionGroup by groupId {} ", userId, clusterId, groupId);

        // get role info,and check whether role belong to current space
        PermissionsGroupRoleEntity groupRoleEntity = checkClusterGroup(clusterId, groupId);

        PermissionsGroupInfo groupInfo = new PermissionsGroupInfo(groupId, groupRoleEntity.getGroupName());

        // get all memberships of role
        List<GroupMember> members = clusterUserComponent.getGroupMembers(groupId);

        groupInfo.setMembers(members);

        return groupInfo;
    }

    /**
     * add a new role
     * @param user
     * @param addReq
     * @return
     * @throws Exception
     */
    @Transactional
    public PermissionsGroupInfo addPermissionGroup(CoreUserEntity user, PermissionGroupAddReq addReq) throws Exception {
        int userId = user.getId();
        checkRequestBody(addReq.hasEmptyField());
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        long clusterId = clusterInfo.getId();

        log.debug("user {} add permission group {} for cluster {}", userId, addReq.getName(), clusterId);
        // check role name duplicated
        checkGroupNameDuplicate(addReq.getName(), clusterId);
        // check role name
        utilService.roleNameCheck(addReq.getName());

        // add role,only can add normal role
        int groupId = clusterUserComponent.addPermissionsGroup(addReq.getName(), clusterId, UserGroupRole.Analyzer);

        log.debug("execute create user sql for group {}", groupId);

        log.debug("add sample data group {} premission.", groupId);

        PermissionsGroupInfo groupInfo = new PermissionsGroupInfo(groupId, addReq.getName());

        return groupInfo;
    }

    /**
     * update role name
     * @param user
     * @param groupId
     * @param updateReq
     * @return
     * @throws Exception
     */
    @Transactional
    public PermissionsGroupInfo updatePermissionGroup(CoreUserEntity user, int groupId,
                                                      PermissionGroupAddReq updateReq) throws Exception {
        checkRequestBody(updateReq.hasEmptyField());
        int userId = user.getId();
        log.debug("user {} update group {}.", userId, groupId);
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        long clusterId = clusterInfo.getId();
        // check if role is admin or all user
        if (groupId == clusterInfo.getAdminGroupId() || groupId == clusterInfo.getAllUserGroupId()) {
            log.error("The group is admin group or all user group.");
            throw new NoPermissionException();
        }

        // get role info,and check role belong to current space
        PermissionsGroupRoleEntity groupRoleEntity = checkClusterGroup(clusterId, groupId);
        String name = updateReq.getName().trim();
        if (name.equals(groupRoleEntity.getGroupName())) {
            log.warn("The permission group name has not changed");
            return new PermissionsGroupInfo(groupId, name);
        }
        // check role name duplicated
        checkGroupNameDuplicate(name, clusterId);
        utilService.roleNameCheck(name);

        // update role name
        groupRoleEntity.setGroupName(name);
        groupRoleRepository.save(groupRoleEntity);
        log.debug("update group {} name {}.", groupId, name);

        return new PermissionsGroupInfo(groupId, name);
    }

    /**
     * delete role
     * @param user
     * @param groupId
     * @throws Exception
     */
    @Transactional
    public void deletePermissionsGroupById(CoreUserEntity user, int groupId) throws Exception {
        int userId = user.getId();
        log.debug("user {} delete group {}.", userId, groupId);

        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);

        // check if role is admin or all user
        if (groupId == clusterInfo.getAdminGroupId() || groupId == clusterInfo.getAllUserGroupId()) {
            log.error("The group is admin group or all user group.");
            throw new NoPermissionException();
        }

        log.debug("delete memberships by group {}.", groupId);
        // delete memberships
        membershipRepository.deleteByGroupId(groupId);

        log.debug("delete group {}.", groupId);
        // delete role
        groupRoleRepository.deleteById(groupId);

    }

    /**
     * get all role memberships of space
     * @param user
     * @return
     * @throws Exception
     */
    public Map<Integer, List<UserGroupMembership>> getAllMemberships(CoreUserEntity user) throws Exception {
        long clusterId = clusterUserComponent.getUserCurrentClusterIdAndCheckAdmin(user);
        // get all roles of current space
        log.debug("get cluster all group.");
        Set<Integer> groupsId = groupRoleRepository.getGroupIdByClusterId(clusterId);

        // get all users
        log.debug("get cluster {} all user.", clusterId);

        List<Integer> stopLdapUsers = userRepository.getByEmptyEntryUUID();
        List<Integer> clusterUserIds = clusterUserMembershipRepository.getUserIdsByClusterId(clusterId);
        clusterUserIds = ListUtil.getAddList(clusterUserIds, stopLdapUsers);

        Map<Integer, List<UserGroupMembership>> result = new HashMap<>();

        log.debug("get user and group membership by cluster.");
        // get user info
        for (Integer userId : clusterUserIds) {
            List<UserGroupMembership> groupMemberships = new ArrayList<>();

            List<PermissionsGroupMembershipEntity> membershipEntities = membershipRepository.getByUserId(userId);
            for (PermissionsGroupMembershipEntity membershipEntity : membershipEntities) {
                if (groupsId.contains(membershipEntity.getGroupId())) {
                    groupMemberships.add(membershipEntity.castToModel());
                }
            }

            if (!groupMemberships.isEmpty()) {
                result.put(userId, groupMemberships);
            }
        }
        return result;
    }

    /**
     * add user to role,and return all memberships
     * @param user
     * @param req
     * @return
     * @throws Exception
     */
    @Transactional
    public List<GroupMember> addMembership(CoreUserEntity user, PermissionMembershipReq req) throws Exception {
        int userId = user.getId();
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        long clusterId = clusterInfo.getId();
        // check role belong to current cluster
        checkClusterGroup(clusterId, req.getGroupId());
        return addUserMembership(req.getUserId(), req.getGroupId(), clusterInfo);
    }

    private List<GroupMember> addUserMembership(int addUserId, int groupId, ClusterInfoEntity clusterInfo) throws Exception {

        log.debug("add membership for cluster {} user {}.", clusterInfo.getId(), addUserId);

        // check the added user belong to current space
        clusterUserComponent.checkUserBelongToCluster(addUserId, clusterInfo.getId());

        // add role memberships
        clusterUserComponent.addGroupUserMembership(addUserId, groupId);

        log.debug("add membership success.");

        // if is admin group, change user as admin user
        if (groupId == clusterInfo.getAdminGroupId()) {
            log.debug("Add user to admin group, update user to admin user.");
            CoreUserEntity userEntity = userRepository.findById(addUserId).get();
            if (userEntity.getClusterId() == clusterInfo.getId()) {
                userEntity.setIsClusterAdmin(true);
                userEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                userRepository.save(userEntity);
                log.debug("set user space admin user success.");
            }
        }

        return clusterUserComponent.getGroupMembers(groupId);
    }

    /**
     * batch add user to role
     * @param user
     * @param req
     * @return
     * @throws Exception
     */
    @Transactional
    public List<GroupMember> batchAddMembership(CoreUserEntity user,
                                                BatchPermissionMembershipReq req) throws Exception {
        int userId = user.getId();
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        long clusterId = clusterInfo.getId();
        // check role belong to space
        checkClusterGroup(clusterId, req.getGroupId());
        for (int userAddId : req.getUserIds()) {

            addUserMembership(userAddId, req.getGroupId(), clusterInfo);
        }

        return clusterUserComponent.getGroupMembers(req.getGroupId());
    }

    /**
     * delete user from group
     * @param user
     * @param membershipId
     * @throws Exception
     */
    @Transactional
    public void deleteMembership(CoreUserEntity user, int membershipId) throws Exception {
        int userId = user.getId();
        ClusterInfoEntity clusterInfo = clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
        long clusterId = clusterInfo.getId();
        log.debug("user {} delete membership {} in cluster {}", userId, membershipId, clusterId);

        // check Membership exists
        log.debug("Check whether the membership exists");
        Optional<PermissionsGroupMembershipEntity> membershipOp = membershipRepository.findById(membershipId);
        if (membershipOp.equals(Optional.empty())) {
            log.error("The membership {} not exist.", membershipId);
            throw new NoPermissionException();
        }

        PermissionsGroupMembershipEntity membershipEntity = membershipOp.get();

        // check role is all user
        if (membershipEntity.getGroupId() == clusterInfo.getAllUserGroupId()) {
            log.error("The group is all user group {}", clusterInfo.getAllUserGroupId());
            throw new NoPermissionException();
        }

        // check group belong to space
        checkClusterGroup(clusterId, membershipEntity.getGroupId());

        log.debug("delete membership");
        membershipRepository.deleteById(membershipId);

        // if deleted admin role,change user as non admin
        if (membershipEntity.getGroupId() == clusterInfo.getAdminGroupId()) {
            log.debug("Delete user from admin group, update user to not space admin user.");
            CoreUserEntity userEntity = userRepository.findById(membershipEntity.getUserId()).get();
            // if user in current space,delete user admin privilege
            if (userEntity.getClusterId() == clusterId) {
                userEntity.setIsClusterAdmin(false);
                userEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                userRepository.save(userEntity);
                log.debug("set user not space admin user success.");
            }
        }
    }

    /**
     * check group belong to space
     * @param clusterId
     * @param groupId
     * @throws Exception
     */
    public PermissionsGroupRoleEntity checkClusterGroup(long clusterId, int groupId) throws Exception {
        log.debug("Check the group {} belongs to the cluster {}.", groupId, clusterId);
        Optional<PermissionsGroupRoleEntity> groupOp = groupRoleRepository.findById(groupId);
        if (groupOp.equals(Optional.empty())) {
            log.debug("The group {} is not exist.", groupId);
            throw new NoPermissionException();
        }
        PermissionsGroupRoleEntity group = groupOp.get();
        if (group.getClusterId() != clusterId) {
            log.debug("The group {} not belong to cluster {}.", groupId, clusterId);
            throw new NoPermissionException();
        }
        return group;
    }

    public void checkGroupNameDuplicate(String name, long clusterId) throws Exception {
        log.debug("check group name {} is exist in cluster {}.", name, clusterId);
        List<PermissionsGroupRoleEntity> groupRoleEntities =
                groupRoleRepository.getByGroupNameAndClusterId(name, clusterId);

        if (groupRoleEntities != null && !groupRoleEntities.isEmpty()) {
            log.error("The group name {} is exist in cluster {}.", name, clusterId);
            throw new NameDuplicatedException();
        }
    }

}

