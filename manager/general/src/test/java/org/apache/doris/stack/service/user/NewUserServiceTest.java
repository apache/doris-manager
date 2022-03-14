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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import lombok.extern.slf4j.Slf4j;
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
import org.apache.doris.stack.entity.SettingEntity;
import org.apache.doris.stack.exception.NoPermissionException;
import org.apache.doris.stack.exception.RequestFieldNullException;
import org.apache.doris.stack.exception.UserEmailDuplicatedException;
import org.apache.doris.stack.exception.UserNameDuplicatedException;
import org.apache.doris.stack.model.request.config.InitStudioReq;
import org.apache.doris.stack.model.request.user.NewUserAddReq;
import org.apache.doris.stack.model.response.user.UserInfo;
import org.apache.doris.stack.service.UtilService;
import org.apache.doris.stack.service.config.ConfigConstant;
import org.apache.doris.stack.util.DeployType;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Optional;

@RunWith(JUnit4.class)
@Slf4j
public class NewUserServiceTest {

    @InjectMocks
    private NewUserService userService;

    @Mock
    private CoreSessionRepository sessionRepository;

    @Mock
    private CoreUserRepository userRepository;

    @Mock
    private PermissionsGroupMembershipRepository membershipRepository;

    @Mock
    private UtilService utilService;

    @Mock
    private MailComponent mailComponent;

    @Mock
    private ClusterUserComponent clusterUserComponent;

    @Mock
    private LdapComponent ldapComponent;

    @Mock
    private SettingComponent settingComponent;

    @Mock
    private PermissionsGroupRoleRepository permissionsGroupRoleRepository;

    @Mock
    private ClusterInfoRepository clusterInfoRepository;

    @Mock
    private Environment environment;

    @Mock
    private ClusterUserMembershipRepository clusterUserMembershipRepository;

    @Mock
    private UserActivityComponent activityComponent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllUser() {
        log.debug("test get all user.");
        int userId = 1;
        long clusterId = 2;

        int userId1 = 2;
        int userId2 = 3;

        CoreUserEntity requestUser = new CoreUserEntity();
        requestUser.setId(userId);
        requestUser.setClusterId(clusterId);
        requestUser.setFirstName("test1");
        requestUser.setActive(true);
        when(clusterUserComponent.userBelongToCluster(userId, clusterId)).thenReturn(true);

        CoreUserEntity user1 = new CoreUserEntity();
        user1.setId(userId1);
        user1.setFirstName("name1");
        user1.setActive(true);
        when(clusterUserComponent.userBelongToCluster(userId1, clusterId)).thenReturn(false);

        CoreUserEntity user2 = new CoreUserEntity();
        user2.setId(userId2);
        user2.setFirstName("test2");
        user2.setActive(false);
        when(clusterUserComponent.userBelongToCluster(userId2, clusterId)).thenReturn(false);

        try {
            // include stopped user, query string is empty
            boolean includeDeactivated = true;
            String queryStr = "";
            when(clusterUserComponent.checkUserClusterAdminPermission(requestUser, clusterId)).thenReturn(new ClusterInfoEntity());
            List<CoreUserEntity> allUsers = Lists.newArrayList(requestUser, user1, user2);

            when(userRepository.findAll()).thenReturn(allUsers);

            List<UserInfo> allUserReuslt = userService.getAllUser(requestUser, includeDeactivated, queryStr);
            Assert.assertEquals(allUserReuslt.size(), 3);

            // include stopped user, query string is 'te'
            queryStr = "te";
            allUserReuslt = userService.getAllUser(requestUser, includeDeactivated, queryStr);
            Assert.assertEquals(allUserReuslt.size(), 2);

            // not include stopped user, query string is 'te'
            includeDeactivated = false;
            List<CoreUserEntity> activeUsers = Lists.newArrayList(requestUser, user1);
            when(userRepository.getByActive(true)).thenReturn(activeUsers);
            allUserReuslt = userService.getAllUser(requestUser, includeDeactivated, queryStr);
            Assert.assertEquals(allUserReuslt.size(), 1);

        } catch (Exception e) {
            log.error("Test get all user error");
            e.printStackTrace();
        }

    }

