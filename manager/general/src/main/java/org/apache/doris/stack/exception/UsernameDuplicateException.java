package org.apache.doris.stack.exception;

public class UsernameDuplicateException extends Exception {
    public static final String MESSAGE = "版本升级涉及用户名兼容性问题，需要及时修改用户名，请先使用邮箱登录。";

    public UsernameDuplicateException() {
        super(MESSAGE);
    }
}
