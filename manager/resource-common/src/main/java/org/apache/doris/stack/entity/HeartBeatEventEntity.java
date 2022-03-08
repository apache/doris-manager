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

package org.apache.doris.stack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "heart_beat_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartBeatEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "request_id")
    private long requestId;

    // HeartBeatEventType
    @Column(name = "type", length = 30)
    private String type;

    // HeartBeatEventResultType
    @Column(name = "status", length = 30)
    private String status;

    // AgentInstallEventStage/InstanceDeployEventStage etc.
    @Column(name = "stage")
    private int stage;

    @Column(name = "is_completed")
    private boolean completed;

    @Column(name = "operate_result", columnDefinition = "TEXT")
    private String operateResult;

    // timestamp
    @Column(name = "create_timestamp")
    private Timestamp createTimestamp;

    @Column(name = "completed_timestamp")
    private Timestamp completedTimestamp;

    // The configuration json information required to complete this event
    @Column(name = "config_info", columnDefinition = "TEXT")
    private String configInfo;

    public HeartBeatEventEntity(String type, String status, String configInfo, long requestId) {
        this.type = type;
        this.status = status;
        this.createTimestamp = new Timestamp(System.currentTimeMillis());
        this.requestId = requestId;
        this.configInfo = configInfo;
        this.stage = 1;
    }

}