    @Test
    public void testGetUserById() {
        int userId = 1;
        long clusterId = 2;

        CoreUserEntity requestUser = new CoreUserEntity();
        requestUser.setId(userId);
        requestUser.setClusterId(clusterId);
        requestUser.setFirstName("test1");
        requestUser.setActive(true);

        try {
            // get user's own information
            UserInfo result = userService.getUserById(userId, requestUser);
            Assert.assertEquals(result.getName(), "test1");

            // super admin get other user information
            requestUser.setSuperuser(true);
            int getUserId = 3;
            CoreUserEntity userEntity = new CoreUserEntity();
            userEntity.setId(getUserId);
            userEntity.setFirstName("name1");
            when(userRepository.findById(getUserId)).thenReturn(Optional.of(userEntity));
            result = userService.getUserById(getUserId, requestUser);
            Assert.assertEquals(result.getName(), "name1");

            // space admin get space user information
            requestUser.setSuperuser(false);

        } catch (Exception e) {
            log.error("get user by id error");
            e.printStackTrace();
        }

    }

    @Test
    public void testGetCurrentUser() {
        int userId = 1;
        long clusterId = 2;
        int collectionId = 4;

        CoreUserEntity userEntity = new CoreUserEntity();
        userEntity.setId(userId);
        userEntity.setClusterId(clusterId);

        ClusterInfoEntity clusterInfoEntity = new ClusterInfoEntity();
        clusterInfoEntity.setId(clusterId);
        clusterInfoEntity.setCollectionId(collectionId);
        // mock cluster
        when(clusterInfoRepository.findById((long) clusterId)).thenReturn(Optional.of(clusterInfoEntity));

        SettingEntity settingEntity = new SettingEntity();
        settingEntity.setKey("auth_type");
        settingEntity.setValue("ldap");
        // mock auth type
        when(settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY)).thenReturn(settingEntity);
        //
        String deployName = DeployType.manager.getName();
        when(environment.getProperty(PropertyDefine.DEPLOY_TYPE_PROPERTY)).thenReturn(deployName);

        try {
            UserInfo result = userService.getCurrentUser(userEntity);
            Assert.assertEquals(result.getAuthType(), InitStudioReq.AuthType.ldap);
            Assert.assertEquals(result.getDeployType(), deployName);
            Assert.assertEquals(result.isManagerEnable(), true);

        } catch (Exception e) {
            log.error("Get current user error.");
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateUserCurrentCluster() {
        int userId = 1;
        long clusterId = 2;
        int adminGroupId = 3;

        CoreUserEntity requestUser = new CoreUserEntity();
        requestUser.setId(userId);
        requestUser.setFirstName("test1");
        requestUser.setActive(true);

        try {
            // cluster not exist
            when(clusterInfoRepository.findById((long) clusterId)).thenReturn(Optional.empty());
            userService.updateUserCurrentCluster(requestUser, clusterId);
        } catch (Exception e) {
            log.debug("cluster not exist.");
            Assert.assertEquals(e.getMessage(), RequestFieldNullException.MESSAGE);
        }

        ClusterInfoEntity clusterInfoEntity = new ClusterInfoEntity();
        clusterInfoEntity.setId(clusterId);
        clusterInfoEntity.setAdminGroupId(adminGroupId);
        when(clusterInfoRepository.findById((long) clusterId)).thenReturn(Optional.of(clusterInfoEntity));

        SettingEntity settingEntity = new SettingEntity();
        settingEntity.setKey("auth_type");
        settingEntity.setValue("ldap");
        // mock auth type
        when(settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY)).thenReturn(settingEntity);
        //
        String deployName = DeployType.manager.getName();
        when(environment.getProperty(PropertyDefine.DEPLOY_TYPE_PROPERTY)).thenReturn(deployName);

        try {
            requestUser.setSuperuser(true);
            requestUser.setClusterId(clusterId);
            requestUser.setIsClusterAdmin(true);

            when(userRepository.save(any(CoreUserEntity.class))).thenReturn(requestUser);

            // super admin change cluster
            UserInfo result = userService.updateUserCurrentCluster(requestUser, clusterId);
            Assert.assertEquals(result.getSpaceId().intValue(), clusterId);
            Assert.assertEquals(result.isAdmin(), true);

        } catch (Exception e) {
            log.error("update user cluster id error.");
            e.printStackTrace();
        }

        try {
            // user change cluster
            // user not belong to cluster
            requestUser.setSuperuser(false);
            when(clusterUserComponent.userBelongToCluster(userId, clusterId)).thenReturn(false);
            userService.updateUserCurrentCluster(requestUser, clusterId);

            // user belong to cluster
            when(clusterUserComponent.userBelongToCluster(userId, clusterId)).thenReturn(true);
            when(membershipRepository.getByUserIdAndGroupId(userId, adminGroupId)).thenReturn(Lists.newArrayList());

            requestUser.setClusterId(clusterId);
            requestUser.setIsClusterAdmin(false);

            UserInfo result = userService.updateUserCurrentCluster(requestUser, clusterId);
            Assert.assertEquals(result.getSpaceId().intValue(), clusterId);
            Assert.assertEquals(result.isAdmin(), true);

        } catch (Exception e) {
            log.debug("user not belong to cluster.");
            Assert.assertEquals(e.getMessage(), NoPermissionException.MESSAGE);
        }

    }

