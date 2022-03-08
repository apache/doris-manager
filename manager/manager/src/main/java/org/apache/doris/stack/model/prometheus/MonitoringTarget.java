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

package org.apache.doris.stack.model.prometheus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MonitoringTarget {
    @JSONField(serialize = false)
    private int hashCode;

    private List<String> targets;

    private Labels labels;

    public MonitoringTarget(List<String> targets, Labels labels) {
        this.targets = targets;
        this.labels = labels;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MonitoringTarget)) {
            return false;
        }
        MonitoringTarget other = (MonitoringTarget) o;
        return labels.getJob().equals(other.getLabels().getJob())
                && labels.getGroup().equals(other.getLabels().getGroup());
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = labels.getJob().hashCode();
            result = 31 * result + labels.getGroup().hashCode();
            hashCode = result;
        }
        return result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Labels {
        @JsonProperty(required = true)
        private String job;

        @JsonProperty(required = true)
        private String group;
    }
}
