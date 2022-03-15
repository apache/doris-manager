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

package org.apache.doris.stack.model.request.control;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ModelControlReq implements Serializable {
    private static final long serialVersionUID = 1L;

    private long clusterId;

    private long requestId;

    private int eventType = 1;

    @JSONField(name = "cluster_id")
    @JsonProperty("cluster_id")
    public long getClusterId() {
        return clusterId;
    }

    @JSONField(name = "cluster_id")
    @JsonProperty("cluster_id")
    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    @JSONField(name = "request_id")
    @JsonProperty("request_id")
    public long getRequestId() {
        return requestId;
    }

    @JSONField(name = "request_id")
    @JsonProperty("request_id")
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @JSONField(name = "event_type")
    @JsonProperty("event_type")
    public int getEventType() {
        return eventType;
    }

    @JSONField(name = "event_type")
    @JsonProperty("event_type")
    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
