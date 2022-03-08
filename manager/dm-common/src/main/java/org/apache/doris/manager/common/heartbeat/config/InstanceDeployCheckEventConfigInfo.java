package org.apache.doris.manager.common.heartbeat.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceDeployCheckEventConfigInfo {
    private String moduleName;

    // Here is the user configured installation path,such as /root/doris
    // The actual instance installation path is /root/doris/fe or /root/doris/be or /root/doris/broker
    private String installInfo;
}
