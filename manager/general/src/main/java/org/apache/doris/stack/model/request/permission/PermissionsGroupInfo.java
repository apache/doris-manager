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

package org.apache.doris.stack.model.request.permission;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.doris.stack.model.response.user.GroupMember;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PermissionsGroupInfo {
    private int id;

    private String name;

    private Integer memberCount;

    private List<GroupMember> members;

    public PermissionsGroupInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @JSONField(name = "member_count")
    @JsonProperty("member_count")
    public Integer getMemberCount() {
        return memberCount;
    }

    @JSONField(name = "member_count")
    @JsonProperty("member_count")
    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

}
