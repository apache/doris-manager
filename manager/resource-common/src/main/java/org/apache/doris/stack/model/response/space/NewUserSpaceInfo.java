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

package org.apache.doris.stack.model.response.space;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewUserSpaceInfo {
    private long id;

    private String name;

    private String creator;

    private String description;

    private List<SpaceAdminUserInfo> spaceAdminUser;

    private List<Integer> spaceAdminUserId;

    private String paloAddress;

    private int httpPort;

    private int queryPort;

    private String paloAdminUser;

    private Timestamp createTime;

    private Timestamp updateTime;

    /**
     * Space administrator user group ID
     */
    private int adminGroupId;

    /**
     * Space default all user group ID
     */
    private int allUserGroupId;

    /**
     * Root collection ID corresponding to space
     */
    private int publicCollectionId;

    private String status;

    private long requestId;

    private String requestType;

    private boolean isRequestCompleted;

    private int eventType;

    private Object requestInfo;

    @JsonProperty("describe")
    @JSONField(name = "describe")
    public String getDescription() {
        return description;
    }

    @JsonProperty("describe")
    @JSONField(name = "describe")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("address")
    @JSONField(name = "address")
    public String getPaloAddress() {
        return paloAddress;
    }

    @JsonProperty("address")
    @JSONField(name = "address")
    public void setPaloAddress(String paloAddress) {
        this.paloAddress = paloAddress;
    }

    @JsonProperty("user")
    @JSONField(name = "user")
    public String getPaloAdminUser() {
        return paloAdminUser;
    }

    @JsonProperty("user")
    @JSONField(name = "user")
    public void setPaloAdminUser(String paloAdminUser) {
        this.paloAdminUser = paloAdminUser;
    }

    @Data
    public static class SpaceAdminUserInfo {
        private int id;

        private String name;

        private String email;
    }
}
