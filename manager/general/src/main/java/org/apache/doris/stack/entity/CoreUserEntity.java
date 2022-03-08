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

package org.apache.doris.stack.entity;

import org.apache.doris.stack.model.ldap.LdapUserInfo;
import org.apache.doris.stack.model.request.user.NewUserAddReq;
import org.apache.doris.stack.model.request.user.UserAddReq;
import org.apache.doris.stack.model.response.space.NewUserSpaceInfo;
import org.apache.doris.stack.model.response.user.UserInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "core_user")
@Data
@NoArgsConstructor
public class CoreUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 254)
    private String email;

    @Column(length = 254, name = "first_name", nullable = false)
    private String firstName;

    @Column(length = 254, name = "last_name", nullable = false)
    private String lastName = "";

    @Column(length = 254, name = "password", nullable = false)
    private String password;

    @Column(length = 254, name = "password_salt", nullable = false)
    private String passwordSalt;

    @Column(name = "date_joined", nullable = false)
    private Timestamp dateJoined;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    @Column(name = "is_superuser", nullable = false)
    private boolean isSuperuser;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(length = 254, name = "reset_token")
    private String resetToken;

    @Column(name = "reset_triggered")
    private Long resetTriggered;

    @Column(name = "is_qbnewb", nullable = false)
    private boolean isQbnewb;

    @Column(name = "google_auth", nullable = false)
    private boolean googleAuth;

    @Column(name = "ldap_auth", nullable = false)
    private boolean ldapAuth;

    @Column(name = "idaas_auth", nullable = false)
    private boolean idaasAuth;

    @Column(columnDefinition = "TEXT", name = "login_attributes")
    private String loginAttributes;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(length = 254, name = "sso_source")
    private String ssoSource;

    @Column(length = 5, name = "locale")
    private String locale;

    @Column(name = "cluster_id")
    private Integer clusterId = 0;

    @Column(name = "is_cluster_admin")
    private Boolean isClusterAdmin = false;

    @Column(length = 254, name = "entry_UUID")
    private String entryUUID;

    public CoreUserEntity(UserAddReq userAddReq) {
        this.email = userAddReq.getEmail();
        this.firstName = userAddReq.getName();
        this.lastName = "palo";
        this.dateJoined = new Timestamp(System.currentTimeMillis());
        this.isSuperuser = false;
        this.isActive = true;
        this.isQbnewb = true;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public CoreUserEntity(NewUserAddReq userAddReq) {
        if (userAddReq.getEmail() == null) {
            this.email = "";
        } else {
            this.email = userAddReq.getEmail();
        }
        this.firstName = userAddReq.getName();
        this.lastName = "palo";
        this.dateJoined = new Timestamp(System.currentTimeMillis());
        this.isSuperuser = false;
        this.isActive = true;
        this.isQbnewb = true;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public CoreUserEntity(UserAddReq userAddReq, LdapUserInfo userInfo) {
        this.email = userAddReq.getEmail();
        this.firstName = userInfo.getLastName() + userInfo.getFirstName();
        this.lastName = "palo";
        this.dateJoined = new Timestamp(System.currentTimeMillis());
        this.isSuperuser = false;
        this.isActive = true;
        this.isQbnewb = true;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
        this.password = userInfo.getPassword();
        this.ldapAuth = true;
    }

    /**
     * @param userEntity
     */
    public CoreUserEntity(CoreUserEntity userEntity) {
        this.id = userEntity.getId();
        this.email = userEntity.getEmail();
        this.firstName = userEntity.getFirstName();
        this.lastName = userEntity.getLastName();
        this.password = userEntity.getPassword();
        this.passwordSalt = userEntity.getPasswordSalt();
        this.dateJoined = new Timestamp(System.currentTimeMillis());
        this.lastLogin = userEntity.getLastLogin();
        this.isSuperuser = false;
        this.isActive = true;
        this.resetToken = userEntity.getResetToken();
        this.resetTriggered = userEntity.getResetTriggered();
        this.isQbnewb = true;
        this.googleAuth = false;
        this.ldapAuth = true;
        this.loginAttributes = userEntity.getLoginAttributes();
        this.updatedAt = new Timestamp(System.currentTimeMillis());
        this.ssoSource = userEntity.getSsoSource();
        this.locale = userEntity.getLocale();
        this.clusterId = userEntity.getClusterId();
        this.isClusterAdmin = userEntity.getIsClusterAdmin();
    }

    public UserInfo castToUserInfoWithoutClusterInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(this.email);
        userInfo.setLdapAuth(this.ldapAuth);
        userInfo.setName(this.firstName);
        userInfo.setLocale(this.locale);
        userInfo.setLastLogin(this.lastLogin);
        userInfo.setActive(this.isActive);
        userInfo.setQbnewb(this.isQbnewb);
        userInfo.setUpdatedAt(this.updatedAt);
        userInfo.setSuperAdmin(this.isSuperuser);
        userInfo.setLoginAttributes(this.loginAttributes);
        userInfo.setId(this.id);
        userInfo.setDateJoined(this.dateJoined);
        userInfo.setCommonName(this.firstName);
        userInfo.setGoogleAuth(this.googleAuth);
        return userInfo;
    }

    public UserInfo castToUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(this.email);
        userInfo.setLdapAuth(this.ldapAuth);
        userInfo.setName(this.firstName);
        userInfo.setLocale(this.locale);
        userInfo.setLastLogin(this.lastLogin);
        userInfo.setActive(this.isActive);
        userInfo.setQbnewb(this.isQbnewb);
        userInfo.setUpdatedAt(this.updatedAt);
        userInfo.setSuperAdmin(this.isSuperuser);
        userInfo.setLoginAttributes(this.loginAttributes);
        userInfo.setId(this.id);
        userInfo.setDateJoined(this.dateJoined);
        userInfo.setCommonName(this.firstName);
        userInfo.setGoogleAuth(this.googleAuth);

        if (this.clusterId == null) {
            userInfo.setSpaceId(0);
        } else {
            userInfo.setSpaceId(this.clusterId);
        }

        if (this.isClusterAdmin == null) {
            userInfo.setAdmin(false);
        } else {
            userInfo.setAdmin(this.isClusterAdmin);
        }
        return userInfo;
    }

    public UserInfo castToSimpleUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setName(this.firstName);
        userInfo.setId(this.id);
        return userInfo;
    }

    public NewUserSpaceInfo.SpaceAdminUserInfo castToAdminUserInfo() {
        NewUserSpaceInfo.SpaceAdminUserInfo userInfo = new NewUserSpaceInfo.SpaceAdminUserInfo();
        userInfo.setId(this.id);
        userInfo.setName(this.firstName);
        userInfo.setEmail(this.email);
        return userInfo;
    }

    public boolean getLdapAuth() {
        return ldapAuth;
    }

    public void setLdapAuth(boolean ldapAuth) {
        this.ldapAuth = ldapAuth;
    }

    public Integer getClusterId() {
        if (clusterId == null) {
            return 0;
        }
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        if (clusterId == null) {
            this.clusterId = 0;
        } else {
            this.clusterId = clusterId;
        }
    }

    public Boolean getClusterAdmin() {
        if (isClusterAdmin == null) {
            return false;
        }
        return isClusterAdmin;
    }

    public void setIsClusterAdmin(Boolean clusterAdmin) {
        if (clusterAdmin == null) {
            this.isClusterAdmin = false;
        } else {
            isClusterAdmin = clusterAdmin;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof CoreUserEntity) {
            CoreUserEntity other = (CoreUserEntity) obj;
            //需要比较的字段相等，则这两个对象相等
            if (this.entryUUID.equals(other.entryUUID)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        return result;
    }
}
