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

import static org.mockito.Mockito.when;

import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.connector.DorisNodesClient;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.model.palo.DorisNodes;
import org.apache.doris.stack.model.prometheus.MonitoringTarget;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
@Slf4j
public class MonitoringNodeServiceTest {
    private int clusterId;

    private ClusterInfoEntity clusterInfo;

    @InjectMocks
    private MonitoringNodeService monitoringNodeService;

    @Mock
    private DorisNodesClient dorisNodesClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateMonitoringNodes() throws Exception {
        monitoringNodeService.setPrometheusHome("./");
        File targets = new File(MonitoringNodeService.TARGETS_NAME);
        if (targets.exists()) {
            targets.delete();
        }
        targets.createNewFile();

        // Test adding the first cluster
        clusterId = 1;
        List<String> feList1 = Lists.newArrayList("127.0.0.32:8030", "127.0.0.33:8030", "10.23.34.32:8030");
        List<String> beList1 = Lists.newArrayList("127.0.0.42:8040", "127.0.0.43:8040", "10.23.34.42:8040");
        DorisNodes dorisNodes1 = new DorisNodes(feList1, beList1);
        mockMonitoringNodeService(dorisNodes1);

        monitoringNodeService.updateMonitoringNodes(clusterInfo);
        List<MonitoringTarget> monitoringTargets1 = loadTargets();
        Assert.assertEquals(2, monitoringTargets1.size());
        assertTargets(monitoringTargets1, feList1, "fe");
        assertTargets(monitoringTargets1, beList1, "be");

        // Test expanding cluster
        feList1.add("101.23.34.32:8030");
        beList1.add("102.23.34.32:8030");
        monitoringNodeService.updateMonitoringNodes(clusterInfo);
        monitoringTargets1 = loadTargets();
        Assert.assertEquals(2, monitoringTargets1.size());
        assertTargets(monitoringTargets1, feList1, "fe");
        assertTargets(monitoringTargets1, beList1, "be");

        // Test adding another cluster
        clusterId = 2;
        List<String> feList2 = Lists.newArrayList("127.0.2.32:8030", "127.0.2.33:8030", "127.0.3.32:8030");
        List<String> beList2 = Lists.newArrayList("127.0.2.42:8040", "127.0.2.43:8040", "127.0.3.42:8040");
        DorisNodes dorisNodes2 = new DorisNodes(feList2, beList2);
        mockMonitoringNodeService(dorisNodes2);

        monitoringNodeService.updateMonitoringNodes(clusterInfo);
        List<MonitoringTarget> monitoringTargets2 = loadTargets();
        Assert.assertEquals(4, monitoringTargets2.size());
        assertTargets(monitoringTargets2, feList2, "fe");
        assertTargets(monitoringTargets2, beList2, "be");

        targets.delete();
    }

    private void mockMonitoringNodeService(DorisNodes dorisNodes) throws Exception {
        clusterInfo = new ClusterInfoEntity();
        clusterInfo.setId(clusterId);
        clusterInfo.setName("jobName");
        clusterInfo.setAddress("127.0.0.32");
        clusterInfo.setHttpPort(8030);
        clusterInfo.setQueryPort(8031);
        clusterInfo.setUser("admin");
        clusterInfo.setPasswd("1234");

        when(dorisNodesClient.getNodes(clusterInfo)).thenReturn(dorisNodes);
    }

    private List<MonitoringTarget> loadTargets() throws IOException {
        Path file = Paths.get("./", MonitoringNodeService.TARGETS_NAME);
        return JSON.parseArray(new String(Files.readAllBytes(file)), MonitoringTarget.class);
    }

    private void assertTargets(List<MonitoringTarget> monitoringTargets, List<String> nodes, String group) {
        List<List<String>> feListResult = monitoringTargets.stream()
                .filter(t -> t.getLabels().getJob().equals(clusterInfo.getPrometheusJobName()) && t.getLabels().getGroup().equals(group))
                .map(MonitoringTarget::getTargets)
                .collect(Collectors.toList());

        Assert.assertEquals(1, feListResult.size());
        Set<String> feNodes = Sets.newHashSet(feListResult.get(0));
        feNodes.addAll(nodes);
        Assert.assertEquals(nodes.size(), feNodes.size());
    }

    @Test
    public void testDeleteMonitoringNodes() throws Exception {
        monitoringNodeService.setPrometheusHome("./");
        File targets = new File(MonitoringNodeService.TARGETS_NAME);
        if (targets.exists()) {
            targets.delete();
        }
        targets.createNewFile();

        clusterId = 1;
        List<String> feList1 = Lists.newArrayList("127.0.0.32:8030", "127.0.0.33:8030", "10.23.34.32:8030");
        List<String> beList1 = Lists.newArrayList("127.0.0.42:8040", "127.0.0.43:8040", "10.23.34.42:8040");
        DorisNodes dorisNodes1 = new DorisNodes(feList1, beList1);
        mockMonitoringNodeService(dorisNodes1);

        monitoringNodeService.updateMonitoringNodes(clusterInfo);
        List<MonitoringTarget> monitoringTargets1 = loadTargets();
        Assert.assertEquals(2, monitoringTargets1.size());
        assertTargets(monitoringTargets1, feList1, "fe");
        assertTargets(monitoringTargets1, beList1, "be");

        monitoringNodeService.deleteMonitoringNodes(clusterInfo);
        List<MonitoringTarget> monitoringTargets1Loaded = loadTargets();
        Assert.assertEquals(0, monitoringTargets1Loaded.size());

        clusterId = 2;
        DorisNodes dorisNodes2 = new DorisNodes(Lists.newArrayList(), Lists.newArrayList());
        mockMonitoringNodeService(dorisNodes2);
        monitoringNodeService.deleteMonitoringNodes(clusterInfo);
        List<MonitoringTarget> monitoringTargets2Loaded = loadTargets();
        Assert.assertEquals(0, monitoringTargets2Loaded.size());

        targets.delete();
    }
}
