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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrometheusValueResp {
    private String status;

    private PrometheusData data;

    @lombok.Data
    public static class PrometheusData {
        private String resultType;

        private List<Result> result;
    }

    @lombok.Data
    public static class Result {
        private Map<String, String> metric;

        private List<String> value;

        public String getInstance() {
            return metric.get("instance");
        }

        public String getJob() {
            return metric.get("job");
        }

        public String getGroup() {
            return metric.get("group");
        }
    }
}
