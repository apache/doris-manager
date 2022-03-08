package org.apache.doris.stack.model.request.permission;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BatchPermissionMembershipReq {
    private List<Integer> userIds;

    private Integer groupId;

    @JSONField(name = "user_ids")
    @JsonProperty("user_ids")
    public List<Integer> getUserIds() {
        return userIds;
    }

    @JSONField(name = "user_ids")
    @JsonProperty("user_ids")
    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
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
