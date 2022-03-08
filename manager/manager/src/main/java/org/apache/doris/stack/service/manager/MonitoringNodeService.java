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

package org.apache.doris.stack.service.manager;

import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.connector.DorisNodesClient;
import org.apache.doris.stack.constant.PropertyDefine;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.model.palo.DorisNodes;
import org.apache.doris.stack.model.prometheus.MonitoringTarget;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/*
 * This class is used to register or update the monitoring nodes, of the specified cluster in Prometheus.
 * These monitoring nodes is obtained from the Fe of Doris. And write the monitoring node as JSON to the
 * 'targets.json' file of the Prometheus directory.
 *
 * targets.json:
 * [
 *    {
 *         "labels":{
 *             "group":"fe",
 *             "job":"doris"
 *        },
 *         "targets":[
 *             "10.23.32.32:8030"
 *         ]
 *    },
 *    {
 *         "labels":{
 *             "group":"be",
 *             "job":"doris"
 *        },
 *         "targets":[
 *             "10.23.32.42:8040"
 *         ]
 *    }
 * ]
 */
@Service
@Slf4j
public class MonitoringNodeService {
    public static final String TARGETS_NAME = "targets.json";

    private String prometheusHome;

    @Autowired
    private DorisNodesClient dorisNodesClient;

    @Autowired
    private Environment environment;

    public void updateMonitoringNodes(ClusterInfoEntity clusterInfoEntity) throws Exception {
        initPrometheusHome();
        if (Strings.isNullOrEmpty(prometheusHome)) {
            // This could be a test or development environment.
            log.warn("No Prometheus deployment path found, skipping monitoring node update.");
            return;
        }

        List<MonitoringTarget> targets = loadTargets();
        DorisNodes nodes = dorisNodesClient.getNodes(clusterInfoEntity);
        MonitoringTarget newFeMonitoringTarget = new MonitoringTarget(nodes.getFrontend(),
                new MonitoringTarget.Labels(clusterInfoEntity.getPrometheusJobName(), "fe"));
        updateTargets(newFeMonitoringTarget, targets);

        List<String> beList = nodes.getBackend();
        if (!beList.isEmpty()) {
            MonitoringTarget newBeMonitoringTarget = new MonitoringTarget(beList,
                    new MonitoringTarget.Labels(clusterInfoEntity.getPrometheusJobName(), "be"));
            updateTargets(newBeMonitoringTarget, targets);
        }

        saveTargets(targets);
    }

    public void deleteMonitoringNodes(ClusterInfoEntity clusterInfoEntity) throws Exception {
        initPrometheusHome();
        if (Strings.isNullOrEmpty(prometheusHome)) {
            // This could be a test or development environment.
            log.warn("No Prometheus deployment path found, skipping monitoring node update.");
            return;
        }

        List<MonitoringTarget> targets = loadTargets();
        MonitoringTarget newFeMonitoringTarget = new MonitoringTarget(Lists.newArrayList(),
                new MonitoringTarget.Labels(clusterInfoEntity.getPrometheusJobName(), "fe"));
        targets.remove(newFeMonitoringTarget);
        MonitoringTarget newBeMonitoringTarget = new MonitoringTarget(Lists.newArrayList(),
                new MonitoringTarget.Labels(clusterInfoEntity.getPrometheusJobName(), "be"));
        targets.remove(newBeMonitoringTarget);

        saveTargets(targets);
        log.info("Deleted prometheus monitoring node. job:{}.", clusterInfoEntity.getPrometheusJobName());
    }

    public synchronized void initPrometheusHome() {
        if (Strings.isNullOrEmpty(prometheusHome)) {
            prometheusHome = environment.getProperty(PropertyDefine.PROMETHEUS_HOME_PROPERTY);
        }
    }

    private List<MonitoringTarget> loadTargets() {
        Path file = Paths.get(prometheusHome, TARGETS_NAME);
        try {
            String targets = new String(Files.readAllBytes(file));
            log.info("read monitoring nodes:\n{}", targets);
            List<MonitoringTarget> monitoringTargets = JSON.parseArray(targets, MonitoringTarget.class);
            if (monitoringTargets != null) {
                return monitoringTargets;
            }
        } catch (IOException e) {
            log.error("Read monitoring node config error. File path:{}", file, e);
        }
        return Lists.newArrayList();
    }

    private void saveTargets(List<MonitoringTarget> targets) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(prometheusHome + "/" + TARGETS_NAME))) {
            String targetsJson = JSON.toJSONString(targets, SerializerFeature.PrettyFormat);
            log.info("write monitoring nodes:\n{}", targetsJson);
            out.write(targetsJson);
        }
    }

    private void updateTargets(MonitoringTarget target, List<MonitoringTarget> monitoringTargets) {
        int index = monitoringTargets.indexOf(target);
        if (index != -1) {
            monitoringTargets.set(index, target);
        } else {
            monitoringTargets.add(target);
        }
    }

    // only used for test
    public void setPrometheusHome(String path) {
        prometheusHome = path;
    }
}