    @Test
    public void testAddUser() {
        // ldap not support add user
        when(ldapComponent.enabled()).thenReturn(true);
        try {
            userService.addUser(null, null);
        } catch (Exception e) {
            log.debug("ldap not support add user");
            Assert.assertEquals(e.getMessage(), "LDAP authentication does not support adding new users independently");
        }
        when(ldapComponent.enabled()).thenReturn(false);

        // mock request user
        int userId = 1;

        CoreUserEntity requestUser = new CoreUserEntity();
        requestUser.setId(userId);
        requestUser.setFirstName("test1");
        requestUser.setActive(true);

        // request error
        NewUserAddReq userAddReq = new NewUserAddReq();
        try {
            userService.addUser(userAddReq, requestUser);
        } catch (Exception e) {
            log.debug("user request error.");
            Assert.assertEquals(e.getMessage(), RequestFieldNullException.MESSAGE);
        }

        // mock request
        String userName = "test1";
        userAddReq.setName(userName);
        userAddReq.setPassword("test_123");

        // name duplicate
        when(userRepository.getByFirstName(userName)).thenReturn(Lists.newArrayList(requestUser));
        try {
            userService.addUser(userAddReq, requestUser);
        } catch (Exception e) {
            log.debug("user name duplicate.");
            Assert.assertEquals(e.getMessage(), UserNameDuplicatedException.MESSAGE);
        }

        // email duplicate
        String email = "test@test.com";
        userAddReq.setEmail(email);
        when(userRepository.getByFirstName(userName)).thenReturn(Lists.newArrayList());
        try {
            when(utilService.emailCheck(userAddReq.getEmail())).thenReturn(true);
            when(userRepository.getByEmail(email)).thenReturn(Lists.newArrayList(requestUser));
            userService.addUser(userAddReq, requestUser);
        } catch (Exception e) {
            log.debug("user email duplicate.");
            Assert.assertEquals(e.getMessage(), UserEmailDuplicatedException.MESSAGE);
        }

        // add new user
        userAddReq.setEmail("");
        try {
            when(utilService.newPasswordCheck(userAddReq.getPassword())).thenReturn(true);
            CoreUserEntity newUser = new CoreUserEntity(userAddReq);
            newUser.setId(2);
            when(userRepository.save(any())).thenReturn(newUser);

            UserInfo result = userService.addUser(userAddReq, requestUser);
            Assert.assertEquals(result.getName(), userName);
            Assert.assertEquals(result.getId(), 2);
        } catch (Exception e) {
            log.error("add user error.");
            e.printStackTrace();
        }
    }

}
