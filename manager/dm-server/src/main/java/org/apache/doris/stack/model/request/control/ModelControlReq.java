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
