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

import static org.apache.doris.stack.constant.ConstantDef.EMAIL_REGEX;

import org.apache.doris.stack.connector.LdapUserAttributeMapper;
import org.apache.doris.stack.dao.ClusterUserMembershipRepository;
import org.apache.doris.stack.dao.CoreSessionRepository;
import org.apache.doris.stack.dao.CoreUserRepository;
import org.apache.doris.stack.dao.PermissionsGroupMembershipRepository;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.exception.BadRequestException;
import org.apache.doris.stack.model.ldap.LdapConnectionInfo;
import org.apache.doris.stack.model.ldap.LdapUser;
import org.apache.doris.stack.model.ldap.LdapUserInfo;
import org.apache.doris.stack.model.ldap.LdapUserInfoReq;
import org.apache.doris.stack.model.request.config.InitStudioReq;
import org.apache.doris.stack.model.request.config.LdapSettingReq;
import org.apache.doris.stack.model.response.config.LdapSettingResp;
import org.apache.doris.stack.connector.LdapClient;
import org.apache.doris.stack.entity.SettingEntity;
import org.apache.doris.stack.exception.UserLoginException;
import org.apache.doris.stack.service.config.ConfigConstant;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

@Component
@Slf4j
public class LdapComponent {
    private static final String USER_BASE_DIV = "&";

    @Autowired
    private SettingComponent settingComponent;

    @Autowired
    private LdapClient ldapClient;

    @Autowired
    private CoreUserRepository userRepository;

    @Autowired
    private PermissionsGroupMembershipRepository membershipRepository;

    @Autowired
    private ClusterUserMembershipRepository clusterUserMembershipRepository;

    @Autowired
    private CoreSessionRepository sessionRepository;

    private LdapTemplate ldapTemplate;

    private LdapContextSource contextSource;

    private static SearchControls controls;

    static {
        controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(new String[]{"*", "+"});
    }

    public void addLdapConfig(LdapSettingReq ldapSettingReq, Map<String, String> configCache) throws Exception {

        settingComponent.addNewSetting(ConfigConstant.LDAP_HOST_KEY, ldapSettingReq.getLdapHost(), configCache);

        String portStr;
        if (ldapSettingReq.getLdapPort() == null) {
            portStr = LdapSettingReq.PORT;
        } else {
            portStr = ldapSettingReq.getLdapPort().toString();
        }
        settingComponent.addNewSetting(ConfigConstant.LDAP_PORT_KEY, portStr, configCache);

        settingComponent.addNewSetting(ConfigConstant.LDAP_SECURITY_KEY, ldapSettingReq.getLdapSecurity(), configCache);

        settingComponent.addNewSetting(ConfigConstant.LDAP_BIND_DN_KEY, ldapSettingReq.getLdapBindDn(), configCache);

        settingComponent.addNewSetting(ConfigConstant.LDAP_PASSWORD_KEY, ldapSettingReq.getLdapPassword(), configCache);

        settingComponent.addNewSetting(ConfigConstant.LDAP_USER_BASE_KEY,
                String.join(USER_BASE_DIV, ldapSettingReq.getLdapUserBase()), configCache);

        settingComponent.addNewSetting(ConfigConstant.LDAP_USER_FILTER_KEY, ldapSettingReq.getLdapUserFilter(), configCache);

        // The default setting property is mail
        String ldapAttributeEmailStr;
        if (ldapSettingReq.getLdapAttributeEmail() == null) {
            ldapAttributeEmailStr = LdapSettingReq.MAIL;
        } else {
            ldapAttributeEmailStr = ldapSettingReq.getLdapAttributeEmail();
        }

        settingComponent.addNewSetting(ConfigConstant.LDAP_ATTRIBUTE_EMAIL_KEY, ldapAttributeEmailStr, configCache);

        settingComponent.addNewSetting(ConfigConstant.LDAP_ATTRIBUTE_FIRSTNAME_KEY, ldapSettingReq.getLdapAttributeFirstName(), configCache);

        settingComponent.addNewSetting(ConfigConstant.LDAP_ATTRIBUTE_LASTNAME_KEY, ldapSettingReq.getLdapAttributeLastName(), configCache);
    }

