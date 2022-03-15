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

package org.apache.doris.stack.service;

import static org.apache.doris.stack.constant.ConstantDef.EMAIL_REGEX;
import static org.apache.doris.stack.service.user.AuthenticationService.failedLoginMap;

import com.google.common.collect.Lists;
import org.apache.doris.stack.constant.ConstantDef;
import org.apache.doris.stack.exception.InputLengthException;
import org.apache.doris.stack.exception.PasswordWeakException;
import org.apache.doris.stack.util.CredsUtil;
import org.apache.doris.stack.util.UuidUtil;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.exception.InputFormatException;
import org.apache.doris.stack.exception.PasswordFormatException;
import org.apache.doris.stack.exception.RequestFieldNullException;
import org.apache.doris.stack.exception.UserDisabledException;
import org.apache.doris.stack.exception.UserLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UtilService extends BaseService {

    private static final String NUMBERS_REGEX = "[0-9]";

    private static final String UPPERCASE_LETTER_REGEX = "[A-Z]";

    private static final String LOWERCASE_LETTER_REGEX = "[a-z]";

    private static final String UNDERLINE_REGEX = "_";

    @Autowired
    public UtilService() {
    }

    public boolean newPasswordCheck(String passwd) throws Exception {
        log.debug("Check password format.");
        if (passwd == null || passwd.isEmpty()) {
            log.error("The password is empty.");
            throw new RequestFieldNullException();
        }

        int length = passwd.length();
        // Check whether the password meets the minimum count requirements for each character class
        if (length < 6) {
            log.error("Password length is less than 6 or more than 12.");
            throw new PasswordWeakException();
        }

        if (length > 12) {
            log.error("Password length is more than 12.");
            throw new PasswordFormatException();
        }

        int letterTypes = isNotOnlyContainLetter(passwd, NUMBERS_REGEX)
                + isNotOnlyContainLetter(passwd, UPPERCASE_LETTER_REGEX)
                + isNotOnlyContainLetter(passwd, LOWERCASE_LETTER_REGEX)
                + isNotOnlyContainLetter(passwd, UNDERLINE_REGEX);

        // Check that the password contains only one or two character
        if (letterTypes < 3) {
            log.error("The password contains only one or two type letters.");
            throw new PasswordWeakException();
        }

        if (!isOnlyContainLetters(passwd, Lists.newArrayList(NUMBERS_REGEX, UPPERCASE_LETTER_REGEX,
                LOWERCASE_LETTER_REGEX, UNDERLINE_REGEX))) {
            log.error("The password contains unsupported characters.");
            throw new PasswordFormatException();
        }

        log.debug("The password is qualified.");
        return true;
    }

    public boolean userNameCheck(String name) throws Exception {
        log.debug("Check user name format.");
        if (name == null || name.isEmpty()) {
            log.error("The use name is empty.");
            throw new RequestFieldNullException();
        }

        int length = name.length();

        if (length >= 20) {
            log.error("The user name length {} too long.", length);
            throw new InputLengthException();
        }

        if (!isOnlyContainLetters(name, Lists.newArrayList(NUMBERS_REGEX, UPPERCASE_LETTER_REGEX,
                LOWERCASE_LETTER_REGEX))) {
            log.error("The user name contains unsupported characters.");
            throw new InputFormatException();
        }

        log.debug("The user name is qualified.");
        return true;
    }

    /**
     * Judge whether the string contains some characters, but not only such characters
     * @param str, original string
     * @param regex, regular expression of some letter
     * @return 1 is returned if the condition is met, and 0 is returned if the condition is not met
     */
    private int isNotOnlyContainLetter(String str, String regex) {
        int length = str.length();
        int noLetterReplaceLen = str.replaceAll(regex, "").length();

        if (noLetterReplaceLen > 0 && noLetterReplaceLen < length) {
            return 1;
        }
        return 0;
    }

    /**
     * Judge whether the string contains only certain characters and cannot contain other characters
     * @param str
     * @param regexs
     * @return, true is returned if the condition is met, and false is returned if the condition is not met
     */
    private boolean isOnlyContainLetters(String str, List<String> regexs) {
        String result = str;
        for (String regex : regexs) {
            result = result.replaceAll(regex, "");
        }
        return result.isEmpty();
    }

    /**
     * Verify whether the password entered by the user is consistent with that stored in the database
     * @param salt
     * @param inputPasswd,Password plaintext
     * @param hash,Password and ciphertext stored in the database
     * @return
     * @throws Exception
     */
    public boolean verifyPassword(String salt, String inputPasswd, String hash) throws Exception {
        log.debug("Verify password.");
        String password = passwordSalt(salt, inputPasswd);

        boolean passwordCheck = CredsUtil.bcryptVerify(password, hash);
        if (!passwordCheck) {
            log.error("Password error.");
            throw new UserLoginException();
        }
        return true;
    }

    // Verify the user name and password. Because verifypassword is used to reset the password, it will not pass ID
    public boolean verifyLoginPassword(String salt, String inputPasswd, String hash, int userId) throws Exception {
        log.debug("Verify password.");
        String password = passwordSalt(salt, inputPasswd);

        boolean passwordCheck = CredsUtil.bcryptVerify(password, hash);
        if (!passwordCheck) {
            log.error("Password error.");
            List<Long> loginAttempts = failedLoginMap.getOrDefault(userId, new ArrayList<>());
            loginAttempts.add(System.currentTimeMillis());
            failedLoginMap.put(userId, loginAttempts);
            throw new UserLoginException();
        }
        return true;
    }

    /**
     * Check whether the email format is qualified
     * @param email
     * @return
     * @throws Exception
     */
    public boolean emailCheck(String email) throws Exception {
        log.debug("Check email format.");
        if (email == null || email.isEmpty()) {
            log.error("The email is empty.");
            throw new RequestFieldNullException();
        }

        boolean isMatch = email.matches(EMAIL_REGEX);

        if (!isMatch) {
            log.error("The email {} format is error.", email);
            throw new InputFormatException();
        }

        log.debug("The mail format is qualified.");

        return true;
    }

    // check role name
    public void roleNameCheck(String name) throws Exception {
        if (name.length() > 20 || name.equals("空间管理员") || name.equals("空间成员")) {
            throw new InputFormatException();
        }
        for (char ch : name.toCharArray()) {
            if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_' && !(ch >= 19968 && ch <= 171941)) {
                throw new InputFormatException();
            }
        }
    }

    /**
     * Detect whether the user is acitve
     * @param user
     * @throws Exception
     */
    public void checkUserActive(CoreUserEntity user) throws Exception {
        log.debug("Check user {} is active.", user.getId());
        if (!user.isActive()) {
            log.error("The user not active.");
            throw new UserDisabledException();
        }
    }

    public String resetUserToken(CoreUserEntity userEntity, boolean isNew) {
        log.debug("Reset user {} token.", userEntity.getId());
        String resetTokenStr = UuidUtil.newUuid();
        String resetToken = CredsUtil.hashBcrypt(resetTokenStr, -1);

        if (isNew) {
            resetToken = ConstantDef.NEW_TOKEN_MARK + resetToken;
        }

        userEntity.setResetToken(resetToken);
        userEntity.setResetTriggered(System.currentTimeMillis());

        return resetTokenStr;
    }

    public String getUserJoinUrl(int userId, String token) {
        StringBuffer joinUrl = new StringBuffer();
        joinUrl.append(getResetPasswordUrl(userId, token));
        joinUrl.append("#new");
        return joinUrl.toString();
    }

    public String getResetPasswordUrl(int userId, String token) {
        StringBuffer joinUrl = new StringBuffer();
        joinUrl.append("/auth/reset_password/");
        joinUrl.append(userId);
        joinUrl.append(ConstantDef.UNDERLINE);
        joinUrl.append(token);
        return joinUrl.toString();
    }

    public String encryptPassword(String salt, String password) {
        String saltPasswd = passwordSalt(salt, password);
        return CredsUtil.hashBcrypt(saltPasswd, -1);
    }

    public void setPassword(CoreUserEntity userEntity, String password) throws Exception {
        log.debug("update user {} password.", userEntity.getId());
        String salt = UuidUtil.newUuid();
        userEntity.setPasswordSalt(salt);
        String storePassword = encryptPassword(salt, password);
        userEntity.setPassword(storePassword);
        userEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Add a salt value to the password
     * @param salt
     * @param password,
     * @return
     */
    private String passwordSalt(String salt, String password) {
        return salt + password;
    }

}
