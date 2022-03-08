package org.apache.doris.stack.model.request.permission;

import lombok.Data;
import org.apache.doris.stack.util.StringUtil;

@Data
public class PermissionGroupAddReq {
    private String name;

    /**
     * check empty field
     * @return boolean
     */
    public boolean hasEmptyField() {
        if (StringUtil.trimStrEmpty(this.name)) {
            return true;
        }
        this.name = this.name.trim();
        return false;
    }
}
