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

package org.apache.doris.stack.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.doris.stack.component.LdapComponent;
import org.apache.doris.stack.component.MailComponent;
import org.apache.doris.stack.component.SettingComponent;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.entity.SettingEntity;
import org.apache.doris.stack.entity.StudioSettingEntity;
import org.apache.doris.stack.model.request.config.EmailInfo;
import org.apache.doris.stack.model.response.config.LdapSettingResp;
import org.apache.doris.stack.model.response.config.SessionSettingResp;
import org.apache.doris.stack.service.config.ConfigConstant;
import org.apache.doris.stack.service.config.ConfigValueDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PropertyService {

    @Autowired
    private MailComponent mailComponent;

    @Autowired
    private LdapComponent ldapComponent;

    @Autowired
    private SettingComponent settingComponent;

    public SessionSettingResp properties() throws Exception {
        SessionSettingResp resp = new SessionSettingResp();

        // authentication type
        SettingEntity authType = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);
        if (authType != null) {
            resp.setAuthType(authType.getValue());
        }

        // What is the current step to system initialization
        SettingEntity initStep = settingComponent.readSetting(ConfigConstant.INIT_STEP_KEY);
        if (initStep != null) {
            resp.setInitStep(Integer.parseInt(initStep.getValue()));
        }
        if (authType != null && initStep != null) {
            if ((authType.getValue().equals("studio") && initStep.getValue().equals("2")) || (
                    authType.getValue().equals("ldap") && initStep.getValue().equals("3"))) {
                resp.setCompleted(1);
            }
        }
        return resp;
    }

    public SessionSettingResp properties(CoreUserEntity user) throws Exception {
        int userId = user.getId();
        SessionSettingResp resp = new SessionSettingResp();

        // Gets the default value of the global definition
        resp.setCustomGeojson(ConfigValueDef.customGeojson);
        resp.setAvailableTimezones(ConfigValueDef.AVAILABLE_TIMEZONES);
        resp.setAvailableLocales(ConfigValueDef.availableLocales);

        // Read mailbox configuration
        EmailInfo mailInfo = mailComponent.getEmailInfo();
        if (mailInfo != null) {
            resp.setEmailConfigured(true);
            resp.setSmtpHost(mailInfo.getSmtpHost());
            resp.setSmtpPort(mailInfo.getSmtpPort());
            resp.setSmtpSecurity(mailInfo.getSmtpSecurity());
            resp.setSmtpUsername(mailInfo.getSmtpUsername());
            resp.setSmtpPassword(mailInfo.getSmtpPassword());
            resp.setFromAddress(mailInfo.getFromAddress());
        }

        // Read LDAP configuration
        LdapSettingResp ldapSettingResp = ldapComponent.readLdapConfig();
        if (ldapSettingResp.getLdapEnabled() != null) {
            resp.setLdapAttributeEmail(ldapSettingResp.getLdapAttributeEmail());
            resp.setLdapAttributeFirstName(ldapSettingResp.getLdapAttributeFirstName());
            resp.setLdapAttributeLastName(ldapSettingResp.getLdapAttributeLastName());
            resp.setLdapBindDn(ldapSettingResp.getLdapBindDn());
            resp.setLdapEnabled(ldapSettingResp.getLdapEnabled());
            resp.setLdapGroupBase(ldapSettingResp.getLdapGroupBase());
            resp.setLdapGroupMappings(ldapSettingResp.getLdapGroupMappings());
            resp.setLdapGroupSync(ldapSettingResp.getLdapGroupSync());
            resp.setLdapHost(ldapSettingResp.getLdapHost());
            resp.setLdapPassword(ldapSettingResp.getLdapPassword());
            resp.setLdapPort(ldapSettingResp.getLdapPort());
            resp.setLdapSecurity(ldapSettingResp.getLdapSecurity());
            resp.setLdapUserBase(ldapSettingResp.getLdapUserBase());
            resp.setLdapUserFilter(ldapSettingResp.getLdapUserFilter());
        }

        // Read additional information
        // authentication type
        SettingEntity authType = settingComponent.readSetting(ConfigConstant.AUTH_TYPE_KEY);
        if (authType != null) {
            resp.setAuthType(authType.getValue());
        }

        // What is the current step to system initialization
        SettingEntity initStep = settingComponent.readSetting(ConfigConstant.INIT_STEP_KEY);
        if (initStep != null) {
            resp.setInitStep(Integer.parseInt(initStep.getValue()));
        }

        // site-url
        SettingEntity siteUrl = settingComponent.readSetting(ConfigConstant.SITE_URL_KEY);
        if (siteUrl != null) {
            resp.setSiteUrl(siteUrl.getValue());
        }

        // site-locale
        SettingEntity siteLocale = settingComponent.readSetting(ConfigConstant.SITE_LOCALE_KEY);
        if (siteLocale != null) {
            resp.setSiteLocale(siteLocale.getValue());
        }

        // enable-public-sharing
        SettingEntity sharing = settingComponent.readSetting(ConfigConstant.ENABLE_PUBLIC_KEY);
        if (sharing != null && !StringUtils.isEmpty(sharing.getValue())) {
            resp.setEnablePublicSharing(Boolean.parseBoolean(sharing.getValue()));
        }

        // custom-formatting
        SettingEntity custom = settingComponent.readSetting(ConfigConstant.CUSTOM_FORMATTING_KEY);
        if (custom != null && !StringUtils.isEmpty(custom.getValue())) {
            resp.setCustomFormatting(JSON.parse(custom.getValue()));
        }

        // If you are an administrator in a space, you need to read the space configuration information
        if (user.getClusterId() > 0) {
            long clusterId = user.getClusterId();
            // enable-query-caching
            StudioSettingEntity enableQueryCache =
                    settingComponent.readAdminSetting(clusterId, ConfigConstant.ENABLE_QUERY_CACHING);
            if (enableQueryCache != null && !StringUtils.isEmpty(enableQueryCache.getValue())) {
                resp.setEnableQueryCaching(Boolean.parseBoolean(enableQueryCache.getValue()));
            }

            // query-caching-ttl-ratio
            StudioSettingEntity queryCacheTtlRatio =
                    settingComponent.readAdminSetting(clusterId, ConfigConstant.QUERY_CACHING_TTL_TATIO);
            if (queryCacheTtlRatio != null && !StringUtils.isEmpty(queryCacheTtlRatio.getValue())) {
                resp.setQueryCachingTtlRatio(Integer.parseInt(queryCacheTtlRatio.getValue()));
            }

            // query-caching-min-ttl
            StudioSettingEntity queryCacheMinTtl =
                    settingComponent.readAdminSetting(clusterId, ConfigConstant.QUERY_CACHING_MIN_TTL);
            if (queryCacheMinTtl != null && !StringUtils.isEmpty(queryCacheMinTtl.getValue())) {
                resp.setQueryCachingMinTtl(Integer.parseInt(queryCacheMinTtl.getValue()));
            }

            // query-caching-max-ttl
            StudioSettingEntity queryCacheMaxTtl =
                    settingComponent.readAdminSetting(clusterId, ConfigConstant.QUERY_CACHING_MAX_TTL);
            if (queryCacheMaxTtl != null && !StringUtils.isEmpty(queryCacheMaxTtl.getValue())) {
                resp.setQueryCachingMaxTtl(Integer.parseInt(queryCacheMaxTtl.getValue()));
            }

            // query-caching-max-kb
            StudioSettingEntity queryCacheMaxKb =
                    settingComponent.readAdminSetting(clusterId, ConfigConstant.QUERY_CACHING_MAX_KB);
            if (queryCacheMaxKb != null && !StringUtils.isEmpty(queryCacheMaxKb.getValue())) {
                resp.setQueryCachingMaxKb(Integer.parseInt(queryCacheMaxKb.getValue()));
            }
        }

        return resp;
    }
}
