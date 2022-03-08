package org.apache.doris.stack.model.request.permission;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PermissionMembershipReq {
    private Integer userId;

    private Integer groupId;

    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public Integer getUserId() {
        return userId;
    }

    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @JSONField(name = "group_id")
    @JsonProperty("group_id")
    public Integer getGroupId() {
        return groupId;
    }

    @JSONField(name = "group_id")
    @JsonProperty("group_id")
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}