    /**
     * Is LDAP authentication enabled
     *
     * @return
     */
    public boolean enabled() {
        SettingEntity entity = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        if (entity == null) {
            log.debug("The ldap is not enabled");
            return false;
        } else if (entity.getValue().equals(InitStudioReq.AuthType.ldap.name())) {
            log.debug("The ldap is enabled");
            return true;
        } else {
            return false;
        }
    }

    /**
     * get LDAP Configuration item
     *
     * @return
     */
    public LdapSettingResp readLdapConfig() {
        LdapSettingResp ldapSettingResp = new LdapSettingResp();

        if (!enabled()) {
            log.warn("Ldap configuration don't exist.");
            ldapSettingResp.setLdapEnabled(false);
            return ldapSettingResp;
        }

        ldapSettingResp.setLdapEnabled(true);

        SettingEntity ldapHost = settingComponent.readSetting(ConfigConstant.LDAP_HOST_KEY);
        if (ldapHost != null) {
            ldapSettingResp.setLdapHost(ldapHost.getValue());
        }

        SettingEntity ldapPort = settingComponent.readSetting(ConfigConstant.LDAP_PORT_KEY);
        if (ldapPort != null) {
            ldapSettingResp.setLdapPort(Integer.parseInt(ldapPort.getValue()));
        }

        SettingEntity ldapSecurity = settingComponent.readSetting(ConfigConstant.LDAP_SECURITY_KEY);
        if (ldapSecurity != null) {
            ldapSettingResp.setLdapSecurity(ldapSecurity.getValue());
        }

        SettingEntity ldapBindDN = settingComponent.readSetting(ConfigConstant.LDAP_BIND_DN_KEY);
        if (ldapBindDN != null) {
            ldapSettingResp.setLdapBindDn(ldapBindDN.getValue());
        }

        SettingEntity ldapPassword = settingComponent.readSetting(ConfigConstant.LDAP_PASSWORD_KEY);
        if (ldapPassword != null) {
            ldapSettingResp.setLdapPassword(ldapPassword.getValue());
        }

        ldapSettingResp.setLdapUserBase(getBaseDn());

        SettingEntity ldapUserFilter = settingComponent.readSetting(ConfigConstant.LDAP_USER_FILTER_KEY);
        if (ldapUserFilter != null) {
            ldapSettingResp.setLdapUserFilter(ldapUserFilter.getValue());
        }

        SettingEntity ldapAttributeEmail = settingComponent.readSetting(ConfigConstant.LDAP_ATTRIBUTE_EMAIL_KEY);
        if (ldapAttributeEmail != null) {
            ldapSettingResp.setLdapAttributeEmail(ldapAttributeEmail.getValue());
        }

        SettingEntity ldapAttributeFirstName = settingComponent.readSetting(ConfigConstant.LDAP_ATTRIBUTE_FIRSTNAME_KEY);
        if (ldapAttributeFirstName != null) {
            ldapSettingResp.setLdapAttributeFirstName(ldapAttributeFirstName.getValue());
        }

        SettingEntity ldapAttributeLastName = settingComponent.readSetting(ConfigConstant.LDAP_ATTRIBUTE_LASTNAME_KEY);
        if (ldapAttributeLastName != null) {
            ldapSettingResp.setLdapAttributeLastName(ldapAttributeLastName.getValue());
        }

        return ldapSettingResp;
    }

    /**
     * get Base DN
     *
     * @return
     */
    public List<String> getBaseDn() {
        SettingEntity userBases = settingComponent.readSetting(ConfigConstant.LDAP_USER_BASE_KEY);
        if (userBases != null && userBases.getValue() != null) {
            return Arrays.asList(userBases.getValue().split(USER_BASE_DIV));
        } else {
            return null;
        }
    }

