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

package org.apache.doris.stack.service.config;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import org.apache.doris.stack.dao.CoreSessionRepository;
import org.apache.doris.stack.dao.CoreUserRepository;
import org.apache.doris.stack.dao.PermissionsGroupMembershipRepository;
import org.apache.doris.stack.entity.CoreSessionEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.model.request.config.ConfigUpdateReq;
import org.apache.doris.stack.model.request.config.InitStudioReq;
import org.apache.doris.stack.model.request.config.IdaasSettingReq;
import org.apache.doris.stack.model.request.config.LdapAuthTypeReq;
import org.apache.doris.stack.model.request.config.LdapSettingReq;
import org.apache.doris.stack.model.request.user.NewUserAddReq;
import org.apache.doris.stack.model.response.config.SettingItem;
import org.apache.doris.stack.constant.PropertyDefine;
import org.apache.doris.stack.component.ClusterUserComponent;
import org.apache.doris.stack.component.IdaasComponent;
import org.apache.doris.stack.component.LdapComponent;
import org.apache.doris.stack.component.SettingComponent;
import org.apache.doris.stack.entity.SettingEntity;
import org.apache.doris.stack.entity.StudioSettingEntity;
import org.apache.doris.stack.exception.BadRequestException;
import org.apache.doris.stack.exception.IdaasConnectionException;
import org.apache.doris.stack.exception.LdapConnectionException;
import org.apache.doris.stack.exception.StudioInitException;
import org.apache.doris.stack.model.response.user.UserInfo;
import org.apache.doris.stack.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.doris.stack.service.UtilService;
import org.apache.doris.stack.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SettingService extends BaseService {

    @Autowired
    private ClusterUserComponent clusterUserComponent;

    @Autowired
    private SettingComponent settingComponent;

    @Autowired
    private Environment environment;

    @Autowired
    private LdapComponent ldapComponent;

    @Autowired
    private IdaasComponent idaasComponent;

    @Autowired
    private CoreUserRepository userRepository;

    @Autowired
    private UtilService utilService;

    @Autowired
    private CoreSessionRepository sessionRepository;

    @Autowired
    private PermissionsGroupMembershipRepository groupMembershipRepository;

    @Transactional
    public void initStudioAuthType(LdapAuthTypeReq req) throws Exception {
        String authType = req.getAuthType();
        checkRequestBody(authType == null);
        if (!(authType.equals("ldap") || authType.equals("studio"))) {
            throw new StudioInitException();
        }
        SettingEntity entity = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        if (entity != null && !StringUtils.isEmpty(entity.getValue())) {
            log.error("Auth config type already exists.");
            throw new BadRequestException("认证类型已选择，不能修改，请刷新页面");
        }

        Map<String, String> configCache = new HashMap<>();
        settingComponent.addNewSetting(ConfigConstant.AUTH_TYPE_KEY, req.getAuthType(), configCache);
        settingComponent.addNewSetting(ConfigConstant.INIT_STEP_KEY, "1", configCache);

        ConfigCache.writeConfigs(configCache);
        log.debug("complete init studio auth type");
    }

    /**
     * Initializes the authentication method for the ervice
     * @param initStudioReq
     * @throws Exception
     */
    @Transactional
    public void initStudio(InitStudioReq initStudioReq) throws Exception {
        log.debug("Init studio auth type.");
        checkRequestBody(initStudioReq.getAuthType() == null);

        // Check whether the authentication method has been configured
        SettingEntity entity = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        if (entity != null && !StringUtils.isEmpty(entity.getValue())) {
            log.error("Auth config type already exists.");
            throw new StudioInitException();
        }

        // Store the cache information and update the cache in batches after writing to the database successfully,
        // because the database can be rolled back, but the cache cannot
        Map<String, String> configCache = new HashMap<>();

        try {
            if (initStudioReq.getAuthType() == InitStudioReq.AuthType.ldap) {
                log.debug("init ladp config.");

                // Check whether the LDAP request information is complete
                log.debug("Check LdapSettingReq info.");
                LdapSettingReq ldapSettingReq = initStudioReq.getLdapSetting();
                checkRequestBody(ldapSettingReq == null);
                checkRequestBody(ldapSettingReq.hasEmptyField());

                boolean isConnection = ldapComponent.checkLdapConnection(ldapSettingReq);
                if (!isConnection) {
                    throw new LdapConnectionException();
                }

                log.debug("add new ldap config.");
                settingComponent.addNewSetting(ConfigConstant.AUTH_TYPE_KEY, initStudioReq.getAuthType().name(),
                        configCache);
                ldapComponent.addLdapConfig(ldapSettingReq, configCache);
                log.debug("add new ldap config success.");

            } else if (initStudioReq.getAuthType() == InitStudioReq.AuthType.studio) {
                log.debug("init studio config.");
                settingComponent.addNewSetting(ConfigConstant.AUTH_TYPE_KEY, initStudioReq.getAuthType().name(),
                       configCache);
            } else if (initStudioReq.getAuthType() == InitStudioReq.AuthType.idaas) {
                log.debug("init idaas config.");
                // Check whether the idaas request information is complete
                log.debug("Check IdaasSettingReq info.");
                IdaasSettingReq idaasSettingReq = initStudioReq.getIdaasSetting();
                checkRequestBody(idaasSettingReq == null);
                checkRequestBody(idaasSettingReq.hasEmptyField());

                // Check idaas connectivity
                boolean isConnection = idaasComponent.checkIdaasConnection(idaasSettingReq);
                if (!isConnection) {
                    throw new IdaasConnectionException();
                }

                log.debug("add new idaas config.");
                settingComponent.addNewSetting(ConfigConstant.AUTH_TYPE_KEY, initStudioReq.getAuthType().name(), configCache);
                idaasComponent.addIdaasConfig(idaasSettingReq, configCache);
                log.debug("add new idaas config success.");

            }  else {
                throw new BadRequestException("不支持此认证方式.");
            }
        } catch (Exception e) {
            SettingEntity authTypeSetting = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);
            if (authTypeSetting != null && !StringUtils.isEmpty(authTypeSetting.getValue())) {
                log.error("Write auth type config error, delete auth type from database.");
                settingComponent.deleteSetting(ConfigConstant.AUTH_TYPE_KEY);
            }
            throw e;
        }

        ConfigCache.writeConfigs(configCache);
    }

    // ldap auth, the second step
    @Transactional
    public void initLdapStudio(InitStudioReq initStudioReq) throws Exception {
        log.debug("Init ldap studio");
        checkRequestBody(initStudioReq.getAuthType() == null);
        SettingEntity entity = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        if (entity == null || !InitStudioReq.AuthType.ldap.name().equals(entity.getValue())) {
            log.error("auth type need to be ldap");
            throw new StudioInitException();
        }

        SettingEntity initStep = settingComponent.readSetting(ConfigConstant.INIT_STEP_KEY);
        if (!initStep.getValue().equals("1")) {
            log.error("auth config has not completed");
            throw new StudioInitException();
        }

        if (initStep.getValue().equals("2")) {
            log.error("ldap config has completed");
            throw new BadRequestException("LDAP认证已完成，请刷新页面");
        }

        try {
            log.debug("init ladp config.");

            // Check whether the LDAP request information is complete
            log.debug("Check LdapSettingReq info.");
            LdapSettingReq ldapSettingReq = initStudioReq.getLdapSetting();
            checkRequestBody(ldapSettingReq == null);
            checkRequestBody(ldapSettingReq.hasEmptyField());
            ldapSettingReq.removeBlankSpace();

            boolean isConnection = ldapComponent.checkLdapConnection(ldapSettingReq);
            if (!isConnection) {
                throw new LdapConnectionException();
            }
            Map<String, String> configCache = new HashMap<>();
            ldapComponent.addLdapConfig(ldapSettingReq, configCache);
            ConfigCache.writeConfigs(configCache);

            log.debug("add new ldap config success.");
            // sync user
            saveLdapUser(ldapSettingReq);
            log.debug("sync ldap user success.");
            settingComponent.addNewSetting(ConfigConstant.INIT_STEP_KEY, "2", configCache);
            ConfigCache.writeConfigs(configCache);

        } catch (Exception e) {
            log.error("init studio ldap auth failed.");
            throw e;
        }

    }

    // save ldap user
    private void saveLdapUser(LdapSettingReq ldapSettingReq) {
        List<CoreUserEntity> coreUserEntities = ldapComponent.syncLdapUser(ldapSettingReq);
        if (coreUserEntities.size() == 0) {
            throw new BadRequestException("同步数据为空，请检查该节点下是否有用户");
        }
        for (CoreUserEntity coreUserEntity : coreUserEntities) {

           ldapComponent.saveLdapUser(coreUserEntity);
        }

    }

    /**
     * Initialize configuration information at service startup
     * If it has been initialized and has not been modified, the configuration will not be changed
     * If the configuration information exists in the database, it is updated to the cache
     */
    @Transactional
    public void initConfig() throws Exception {
        log.debug("init config.");

        // Store cache information
        Map<String, String> configCache = new HashMap<>();

        // Write configuration items that can read environment variables to the database and cache
        for (String key : ConfigConstant.PUBLIC_CONFIGS.keySet()) {
            ConfigItem configItem = ConfigConstant.PUBLIC_CONFIGS.get(key);

            // Read the environment variable and judge whether the user has set new configuration information
            String envValue = System.getenv(configItem.getEnvName());
            if (StringUtils.isEmpty(envValue)) {
                // If the environment variable is not configured, it will be configured according to whether the
                // configuration information already exists. If it does not exist, the default value will be used
                if (key.equals(ConfigConstant.SITE_URL_KEY)) {
                    // The service address information does not need to be configured here
                    continue;
                } else {
                    SettingEntity entity = settingComponent.readSetting(key);
                    if (entity == null || StringUtils.isEmpty(entity.getValue())) {
                        settingComponent.addNewSetting(key, configItem.getDefaultValue(), configCache);
                    }
                }
            } else {
                // If the environment variable is configured with new information,
                // it indicates that the user wants to modify the new configuration
                settingComponent.addNewSetting(key, envValue, configCache);
            }
        }

        // Write deploy-type to database and cache
        settingComponent.addNewSetting(ConfigConstant.DEPLOY_TYPE,
                environment.getProperty(PropertyDefine.DEPLOY_TYPE_PROPERTY), configCache);

        ConfigCache.writeConfigs(configCache);
        log.debug("init config end.");
    }

    @Transactional
    public void syncLdapUser() {
        log.debug("sync ldap user manually");

        ldapComponent.incrementalSyncLdapUser();
    }

    // add system init admin
    @Transactional
    public String addAdminUser(NewUserAddReq addReq) throws Exception {
        // check whether system has been init
        SettingEntity entity = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        if (entity == null) {
            log.error("auth type need to be set");
            throw new StudioInitException();
        }

        SettingEntity initStep = settingComponent.readSetting(ConfigConstant.INIT_STEP_KEY);

        if (entity.getValue().equals("ldap")) {
            // no password
            checkRequestBody(addReq.checkNameEmpty());
        } else {
            checkRequestBody(addReq.hasEmptyField());
            if (!StringUtils.isEmpty(addReq.getEmail())) {
                // Check whether the mailbox format meets the specification
                utilService.emailCheck(addReq.getEmail());
            }
        }

        CoreUserEntity userEntity;
        Map<String, String> configCache = new HashMap<>();
        if (ldapComponent.enabled()) {
            log.debug("add ldap init admin user");
            if (!initStep.getValue().equals("2")) {
                log.error("the second step ldap config not completed");
                throw new StudioInitException();
            }
            if (initStep.getValue().equals("3")) {
                log.error("ldap config has completed");
                throw new StudioInitException();
            }

            // password does not sync to studio
            // validate ldap user password by ldap search
//            LdapUser ldapUser = ldapComponent.authenticateInitAdminUser(addReq);
//            if (ldapUser == null) {
//                throw new BadRequestException("input Ldap user or password error");
//            } else if (addReq.getEmail() != null) {
//                if (!ldapUser.getEmail().equals(addReq.getEmail())) {
//                    throw new BadRequestException("input Ldap email error");
//                }
//            }
            userEntity = userRepository.getByFirstName(addReq.getName()).get(0);

            settingComponent.addNewSetting(ConfigConstant.INIT_STEP_KEY, "3", configCache);
        } else {
            if (initStep.getValue().equals("2")) {
                log.error("studio config has completed");
                throw new StudioInitException();
            }
            userEntity = new CoreUserEntity(addReq);
            utilService.newPasswordCheck(addReq.getPassword());

            utilService.setPassword(userEntity, addReq.getPassword());
            settingComponent.addNewSetting(ConfigConstant.INIT_STEP_KEY, "2", configCache);
        }
        ConfigCache.writeConfigs(configCache);
        userEntity.setSuperuser(true);
        userEntity.setLastLogin(new Timestamp(System.currentTimeMillis()));
        userEntity.setDateJoined(new Timestamp(System.currentTimeMillis()));

        CoreUserEntity saveUserEntity = userRepository.save(userEntity);
        log.debug("save new user {} success.", saveUserEntity.getId());

        // add login session
        String sessionId = UuidUtil.newUuid();
        CoreSessionEntity sessionEntity = new CoreSessionEntity(sessionId, saveUserEntity.getId(),
                new Timestamp(System.currentTimeMillis()), null);
        sessionRepository.save(sessionEntity);
        log.debug("Add session for user {}", saveUserEntity.getId());
        return sessionId;

    }

    /**
     * The space administrator obtains the configuration items of the current
     * space and the configuration items common to all spaces
     * @param user
     * @return
     * @throws Exception
     */
    public List<SettingItem> getAllConfig(CoreUserEntity user) throws Exception {
        int userId = user.getId();
        log.debug("user {} get all config info.", userId);
        int clusterId = clusterUserComponent.getUserCurrentClusterIdAndCheckAdmin(user);

        List<SettingItem> settingItems = getAllPublicConfig();

        for (String key : ConfigConstant.ALL_ADMIN_CONFIGS.keySet()) {
            ConfigItem item = ConfigConstant.ALL_ADMIN_CONFIGS.get(key);
            StudioSettingEntity studioSettingEntity = settingComponent.readAdminSetting(clusterId, key);
            SettingItem settingItem = item.transAdminSettingToModel(studioSettingEntity);
            settingItems.add(settingItem);
        }

        log.debug("get all config info.");

        return settingItems;
    }

    /**
     * Super administrator can only view all public configuration items
     * @return
     * @throws Exception
     */
    public List<SettingItem> getAllPublicConfig() {
        log.debug("super admin get all common config info.");
        List<SettingItem> settingItems = new ArrayList<>();

        for (String key : ConfigConstant.ALL_PUBLIC_CONFIGS.keySet()) {
            ConfigItem item = ConfigConstant.ALL_PUBLIC_CONFIGS.get(key);
            SettingEntity entity = settingComponent.readSetting(key);
            SettingItem settingItem = item.transSettingToModel(entity);
            settingItems.add(settingItem);
        }

        return settingItems;
    }

    /**
     * Super administrator can only view all public configuration items
     * @param key
     * @return
     * @throws Exception
     */
    public SettingItem getConfigByKey(String key) throws Exception {
        if (ConfigConstant.ALL_PUBLIC_CONFIGS.keySet().contains(key)) {
            log.debug("public config key {}.", key);
            SettingEntity entity = settingComponent.readSetting(key);
            ConfigItem item = ConfigConstant.ALL_PUBLIC_CONFIGS.get(key);
            SettingItem settingItem = item.transSettingToModel(entity);
            return settingItem;
        }

        log.error("Input key {} error.", key);
        throw new BadRequestException("Configuration key does not exist.");
    }

    /**
     * The space administrator obtains the configuration information common to the current space
     * In space access
     * @param user
     * @param key
     * @return
     * @throws Exception
     */
    public SettingItem getConfigByKey(String key, CoreUserEntity user) throws Exception {
        int userId = user.getId();
        log.debug("User {} get config by key {}.", userId, key);
        int clusterId = clusterUserComponent.getUserCurrentClusterIdAndCheckAdmin(user);
        if (ConfigConstant.ALL_ADMIN_CONFIGS.keySet().contains(key)) {
            StudioSettingEntity entity = settingComponent.readAdminSetting(clusterId, key);
            ConfigItem item = ConfigConstant.ALL_ADMIN_CONFIGS.get(key);
            return item.transAdminSettingToModel(entity);
        }

        log.error("Input key {} error.", key);
        throw new BadRequestException("Configuration key does not exist.");
    }

    /**
     * The space administrator can only modify the configuration items of the current space
     * In space access
     * @param key
     * @param user
     * @param updateReq
     * @throws Exception
     */
    @Transactional
    public SettingItem amdinUpdateConfigByKey(String key, CoreUserEntity user, ConfigUpdateReq updateReq) throws Exception {
        int userId = user.getId();
        log.debug("User {} update config by key {}.", userId, key);
        checkRequestBody(updateReq.hasEmptyField());
        int clusterId = clusterUserComponent.getUserCurrentClusterIdAndCheckAdmin(user);

        if (ConfigConstant.ALL_ADMIN_CONFIGS.keySet().contains(key)) {
            ConfigItem configItem = ConfigConstant.ALL_ADMIN_CONFIGS.get(key);
            String value = updateReq.getValue().toString();

            StudioSettingEntity oldEntity = settingComponent.readAdminSetting(clusterId, key);
            StudioSettingEntity newEntity = settingComponent.addNewAdminSetting(clusterId, key, value);

            SettingItem item = configItem.transAdminSettingToModel(newEntity);
            if (oldEntity != null) {
                item.setOriginalValue(oldEntity.getValue());
            }
            return item;
        } else {
            log.error("Input key {} error.", key);
            throw new BadRequestException("Configuration key does not exist.");
        }
    }

    /**
     * Super administrator can only modify public configuration items
     * @param key
     * @param updateReq
     * @throws Exception
     */
    @Transactional
    public SettingItem superUpdateConfigByKey(String key, ConfigUpdateReq updateReq) throws Exception {
        log.debug("Super User update config by key {}.", key);
        checkRequestBody(updateReq.hasEmptyField());
        if (ConfigConstant.ALL_PUBLIC_CONFIGS.keySet().contains(key)) {
            log.debug("public config key.");
            ConfigItem configItem = ConfigConstant.ALL_PUBLIC_CONFIGS.get(key);
            String value;
            if (key.equals(ConfigConstant.CUSTOM_FORMATTING_KEY)) {
                value = JSON.toJSONString(updateReq.getValue());
            } else {
                value = updateReq.getValue().toString();
            }
            SettingEntity oldEntity = settingComponent.readSetting(key);
            SettingEntity newEntity = settingComponent.addNewSetting(key, value, ConfigCache.configCache);
            SettingItem item = configItem.transSettingToModel(newEntity);
            if (oldEntity != null) {
                item.setOriginalValue(oldEntity.getValue());
            }
            return item;
        } else {
            log.error("Input key {} error.", key);
            throw new BadRequestException("Configuration key does not exist.");
        }
    }

    // reset system init
    @Transactional
    public void reset(int userId) throws Exception {

        // built in user
        if (userId != -1) {
            throw new BadRequestException("user id not valid");
        }
        log.debug("reset system init");
        Map<String, String> configCache = new HashMap<>();
        settingComponent.addNewSetting(ConfigConstant.AUTH_TYPE_KEY, "", configCache);
        settingComponent.addNewSetting(ConfigConstant.INIT_STEP_KEY, "0", configCache);

        ConfigCache.writeConfigs(configCache);

        List<CoreUserEntity> userEntities = userRepository.findAll();
        userEntities.forEach(user -> userRepository.deleteUserById(user.getId()));
        // delete user, do not use jpa deleteAll
        userRepository.deleteAllUsers();
        // delete session
        sessionRepository.deleteAllSessions();
        // delete group member
        groupMembershipRepository.deleteAllGroupMembers();
        log.debug("delete all users, sessions, group members succeed");

    }

    // ldap auth,get all user, select one as admin
    public List<UserInfo> getAllUsers(String q) throws Exception {

        SettingEntity entity = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        SettingEntity initStep = settingComponent.readSetting(ConfigConstant.INIT_STEP_KEY);

        if (entity == null || !entity.getValue().equals("ldap")) {
            log.error("auth type need to be set");
            throw new StudioInitException();
        }

        if (initStep == null || !initStep.getValue().equals("2")) {
            log.error("ldap init step error");
            throw new StudioInitException();
        }

        // get all user
        List<CoreUserEntity> userEntities = userRepository.getAllUsers(q);

        List<UserInfo> userInfos = Lists.newArrayList();
        for (CoreUserEntity userEntity : userEntities) {
            UserInfo userInfo = userEntity.castToUserInfoWithoutClusterInfo();
            userInfos.add(userInfo);
        }

        return userInfos;
    }

    // studio upgrade，skip the init system process
    public void skipInitSystem() throws Exception {
        List<CoreUserEntity> userEntities = userRepository.findAll();
        if (userEntities.size() > 0) {
            SettingEntity entity = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);
            Map<String, String> configCache = new HashMap<>();
            if (entity != null) {
                 if (entity.getValue().equals("studio")) {
                     settingComponent.addNewSetting(ConfigConstant.INIT_STEP_KEY, "2", configCache);

                 } else {
                     settingComponent.addNewSetting(ConfigConstant.INIT_STEP_KEY, "3", configCache);
                 }
                ConfigCache.writeConfigs(configCache);
            }

        }
    }
}
