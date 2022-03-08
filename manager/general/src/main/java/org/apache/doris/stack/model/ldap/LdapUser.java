package org.apache.doris.stack.model.ldap;

import lombok.Data;

@Data
public class LdapUser {
    private String dn;
    private String email;
    private String lastName;
    private String entryUUID;
    private String firstName;
}