    /**
     * get ldap Connected entity
     *
     * @return
     */
    public LdapConnectionInfo getConnInfo() {
        LdapSettingResp resp = readLdapConfig();

        LdapConnectionInfo ldapConnection = new LdapConnectionInfo();
        ldapConnection.setHost(resp.getLdapHost());
        ldapConnection.setPort(resp.getLdapPort());
        ldapConnection.setBindDn(resp.getLdapBindDn());
        ldapConnection.setPassword(resp.getLdapPassword());
        ldapConnection.setAttributeEmail(resp.getLdapAttributeEmail());

        return ldapConnection;
    }

    /**
     * Encapsulate LDAP request information
     *
     * @param ldapSettingReq
     * @return
     */
    public boolean checkLdapConnection(LdapSettingReq ldapSettingReq) {
        log.debug("check ldap connection info by LdapSettingReq.");
        LdapConnectionInfo ldapConnection = new LdapConnectionInfo();
        ldapConnection.setHost(ldapSettingReq.getLdapHost());
        if (ldapSettingReq.getLdapPort() == null) {
            ldapConnection.setPort(Integer.valueOf(LdapSettingReq.PORT));
        } else {
            ldapConnection.setPort(ldapSettingReq.getLdapPort());
        }
        ldapConnection.setBindDn(ldapSettingReq.getLdapBindDn());
        ldapConnection.setPassword(ldapSettingReq.getLdapPassword());
        ldapConnection.setSecurity(ldapSettingReq.getLdapSecurity());
        ldapConnection.setUserBase(ldapSettingReq.getLdapUserBase());
        ldapConnection.setUserFilter(ldapSettingReq.getLdapUserFilter());
        if (ldapSettingReq.getLdapAttributeEmail() == null) {
            ldapConnection.setAttributeEmail(LdapSettingReq.MAIL);
        } else {
            ldapConnection.setAttributeEmail(ldapSettingReq.getLdapAttributeEmail());
        }

        ldapConnection.setAttributeFirstname(ldapSettingReq.getLdapAttributeFirstName());
        ldapConnection.setAttributeLastname(ldapSettingReq.getLdapAttributeLastName());

        if (ldapClient.getConnection(ldapConnection) == null) {
            log.error("Ldap Connection info error, please check.");
            return false;
        }
        log.debug("Ldap Connection success.");
        return true;
    }

    /**
     * Find users by mailbox ID
     *
     * @param email
     * @return
     */
    public LdapUserInfo searchUserByEmail(String email) throws Exception {
        // get LDAP connection
        LdapConnectionInfo connectionInfo = getConnInfo();
        LDAPConnection connection = ldapClient.getConnection(connectionInfo);

        // LDAP request
        LdapUserInfoReq userInfoReq = new LdapUserInfoReq();
        userInfoReq.setBaseDn(getBaseDn());
        userInfoReq.setUserAttribute(connectionInfo.getAttributeEmail());
        userInfoReq.setUserValue(email);

        // search user
        LdapUserInfo ldapUser = ldapClient.getUser(connection, userInfoReq);
        if (ldapUser.getExist()) {
            return ldapUser;
        }
        log.error("Ldap User {} is not exist, please check and try again.", email);
        throw new UserLoginException();
    }

    // ldapTemplate init
    public void initLdap(LdapSettingReq ldapSettingReq) {
        contextSource = new LdapContextSource();
        Map<String, Object> config = new HashMap<>();

        contextSource.setUrl("ldap://" + ldapSettingReq.getLdapHost() + ":" + ldapSettingReq.getLdapPort());
        contextSource.setUserDn(ldapSettingReq.getLdapBindDn());
        contextSource.setPassword(ldapSettingReq.getLdapPassword());
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();
        config.put("java.naming.ldap.attributes.binary", "objectGUID");

        contextSource.setPooled(true);
        contextSource.setBaseEnvironmentProperties(config);
        ldapTemplate = new LdapTemplate(contextSource);
    }

    // get all user in ldap server
    public List<CoreUserEntity> syncLdapUser(LdapSettingReq ldapSettingReq) {
        List<CoreUserEntity> coreUserEntities = new ArrayList<>();
        initLdap(ldapSettingReq);
        for (String base : ldapSettingReq.getLdapUserBase()) {
            // return internal attributes

            String filter = ldapSettingReq.getLdapUserFilter();
            List<CoreUserEntity> list = ldapTemplate.search(base, filter,
                    controls, new LdapUserAttributeMapper(ldapSettingReq));
            coreUserEntities.addAll(list);
        }

        return coreUserEntities;
    }

