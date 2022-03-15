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

package org.apache.doris.stack.model.response.user;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.doris.stack.model.request.config.InitStudioReq;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserInfo {
    private String email;

    private boolean ldapAuth;

    private String name;

    private String locale;

    private Timestamp lastLogin;

    private boolean isActive;

    private boolean isQbnewb;

    private Timestamp updatedAt;

    // Todo: this field will also be deleted later
    private List<Integer> groupIds;

    private InitStudioReq.AuthType authType;

    /**
     * Is it a space administrator
     */
    private Boolean isAdmin;

    /**
     * Is it a super admin
     */
    private boolean isSuperAdmin;

    private Boolean isSpaceUser;

    private String loginAttributes;

    private int id;

    private Timestamp dateJoined;

    private String commonName;

    private boolean googleAuth;

    /**
     * current User's space ID
     */
    private Long spaceId;

    private String spaceName;

    private Integer collectionId;

    /**
     * TODO:This field is unnecessary and will be deleted later
     * Whether the user's spatial information is complete (i.e. whether the Doris cluster information is complete.
     * If it is incomplete, it needs to be improved, otherwise the cluster is unavailable)
     */
    private Boolean spaceComplete = true;

    private String deployType;

    private boolean managerEnable;

    @JSONField(name = "ldap_auth")
    @JsonProperty("ldap_auth")
    public boolean isLdapAuth() {
        return ldapAuth;
    }

    @JsonProperty("ldap_auth")
    @JSONField(name = "ldap_auth")
    public void setLdapAuth(boolean ldapAuth) {
        this.ldapAuth = ldapAuth;
    }

    @JsonProperty("last_login")
    @JSONField(name = "last_login")
    public Timestamp getLastLogin() {
        return lastLogin;
    }

    @JsonProperty("last_login")
    @JSONField(name = "last_login")
    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    @JsonProperty("is_active")
    @JSONField(name = "is_active")
    public boolean isActive() {
        return isActive;
    }

    @JsonProperty("is_active")
    @JSONField(name = "is_active")
    public void setActive(boolean active) {
        isActive = active;
    }

    @JsonProperty("is_qbnewb")
    @JSONField(name = "is_qbnewb")
    public boolean isQbnewb() {
        return isQbnewb;
    }

    @JsonProperty("is_qbnewb")
    @JSONField(name = "is_qbnewb")
    public void setQbnewb(boolean qbnewb) {
        isQbnewb = qbnewb;
    }

    @JsonProperty("updated_at")
    @JSONField(name = "updated_at")
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    @JSONField(name = "updated_at")
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("group_ids")
    @JSONField(name = "group_ids")
    public List<Integer> getGroupIds() {
        return groupIds;
    }

    @JsonProperty("group_ids")
    @JSONField(name = "group_ids")
    public void setGroupIds(List<Integer> groupIds) {
        this.groupIds = groupIds;
    }

    @JsonProperty("is_admin")
    @JSONField(name = "is_admin")
    public Boolean isAdmin() {
        return isAdmin;
    }

    @JsonProperty("is_admin")
    @JSONField(name = "is_admin")
    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    @JsonProperty("is_space_user")
    @JSONField(name = "is_space_user")
    public Boolean getSpaceUser() {
        return isSpaceUser;
    }

    @JsonProperty("is_space_user")
    @JSONField(name = "is_space_user")
    public void setSpaceUser(Boolean spaceUser) {
        isSpaceUser = spaceUser;
    }

    @JsonProperty("is_super_admin")
    @JSONField(name = "is_super_admin")
    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    @JsonProperty("is_super_admin")
    @JSONField(name = "is_super_admin")
    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    @JsonProperty("login_attributes")
    @JSONField(name = "login_attributes")
    public String getLoginAttributes() {
        return loginAttributes;
    }

    @JsonProperty("login_attributes")
    @JSONField(name = "login_attributes")
    public void setLoginAttributes(String loginAttributes) {
        this.loginAttributes = loginAttributes;
    }

    @JsonProperty("date_joined")
    @JSONField(name = "date_joined")
    public Timestamp getDateJoined() {
        return dateJoined;
    }

    @JsonProperty("date_joined")
    @JSONField(name = "date_joined")
    public void setDateJoined(Timestamp dateJoined) {
        this.dateJoined = dateJoined;
    }

    @JsonProperty("common_name")
    @JSONField(name = "common_name")
    public String getCommonName() {
        return commonName;
    }

    @JsonProperty("common_name")
    @JSONField(name = "common_name")
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    @JsonProperty("google_auth")
    @JSONField(name = "google_auth")
    public boolean isGoogleAuth() {
        return googleAuth;
    }

    @JsonProperty("google_auth")
    @JSONField(name = "google_auth")
    public void setGoogleAuth(boolean googleAuth) {
        this.googleAuth = googleAuth;
    }

    @JsonProperty("space_id")
    @JSONField(name = "space_id")
    public Long getSpaceId() {
        return spaceId;
    }

    @JsonProperty("space_id")
    @JSONField(name = "space_id")
    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    @JsonProperty("space_name")
    @JSONField(name = "space_name")
    public String getSpaceName() {
        return spaceName;
    }

    @JsonProperty("space_name")
    @JSONField(name = "space_name")
    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    @JsonProperty("space_complete")
    @JSONField(name = "space_complete")
    public Boolean getSpaceComplete() {
        return spaceComplete;
    }

    @JsonProperty("space_complete")
    @JSONField(name = "space_complete")
    public void setSpaceComplete(Boolean spaceComplete) {
        this.spaceComplete = spaceComplete;
    }

    @JsonProperty("deploy_type")
    @JSONField(name = "deploy_type")
    public String getDeployType() {
        return deployType;
    }

    @JsonProperty("deploy_type")
    @JSONField(name = "deploy_type")
    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    @JsonProperty("manager_enable")
    @JSONField(name = "manager_enable")
    public boolean isManagerEnable() {
        return managerEnable;
    }

    @JsonProperty("manager_enable")
    @JSONField(name = "manager_enable")
    public void setManagerEnable(boolean managerEnable) {
        this.managerEnable = managerEnable;
    }
}
