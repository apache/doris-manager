package org.apache.doris.manager.agent.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagerServerResponse {
    private String msg;

    private int code;

    private String data;

    private int count;
}
