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

import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.doris.stack.model.request.space.ClusterCreateReq;
import org.apache.doris.stack.model.request.space.ClusterType;
import org.apache.doris.stack.model.response.space.NewUserSpaceInfo;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Description：Doris cluster app space information（app cluster info）
 */
@Entity
@Table(name = "cluster_info")
@Data
@NoArgsConstructor
public class ClusterInfoEntity {
    // TODO:The ID type has changed from int to long. Part of the data associated with
    //  this ID also needs to be modified and completed later
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Resource cluster ID
    @Column(name = "resource_cluster_id")
    private Long resourceClusterId;

    // Doris application deploy type
    @Column(name = "deploy_type", length = 50)
    private String deployType = AppDeployType.PRIVATE.name();

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "creator")
    private String creator;

    private String address;

    private int httpPort;

    private int queryPort;

    /**
     * Admin user of Doris cluster
     */
    @Column(name = "[user]", length = 100)
    private String user;

    /**
     * Doris user password
     * TODO:The subsequent storage shall be encrypted to prevent the leakage of password information
     */
    @Column(length = 100)
    private String passwd;

    @Column(length = 50)
    private String sessionId;

    private Timestamp createTime;

    private Timestamp updateTime;

    /**
     * Broker name information for file import
     */
    private String brokerName;

    /**
     * Description information
     */
    private String description;

    private int adminUserId;

    private String adminUserMail;

    private int adminGroupId;

    private int allUserGroupId;

    private int collectionId;

    private boolean isActive = true;

    /**
     * Engine type（Doris/Mysql/Dae）
     */
    private String type;

    @Column(name = "timezone", length = 254)
    private String timezone;

    private boolean managerEnable;

    /**
     * json format install information for doris application
     * TODO：It will only be used when subsequent physical clusters
     *  support the deployment of multiple Doris application instances
     */
    @Column(name = "install_info", columnDefinition = "TEXT")
    private String installInfo;

    // Doris application target version info(TODO:Reserved version management fields)
    @Column(name = "desired_template_id")
    private Long desiredTemplateId;

    // Doris application current version info(TODO:Reserved version management fields)
    @Column(name = "current_template_id")
    private Long currentTemplateId;

    // Doris cluster target state(ModelControlState)
    @Column(name = "desired_state")
    private Integer desiredState;

    // Doris cluster current state(ModelControlState)
    @Column(name = "current_state")
    private Integer currentState;

    // Doris application service status, TODO:Follow up supplement according to product design
    @Column(name = "status", length = 50)
    private String status = AppClusterStatus.UNKNOWN.name();

    // The application creation source is compatible with Doris deployed by the old Baidu cloud
    @Column(name = "origin_info", columnDefinition = "TEXT")
    private String originInfo;

    // app other information, such as creation information and deployment information,
    // is determined according to the actual deployment method
    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    public void updateByClusterInfo(ClusterCreateReq createReq) {
        this.address = createReq.getAddress();
        this.httpPort = createReq.getHttpPort();
        this.queryPort = createReq.getQueryPort();
        this.user = createReq.getUser();
        this.passwd = createReq.getPasswd();
        this.updateTime = new Timestamp(System.currentTimeMillis());
        if (createReq.getType() == null) {
            this.type = ClusterType.Doris.name();
        } else {
            this.type = createReq.getType().name();
        }
    }

    public NewUserSpaceInfo transToNewModel() {
        NewUserSpaceInfo userSpaceInfo = new NewUserSpaceInfo();
        userSpaceInfo.setId((int) this.id);
        userSpaceInfo.setName(this.name);
        userSpaceInfo.setDescription(this.description);
        userSpaceInfo.setPaloAddress(this.address);
        userSpaceInfo.setHttpPort(this.httpPort);
        userSpaceInfo.setQueryPort(this.queryPort);
        userSpaceInfo.setPaloAdminUser(this.user);
        userSpaceInfo.setUpdateTime(this.updateTime);
        userSpaceInfo.setCreateTime(this.createTime);
        userSpaceInfo.setAllUserGroupId(this.allUserGroupId);
        userSpaceInfo.setAdminGroupId(this.adminGroupId);
        userSpaceInfo.setPublicCollectionId(this.collectionId);
        userSpaceInfo.setStatus(this.status);
        userSpaceInfo.setCreator(this.creator);
        return userSpaceInfo;
    }

    public String getPrometheusJobName() {
        return "cluster_" + id;
    }

    public enum AppDeployType {
        CLOUD, // Public cloud deployment
        PRIVATE // Privatize local deployment
    }

    public enum AppClusterStatus {
        STOPPED,
        NORMAL,
        ABNORMAL,
        WARNING,
        UNKNOWN
    }

    public Long getResourceClusterId() {
        if (resourceClusterId == null) {
            return 0L;
        }
        return resourceClusterId;
    }

    public void setResourceClusterId(Long resourceClusterId) {
        this.resourceClusterId = resourceClusterId;
    }

    public Long getDesiredTemplateId() {
        if (desiredTemplateId == null) {
            return 0L;
        }
        return desiredTemplateId;
    }

    public void setDesiredTemplateId(Long desiredTemplateId) {
        this.desiredTemplateId = desiredTemplateId;
    }

    public Long getCurrentTemplateId() {
        if (currentTemplateId == null) {
            return 0L;
        }
        return currentTemplateId;
    }

    public void setCurrentTemplateId(Long currentTemplateId) {
        this.currentTemplateId = currentTemplateId;
    }

    public Integer getDesiredState() {
        if (desiredState == null) {
            return 0;
        }
        return desiredState;
    }

    public void setDesiredState(Integer desiredState) {
        this.desiredState = desiredState;
    }

    public Integer getCurrentState() {
        if (currentState == null) {
            return 0;
        }
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }
}
