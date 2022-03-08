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
import org.apache.doris.stack.control.ModelControlLevel;
import org.apache.doris.stack.control.ModelControlRequestType;
import org.apache.doris.stack.control.ModelControlStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "model_control_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelControlRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "[user]")
    private String user;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_level", length = 30)
    private ModelControlLevel modelLevel;

    // Model ID corresponding to model level (such as resource cluster ID, Doris application cluster ID, etc.)
    @Column(name = "model_id")
    private long modelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", length = 30)
    private ModelControlRequestType requestType;

    // Event type of the request being processed
    @Column(name = "current_event_type")
    private int currentEventType;

    @Column(name = "completed")
    private boolean completed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ModelControlStatus status;

    // timestamp
    @Column(name = "create_timestamp")
    private Timestamp createTimestamp;

    @Column(name = "completed_timestamp")
    private Timestamp completedTimestamp;

    // Request content cache
    @Column(name = "request_info", columnDefinition = "TEXT")
    private String requestInfo;

    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    public ModelControlRequestEntity(ModelControlLevel modelLevel, long modelId,
                                     ModelControlRequestType requestType, String userName) {
        this.modelLevel = modelLevel;
        this.modelId = modelId;
        this.createTimestamp = new Timestamp(System.currentTimeMillis());
        this.requestType = requestType;
        this.user = userName;
    }
}
