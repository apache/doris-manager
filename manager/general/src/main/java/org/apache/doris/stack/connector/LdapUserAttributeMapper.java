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

package org.apache.doris.stack.connector;

import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.exception.BadRequestException;
import org.apache.doris.stack.model.request.config.LdapSettingReq;
import org.springframework.ldap.core.AttributesMapper;

import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

/**
 * ldap user mapper
 */
public class LdapUserAttributeMapper implements AttributesMapper {

    private LdapSettingReq ldapSettingReq;
    private static boolean flag = false;
    public LdapUserAttributeMapper(LdapSettingReq ldapSettingReq) {
        this.ldapSettingReq = ldapSettingReq;
    }

    /**
     *
     * @param attrs
     * @return
     * @throws NamingException
     */
    public Object mapFromAttributes(Attributes attrs) throws NamingException {

        CoreUserEntity user  = new CoreUserEntity();

        // only check once
        if (!flag) {
            Set<String> ldapAttributes = new HashSet<>();
            NamingEnumeration<? extends Attribute> all = attrs.getAll();
            while (all.hasMore()) {
                Attribute next = all.next();
                ldapAttributes.add(next.getID());
            }
            if (!ldapAttributes.contains(ldapSettingReq.getLdapAttributeFirstName())) {
                throw new BadRequestException(String.format("输入属性%s不存在", ldapSettingReq.getLdapAttributeFirstName()));
            }
            if (!ldapAttributes.contains(ldapSettingReq.getLdapAttributeLastName())) {
                throw new BadRequestException(String.format("输入属性%s不存在", ldapSettingReq.getLdapAttributeLastName()));
            }
            flag = true;
        }

        // set uid as our firstName,in ldap first name is givenName
        if (attrs.get(ldapSettingReq.getLdapAttributeFirstName()) != null) {
            user.setFirstName(attrs.get(ldapSettingReq.getLdapAttributeFirstName()).get().toString());
        } else {
            user.setFirstName("");
        }

        // mail is not required
        if (attrs.get(ldapSettingReq.getLdapAttributeEmail()) != null) {
            user.setEmail(attrs.get(ldapSettingReq.getLdapAttributeEmail()).get().toString());
        } else {
            user.setEmail("");
        }

        // last name is sn
        if (attrs.get(ldapSettingReq.getLdapAttributeLastName()) != null) {
            user.setLastName(attrs.get(ldapSettingReq.getLdapAttributeLastName()).get().toString());

        } else {
            user.setLastName("");
        }

        if (attrs.get("entryUUID") != null) {
            user.setEntryUUID(attrs.get("entryUUID").get().toString());
        } else {
            user.setEntryUUID("");
        }

        return user;
    }
}
