package org.apache.doris.manager.common.heartbeat.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceInstallEventConfigInfo {
    private String moduleName;

    // Here is the user configured installation path,such as /root/doris
    // The actual instance installation path is /root/doris/fe or /root/doris/be or /root/doris/broker
    private String installInfo;

    private String packageDir;

    // If it is a Observer node of Fe, it is not empty here. If it is a Follower node, it is empty
    private String followerEndpoint;

    private Map<String, String> parms = new HashMap<>();

    public void addParm(String key, String value) {
        parms.put(key, value);
    }
}