    // Incremental ldap user synchronization
    public void incrementalSyncLdapUser() {

        SettingEntity authType = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);
        SettingEntity initStep = settingComponent.readSetting(ConfigConstant.INIT_STEP_KEY);
        if (!(authType != null && initStep != null && authType.getValue().equals("ldap") && initStep.getValue().equals("3"))) {
            log.debug("not ldap auth or auth has not completed, skip increment sync ldap user");
            return;
        }

        log.debug("start to increment sync ldap user");
        LdapSettingResp resp = readLdapConfig();

        // set ldap setting
        LdapSettingReq req = getLdapSettings(resp);
        synchronized (this) {
            log.debug("get lock, start to increment sync ldap user");
            List<CoreUserEntity> coreUserEntities = syncLdapUser(req);

            List<CoreUserEntity> oldCoreUserEntities = userRepository.findAll();
            oldCoreUserEntities =
                    oldCoreUserEntities.stream().filter(e -> (e.getEntryUUID() == null || !e.getEntryUUID().equals(""))).collect(Collectors.toList());

            List<CoreUserEntity> existUserEntities = ListUtil.getExistList(coreUserEntities, oldCoreUserEntities);
            // update email or username if it has changed
            for (CoreUserEntity existUserEntity : existUserEntities) {
                String entryUUID = existUserEntity.getEntryUUID();
                CoreUserEntity oldUser =
                        oldCoreUserEntities.stream().filter(user -> user.getEntryUUID().equals(entryUUID)).collect(Collectors.toList()).get(0);
                if (!oldUser.getEmail().equals(existUserEntity.getEmail())) {
                    oldUser.setEmail(existUserEntity.getEmail());
                }
                if (!oldUser.getFirstName().equals(existUserEntity.getFirstName())) {
                    oldUser.setFirstName(existUserEntity.getFirstName());
                }
                userRepository.save(oldUser);
            }
            List<CoreUserEntity> addList = ListUtil.getAddList(coreUserEntities, oldCoreUserEntities);
            List<CoreUserEntity> reduceList = ListUtil.getReduceList(coreUserEntities, oldCoreUserEntities);
            log.debug("ldap user size is {},user table size is {}", coreUserEntities.size(),
                    oldCoreUserEntities.size());
            // add new ldap user
            log.debug("add new ldap user ,size is {}", addList.size());
            addList.forEach(this::saveLdapUser);
            // reduce ldap user no longer exist
            log.debug("reduce ldap user no longer exist, size is {}", reduceList.size());
            for (CoreUserEntity userEntity : reduceList) {
                //List<CoreUserEntity> userEntities = userRepository.getByFirstName(userEntity.getFirstName());
                //userEntities.forEach(user -> userRepository.deleteById(user.getId()));
                // stop this user, todo delete
                userEntity.setEntryUUID("");
                userRepository.save(userEntity);
//                membershipRepository.deleteByUserId(userEntity.getId());
//                clusterUserMembershipRepository.deleteByUserId(userEntity.getId());
//                sessionRepository.deleteByUserId(userEntity.getId());
            }
        }

    }

    public void saveLdapUser(CoreUserEntity coreUserEntity) {
        coreUserEntity.setActive(true);
        coreUserEntity.setDateJoined(new Timestamp(System.currentTimeMillis()));
        coreUserEntity.setLdapAuth(true);
        coreUserEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        coreUserEntity.setPassword("");
        coreUserEntity.setPasswordSalt("");
        userRepository.saveAndFlush(coreUserEntity);
    }

    private LdapSettingReq getLdapSettings(LdapSettingResp resp) {

        // set ldap setting
        LdapSettingReq req = new LdapSettingReq();
        req.setLdapHost(resp.getLdapHost());
        req.setLdapPort(resp.getLdapPort());
        req.setLdapBindDn(resp.getLdapBindDn());
        req.setLdapPassword(resp.getLdapPassword());
        req.setLdapAttributeEmail(resp.getLdapAttributeEmail());
        req.setLdapAttributeFirstName(resp.getLdapAttributeFirstName());
        req.setLdapAttributeLastName(resp.getLdapAttributeLastName());
        req.setLdapUserBase(resp.getLdapUserBase());
        req.setLdapUserFilter(resp.getLdapUserFilter());
        return req;
    }

    // authenticate ldap user and password
    public LdapUser authenticateLdapUser(String name, String password) {
        LdapSettingResp resp = readLdapConfig();

        // set ldap setting
        LdapSettingReq req = getLdapSettings(resp);

        // init ldap
        initLdap(req);

        DirContext ctx = null;
        LdapUser ldapUser = null;
        for (String base : resp.getLdapUserBase()) {
            try {
                String filter;
                if (name.matches(EMAIL_REGEX)) {
                    filter = resp.getLdapAttributeEmail();
                } else {
                    filter = resp.getLdapAttributeFirstName();
                }
                List<LdapUser> ldapUsers = ldapTemplate.search(base, filter + "=" + name,
                        controls, new AbstractContextMapper<LdapUser>() {
                            @Override
                            protected LdapUser doMapFromContext(DirContextOperations ctx) {
                                LdapUser ldapUser = new LdapUser();

                                String mailValue = (String) ctx.getObjectAttribute(resp.getLdapAttributeEmail());
                                ldapUser.setEmail(mailValue);
                                String lastNameValue = (String) ctx.getObjectAttribute(resp.getLdapAttributeLastName());
                                ldapUser.setLastName(lastNameValue);
                                ldapUser.setDn(ctx.getDn().toString());
                                ldapUser.setEntryUUID(ctx.getStringAttribute("entryUUID"));
                                ldapUser.setFirstName(ctx.getStringAttribute(resp.getLdapAttributeFirstName()));
                                return ldapUser;
                            }
                        });
                if (ldapUsers.size() != 1) {
                    throw new BadRequestException("input user name not exist");
                }
                ldapUser = ldapUsers.get(0);
                log.debug("user dn is {}", ldapUser.getDn());
                ctx = contextSource.getContext(ldapUser.getDn(), password);

                log.debug("authenticate ldap user and password success");
                return ldapUser;

            } catch (Exception e) {

                log.error("authenticate ldap user and password failed in base {}", base);

            } finally {

                LdapUtils.closeContext(ctx);

            }

        }
        return null;

    }

    // studio upgrade, set entry uuid for old user
    public void setEntryUUIDForUser() {
        SettingEntity authType = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);

        if (authType != null && authType.getValue().equals("ldap")) {
            log.debug("studio upgrade, set entry uuid for old user");
            List<CoreUserEntity> coreUsers = userRepository.getByNullEntryUUID();
            log.debug("studio upgrade, set entry uuid for old user, size is {}", coreUsers.size());
            if (coreUsers.size() == 0) {
                return;
            }
            LdapSettingResp resp = readLdapConfig();

            // set ldap setting
            LdapSettingReq req = getLdapSettings(resp);

            List<CoreUserEntity> ldapUserEntities = syncLdapUser(req);
            for (CoreUserEntity coreUser : coreUsers) {
                // studio 1.1 , first name = given name
                String email = coreUser.getEmail();
                log.debug("user {} email is {}", coreUser.getId(), coreUser.getEmail());
                if (email != null) {

                    Optional<CoreUserEntity> ldapUserOptional =
                            ldapUserEntities.stream().filter(e -> e.getEmail().equals(email)).findFirst();
                    if (!ldapUserOptional.isPresent()) {
                        log.warn("user email {} not exist", email);
                    } else {
                        CoreUserEntity ldapUser = ldapUserOptional.get();
                        coreUser.setEntryUUID(ldapUser.getEntryUUID());
                        userRepository.save(coreUser);
                        log.debug("save entry uuid for user {} success", coreUser.getId());
                    }
                }

            }
        }
    }

}
