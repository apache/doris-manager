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
