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

import org.apache.doris.stack.component.ClusterUserComponent;
import org.apache.doris.stack.connector.PrometheusMonitorClient;
import org.apache.doris.stack.constant.PropertyDefine;
import org.apache.doris.stack.entity.ClusterInfoEntity;
import org.apache.doris.stack.entity.CoreUserEntity;
import org.apache.doris.stack.model.prometheus.PrometheusTimeSeriesResp;
import org.apache.doris.stack.model.prometheus.PrometheusValueResp;
import org.apache.doris.stack.model.request.monitor.MonitorRequestBody;
import org.apache.doris.stack.model.response.construct.MonitorDataResp;
import org.apache.doris.stack.model.response.construct.MonitorDataTransactionResp;
import org.apache.doris.stack.service.user.AuthenticationService;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * https://prometheus.io/docs/prometheus/latest/querying/api/
 * <p>
 * Query prometheus Monitoring Data.
 */
@Service
@Slf4j
public class MonitoringQueryService {
    // prometheus scrapes monitoring data at 15s intervals.
    private static final int SCRAPE_INTERVAL = 15;

    private static final String QUERY = "query";

    private static final String START = "start";

    private static final String END = "end";

    private static final String STEP = "step";

    private static final String TIMEOUT = "timeout";

    private static final String RATE = "rate";

    private static final String IRATE = "irate";

    private String queryRangeUrl;

    private String queryUrl;

    public enum MonitorType {
        QPS,
        QUERY_LATENCY,
        QUERY_ERR_RATE,
        CONN_TOTAL,
        TXN_STATUS,
        SCHEDULED_TABLET_NUM,
        BE_CPU_IDLE,
        BE_MEM,
        BE_DISK_IO,
        BE_BASE_COMPACTION_SCORE,
        BE_CUMU_COMPACTION_SCORE
    }

    public enum ClusterInfoType {
        NODE_NUM,
        DISKS_CAPACITY,
        STATISTIC,
        FE_LIST,
        BE_LIST
    }

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PrometheusMonitorClient prometheusMonitorClient;

    @Autowired
    private ClusterUserComponent clusterUserComponent;

    @Autowired
    private Environment environment;

    private synchronized void intPrometheusUrl() {
        String url = "http://" + environment.getProperty(PropertyDefine.PROMETHEUS_HOST_PROPERTY) + ":"
                + environment.getProperty(PropertyDefine.PROMETHEUS_PORT_PROPERTY);
        this.queryRangeUrl = url + "/api/v1/query_range";
        this.queryUrl = url + "/api/v1/query";
    }

    public ClusterInfoEntity checkAndHandleCluster(HttpServletRequest request, HttpServletResponse response) throws Exception {
        CoreUserEntity user = authenticationService.checkNewUserAuthWithCookie(request, response);
        return clusterUserComponent.getUserCurrentClusterAndCheckAdmin(user);
    }

    /*
     * Json data format of MonitorDataResp:
     *  {
     *         "x_value": [
     *             timestamp,
     *             timestamp
     *         ],
     *         "y_value": {
     *             "host:http_port": [
     *                 value,
     *                 value
     *             ]
     *      }
     *  }
     *
     * json data format of MonitorDataTransactionResp:
     * {
     *         "x_value": [
     *             timestamp,
     *             timestamp
     *         ],
     *         "y_value": {
     *             "fe_host1:http_port": {
     *                 "begin" [
     *                     value,
     *                     value
     *                 ]
     *                 "success": [
     *                     value,
     *                     value
     *                 ],
     *                 "failed": [
     *                     value,
     *                     value
     *                 ]
     *         }
     *     }
     * }
     */
    public Object handleTimeSerialMonitor(HttpServletRequest request, HttpServletResponse response,
                                          long start, long end, MonitorRequestBody bodyParameter,
                                          String monitorTypeString) throws Exception {
        MonitorType monitorType;
        try {
            monitorType = MonitorType.valueOf(monitorTypeString.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("No such monitor type: " + monitorTypeString);
        }

        ClusterInfoEntity clusterInfoEntity = checkAndHandleCluster(request, response);
        String jobName = clusterInfoEntity.getPrometheusJobName();

        if (Strings.isNullOrEmpty(queryUrl) || Strings.isNullOrEmpty(queryRangeUrl)) {
            intPrometheusUrl();
        }

        long startSecond = start / 1000;
        long endSecond = end / 1000;
        int dataScale = bodyParameter.getPointNum() == 0 ? 100 : bodyParameter.getPointNum();
        String step = calculateQueryStep(startSecond, endSecond, dataScale);

        Map<String, String> params = Maps.newHashMap();
        params.put(START, String.valueOf(startSecond));
        params.put(END, String.valueOf(endSecond));
        params.put(STEP, step);
        params.put(TIMEOUT, "1m");

        switch (monitorType) {
            case QPS:
                return qps(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case QUERY_LATENCY:
                if (Strings.isNullOrEmpty(bodyParameter.getQuantile())) {
                    throw new IllegalArgumentException("The parameter of quantile is required.");
                }
                return queryLatency(queryRangeUrl, params, bodyParameter.getNodes(), bodyParameter.getQuantile(), jobName);
            case QUERY_ERR_RATE:
                return queryErrRate(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case CONN_TOTAL:
                return connTotal(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case TXN_STATUS:
                return txnStatus(queryRangeUrl, params, jobName);
            case SCHEDULED_TABLET_NUM:
                return scheduledTabletNum(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case BE_CPU_IDLE:
                return beCpuIdle(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case BE_MEM:
                return beMem(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case BE_DISK_IO:
                return beDiskIO(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case BE_BASE_COMPACTION_SCORE:
                return beBaseCompactionScore(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            case BE_CUMU_COMPACTION_SCORE:
                return beCumuCompactionScore(queryRangeUrl, params, bodyParameter.getNodes(), jobName);
            default:
                throw new RuntimeException("No such monitoring type is defined:" + monitorType);
        }
    }

    private String calculateQueryStep(long startSecond, long endSecond, int dataScale) {
        long numSecond = (endSecond - startSecond) / dataScale / SCRAPE_INTERVAL * SCRAPE_INTERVAL;
        return (numSecond == 0 ? SCRAPE_INTERVAL : numSecond) + "s";
    }

    private MonitorDataResp<Double> qps(String url, Map<String, String> prometheusParams, List<String> nodes,
                                        String cluster) throws IOException {
        // rate(doris_fe_query_total{job="cluster",instance=~"node1|node2"}[INTERVAL])
        prometheusParams.put(QUERY,
                concatPromQLOfRate(RATE, "doris_fe_query_total", nodes, cluster, null, prometheusParams.get(STEP)));
        return parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Double> queryLatency(String url, Map<String, String> prometheusParams, List<String> nodes,
                                                 String quantile, String cluster) throws IOException {
        // sum(doris_fe_query_latency_ms{job="cluster",instance=~"node1|node2",quantile="0.99"}) by (instance)
        StringBuilder query = new StringBuilder();
        query.append("sum(doris_fe_query_latency_ms{job=\"")
                .append(cluster)
                .append("\"");
        if (nodes != null && !nodes.isEmpty()) {
            query.append(",instance=~\"")
                    .append(Joiner.on("|").join(nodes))
                    .append("\"");
        }
        query.append(",quantile=\"")
                .append(quantile)
                .append("\"")
                .append("}) by (instance)");
        prometheusParams.put(QUERY, query.toString());

        return parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Double> queryErrRate(String url, Map<String, String> prometheusParams, List<String> nodes,
                                                 String cluster) throws IOException {
        // rate(doris_fe_query_err{job="cluster",instance=~"node1|node2"}[INTERVAL])
        prometheusParams.put(QUERY,
                concatPromQLOfRate(RATE, "doris_fe_query_err", nodes, cluster, null, prometheusParams.get(STEP)));
        return parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Long> connTotal(String url, Map<String, String> prometheusParams, List<String> nodes,
                                            String cluster) throws IOException {
        // doris_fe_connection_total{job="cluster",instance=~"node1|node2"}
        prometheusParams.put(QUERY, concatPromQLOfMetric("doris_fe_connection_total", nodes, cluster, null));
        return parsePrometheusResponseLong(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataTransactionResp txnStatus(String url, Map<String, String> prometheusParams, String cluster)
            throws IOException {
        Map<String, List<Double>> data = Maps.newHashMap();
        String masterNode = "";
        // irate(doris_fe_txn_begin{job="cluster"}[1m]) and on (instance) node_info{group="fe", job="cluster", type="is_master"}
        prometheusParams.put(QUERY, concatPromQLOfRate(IRATE, "doris_fe_txn_begin", null, cluster, null,
                prometheusParams.get(STEP)) + forMaster(cluster));
        MonitorDataResp<Double> begin = parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams),
                prometheusParams);
        for (String node : begin.getYValue().keySet()) {
            masterNode = node;
            break;
        }
        data.put("begin", begin.getYValue().get(masterNode));

        // irate(doris_fe_txn_success{job="cluster"}[1m]) and on (instance) node_info{group="fe", job="cluster", type="is_master"}
        prometheusParams.put(QUERY, concatPromQLOfRate(IRATE, "doris_fe_txn_success", null, cluster, null,
                prometheusParams.get(STEP)) + forMaster(cluster));
        MonitorDataResp<Double> success = parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams),
                prometheusParams);
        data.put("success", success.getYValue().get(masterNode));

        // irate(doris_fe_txn_failed{job="cluster"}[1m]) and on (instance) node_info{group="fe", job="cluster", type="is_master"}
        prometheusParams.put(QUERY, concatPromQLOfRate(IRATE, "doris_fe_txn_failed", null, cluster, null,
                prometheusParams.get(STEP)) + forMaster(cluster));
        MonitorDataResp<Double> failed = parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams),
                prometheusParams);
        data.put("failed", failed.getYValue().get(masterNode));

        Map<String, Map<String, List<Double>>> yValue = Maps.newHashMap();
        yValue.put(masterNode, data);
        return new MonitorDataTransactionResp(begin.getXValue(), yValue);
    }

    private MonitorDataResp<Long> scheduledTabletNum(String url, Map<String, String> prometheusParams, List<String> nodes,
                                                     String cluster) throws IOException {
        // doris_fe_scheduled_tablet_num{job="cluster",instance=~"node1|node2"}
        prometheusParams.put(QUERY, concatPromQLOfMetric("doris_fe_scheduled_tablet_num", nodes, cluster, null)
                .concat(forMaster(cluster)));
        return parsePrometheusResponseLong(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Double> beCpuIdle(String url, Map<String, String> prometheusParams, List<String> nodes,
                                              String cluster) throws IOException {
        // "(sum(rate(doris_be_cpu{mode=\"idle\", job=\"cluster\"}[1m])) by (job, instance))
        // / (sum(rate(doris_be_cpu{job=\"cluster\"}[1m])) by (job, instance)) * 100"
        StringBuilder query = new StringBuilder();
        query.append("(sum(")
                .append(concatPromQLOfRate(RATE, "doris_be_cpu", nodes, cluster,
                        Lists.newArrayList("mode=\"idle\""), prometheusParams.get(STEP)))
                .append(") by (job, instance))")
                .append(" / (sum(")
                .append(concatPromQLOfRate(RATE, "doris_be_cpu", nodes, cluster,
                        null, prometheusParams.get(STEP)))
                .append(") by (job, instance)) * 100");
        prometheusParams.put(QUERY, query.toString());
        return parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Long> beMem(String url, Map<String, String> prometheusParams, List<String> nodes,
                                        String cluster) throws IOException {
        // doris_be_memory_allocated_bytes{job="palo"}
        prometheusParams.put(QUERY,
                concatPromQLOfMetric("doris_be_memory_allocated_bytes", nodes, cluster, null));
        return parsePrometheusResponseLong(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Double> beDiskIO(String url, Map<String, String> prometheusParams, List<String> nodes,
                                             String cluster) throws IOException {
        // rate(doris_be_disk_io_time_ms{job="palo"}[1m]) / 10
        prometheusParams.put(QUERY,
                concatPromQLOfRate(RATE, "doris_be_disk_io_time_ms", nodes, cluster, null,
                        prometheusParams.get(STEP)) + " / 10");
        return parsePrometheusResponseDouble(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Long> beBaseCompactionScore(String url, Map<String, String> prometheusParams, List<String> nodes,
                                                        String cluster) throws IOException {
        // doris_be_tablet_base_max_compaction_score{job="doris"}
        prometheusParams.put(QUERY,
                concatPromQLOfMetric("doris_be_tablet_base_max_compaction_score", nodes, cluster, null));
        return parsePrometheusResponseLong(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private MonitorDataResp<Long> beCumuCompactionScore(String url, Map<String, String> prometheusParams, List<String> nodes,
                                                        String cluster) throws IOException {
        // doris_be_tablet_cumulative_max_compaction_score{job="doris"}
        prometheusParams.put(QUERY,
                concatPromQLOfMetric("doris_be_tablet_cumulative_max_compaction_score", nodes, cluster, null));
        return parsePrometheusResponseLong(queryPrometheusTimeSeries(url, prometheusParams), prometheusParams);
    }

    private String concatPromQLOfRate(String rate, String metric, List<String> nodes, String cluster, List<String> label,
                                      String step) {
        // At least two data are needed to calculate the rate, and the time interval is at least twice the SCRAPE_INTERVAL.
        int stepSecond = Integer.parseInt(step.substring(0, step.length() - 1));
        String interval = Math.max(stepSecond, 2 * SCRAPE_INTERVAL) + "s";
        return rate + "(" + concatPromQLOfMetric(metric, nodes, cluster, label) + "[" + interval + "])";
    }

    private String concatPromQLOfMetric(String metric, List<String> nodes, String cluster, List<String> label) {
        StringBuilder query = new StringBuilder();
        query.append(metric)
                .append("{job=\"")
                .append(cluster)
                .append("\"");
        if (nodes != null && !nodes.isEmpty()) {
            query.append(",instance=~\"")
                    .append(Joiner.on("|").join(nodes))
                    .append("\"");
        }
        if (label != null && !label.isEmpty()) {
            query.append(",")
                    .append(Joiner.on(",").join(label));
        }
        query.append("}");
        return query.toString();
    }

    private String forMaster(String cluster) {
        return " and on (instance) node_info{group=\"fe\", job=\"" + cluster + "\", type=\"is_master\"}";
    }

    private PrometheusTimeSeriesResp queryPrometheusTimeSeries(String url, Map<String, String> prometheusParams)
            throws IOException {
        String responseJson = prometheusMonitorClient.doPost(url, prometheusParams);
        PrometheusTimeSeriesResp prometheusTimeSeriesResp = JSON.parseObject(responseJson, PrometheusTimeSeriesResp.class);
        if (!prometheusTimeSeriesResp.getStatus().equals("success")) {
            log.warn("query prometheus error. url:{}, params:{}, response:{}", url, prometheusParams.toString(),
                    responseJson);
            throw new RuntimeException("prometheus response error.");
        }
        return prometheusTimeSeriesResp;
    }

    private MonitorDataResp<Long> parsePrometheusResponseLong(PrometheusTimeSeriesResp prometheusTimeSeriesResp,
                                                              Map<String, String> prometheusParams) {
        List<PrometheusTimeSeriesResp.Result> results = prometheusTimeSeriesResp.getData().getResult();
        if (results == null || results.isEmpty()) {
            return new MonitorDataResp<>(Lists.newArrayList(), Maps.newHashMap());
        }
        List<Long> xValue = results.get(0).getValues().stream().map(l -> Long.parseLong(l.get(0)) * 1000)
                .collect(Collectors.toList());
        Map<String, List<Long>> yValue = Maps.newHashMap();
        for (PrometheusTimeSeriesResp.Result result : results) {
            List<Long> monitorValue = result.getValues().stream().map(l -> Long.parseLong(l.get(1)))
                    .collect(Collectors.toList());
            yValue.put(result.getInstance(), monitorValue);
        }
        MonitorDataResp<Long> resp = new MonitorDataResp<>(xValue, yValue);
        fillMonitoringDataPoint(prometheusParams, resp);
        return resp;
    }

    private MonitorDataResp<Double> parsePrometheusResponseDouble(PrometheusTimeSeriesResp prometheusTimeSeriesResp,
                                                                  Map<String, String> prometheusParams) {
        List<PrometheusTimeSeriesResp.Result> results = prometheusTimeSeriesResp.getData().getResult();
        if (results == null || results.isEmpty()) {
            return new MonitorDataResp<>();
        }
        List<Long> xValue = results.get(0).getValues().stream().map(l -> Long.parseLong(l.get(0)) * 1000)
                .collect(Collectors.toList());
        Map<String, List<Double>> yValue = Maps.newHashMap();
        for (PrometheusTimeSeriesResp.Result result : results) {
            List<Double> monitorValue = result.getValues().stream()
                    .map(l -> Math.round(Double.parseDouble(l.get(1)) * 100) / 100.0).collect(Collectors.toList());
            yValue.put(result.getInstance(), monitorValue);
        }
        MonitorDataResp<Double> resp = new MonitorDataResp<>(xValue, yValue);
        fillMonitoringDataPoint(prometheusParams, resp);
        return resp;
    }

    // Although the start time and end time of the query monitoring data are specified, prometheus will only return
    // the timestamp for which the monitoring data is available.
    // This method will fill in the timestamp for xValue and null for yValue.
    // xValue: [1630618392000, 1630623972000]  -> [1630607232000, 1630612812000, 1630618392000, 1630623972000]
    // yValue: [1,2] -> [null, null, 1, 2]
    private <E> void fillMonitoringDataPoint(Map<String, String> prometheusParams, MonitorDataResp<E> monitorDataResp) {
        Preconditions.checkNotNull(prometheusParams);
        Preconditions.checkNotNull(monitorDataResp);

        List<Long> xValue = monitorDataResp.getXValue();
        if (xValue.isEmpty()) {
            return;
        }

        String step = prometheusParams.get(STEP);
        long stepMs = Long.parseLong(step.substring(0, step.length() - 1)) * 1000;
        long startMs = Long.parseLong(prometheusParams.get(START)) * 1000;
        long endMs = Long.parseLong(prometheusParams.get(END)) * 1000;

        if (startMs <= xValue.get(0) - stepMs) {
            fillFront(startMs, xValue.get(0), stepMs, monitorDataResp);
        }
        if (endMs >= xValue.get(xValue.size() - 1) + stepMs) {
            fillBack(xValue.get(xValue.size() - 1), endMs, stepMs, monitorDataResp);
        }
    }

    private <E> void fillFront(long start, long end, long step, MonitorDataResp<E> monitorDataResp) {
        long startSecond = end - (end - start) / step * step;
        List<Long> newXValue = Lists.newArrayList();
        List<E> value = Lists.newArrayList();
        for (long timestamp = startSecond; timestamp < end; timestamp += step) {
            newXValue.add(timestamp);
            value.add(null);
        }
        newXValue.addAll(monitorDataResp.getXValue());
        monitorDataResp.setXValue(newXValue);

        Map<String, List<E>> newYValue = Maps.newHashMap();
        for (Map.Entry<String, List<E>> entry : monitorDataResp.getYValue().entrySet()) {
            List<E> list = Lists.newArrayList(value);
            list.addAll(entry.getValue());
            newYValue.put(entry.getKey(), list);
        }

        monitorDataResp.setYValue(newYValue);
    }

    private <E> void fillBack(long start, long end, long step, MonitorDataResp<E> monitorDataResp) {
        List<Long> xValue = monitorDataResp.getXValue();
        List<E> value = Lists.newArrayList();
        for (long timestamp = start + step; timestamp <= end; timestamp += step) {
            xValue.add(timestamp);
            value.add(null);
        }
        for (List<E> values : monitorDataResp.getYValue().values()) {
            values.addAll(value);
        }
    }

    public Object handleScalarMonitor(HttpServletRequest request, HttpServletResponse response, String typeString)
            throws Exception {
        ClusterInfoType type;
        try {
            type = ClusterInfoType.valueOf(typeString.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("No such monitor type: " + typeString);
        }

        ClusterInfoEntity clusterInfoEntity = checkAndHandleCluster(request, response);
        String jobName = clusterInfoEntity.getPrometheusJobName();

        if (Strings.isNullOrEmpty(queryUrl) || Strings.isNullOrEmpty(queryRangeUrl)) {
            intPrometheusUrl();
        }

        switch (type) {
            case NODE_NUM:
                return nodeNum(jobName);
            case DISKS_CAPACITY:
                return disksCapacity(jobName);
            case STATISTIC:
                return statistic(jobName);
            case FE_LIST:
                return feList(jobName);
            case BE_LIST:
                return beList(jobName);
            default:
                throw new RuntimeException("No such monitoring type is defined:" + type);
        }
    }

    /*
     * {
     *         "fe_node_num_total": value,
     *         "fe_node_num_alive": value,
     *         "be_node_num_total": value,
     *         "be_node_num_alive": value
     * }
     */
    private Map<String, Long> nodeNum(String cluster) throws IOException {
        Map<String, Long> result = Maps.newHashMap();
        Map<String, String> prometheusParams = Maps.newHashMap();
        prometheusParams.put(QUERY, "count(up{group=\"fe\", job=\"" + cluster + "\"})");
        PrometheusValueResp feNodeTotalResp = queryPrometheusValue(queryUrl, prometheusParams);
        result.put("fe_node_num_total", parsePrometheusValue(feNodeTotalResp));

        prometheusParams.put(QUERY, "count(up{group=\"fe\", job=\"" + cluster + "\"} == 1)");
        PrometheusValueResp feNodeAliveResp = queryPrometheusValue(queryUrl, prometheusParams);
        result.put("fe_node_num_alive", parsePrometheusValue(feNodeAliveResp));

        prometheusParams.put(QUERY, "count(up{group=\"be\", job=\"" + cluster + "\"})");
        PrometheusValueResp beNodeTotalResp = queryPrometheusValue(queryUrl, prometheusParams);
        result.put("be_node_num_total", parsePrometheusValue(beNodeTotalResp));

        prometheusParams.put(QUERY, "count(up{group=\"be\", job=\"" + cluster + "\"} == 1)");
        PrometheusValueResp beNodeAliveResp = queryPrometheusValue(queryUrl, prometheusParams);
        result.put("be_node_num_alive", parsePrometheusValue(beNodeAliveResp));

        return result;
    }

    /*
     * {
     *         "be_disks_used": value,
     *         "be_disks_total": value
     * }
     */
    private Map<String, Long> disksCapacity(String cluster) throws IOException {
        // SUM(doris_be_disks_data_used_capacity{job="$cluster_name"})
        Map<String, Long> result = Maps.newHashMap();
        Map<String, String> prometheusParams = Maps.newHashMap();
        prometheusParams.put(QUERY, "SUM(doris_be_disks_data_used_capacity{job=\"" + cluster + "\"})");
        PrometheusValueResp disksUsedResp = queryPrometheusValue(queryUrl, prometheusParams);
        result.put("be_disks_used", parsePrometheusValue(disksUsedResp));

        prometheusParams.put(QUERY, "SUM(doris_be_disks_total_capacity{job=\"" + cluster + "\"})");
        PrometheusValueResp diskTotalResp = queryPrometheusValue(queryUrl, prometheusParams);
        result.put("be_disks_total", parsePrometheusValue(diskTotalResp));

        return result;
    }

    /*
     * {
     *      "unhealthy_tablet_num": value
     * }
     */
    private Map<String, Long> statistic(String cluster) throws IOException {
        Map<String, Long> result = Maps.newHashMap();
        Map<String, String> prometheusParams = Maps.newHashMap();
        prometheusParams.put(QUERY, concatPromQLOfMetric("doris_fe_tablet_status_count", null, cluster,
                Lists.newArrayList("type=\"unhealthy\"")).concat(forMaster(cluster)));
        PrometheusValueResp unhealthyTabletResp = queryPrometheusValue(queryUrl, prometheusParams);
        result.put("unhealthy_tablet_num", parsePrometheusValue(unhealthyTabletResp));
        return result;
    }

    /*
     * [
     *      "fe_host:http_port"
     * ]
     */
    private List<String> feList(String cluster) throws IOException {
        return nodeList("up{group=\"fe\", job=\"" + cluster + "\"}");
    }

    /*
     * [
     *      "be_host:http_port"
     * ]
     */
    private List<String> beList(String cluster) throws IOException {
        return nodeList("up{group=\"be\", job=\"" + cluster + "\"}");
    }

    private List<String> nodeList(String query) throws IOException {
        List<String> nodeList = Lists.newArrayList();
        Map<String, String> prometheusParams = Maps.newHashMap();
        prometheusParams.put(QUERY, query);
        PrometheusValueResp feListResp = queryPrometheusValue(queryUrl, prometheusParams);
        List<PrometheusValueResp.Result> results = feListResp.getData().getResult();
        for (PrometheusValueResp.Result result : results) {
            nodeList.add(result.getInstance());
        }
        return nodeList;
    }

    private PrometheusValueResp queryPrometheusValue(String url, Map<String, String> prometheusParams) throws IOException {
        String responseJson = prometheusMonitorClient.doPost(url, prometheusParams);
        PrometheusValueResp prometheusValueResp = JSON.parseObject(responseJson, PrometheusValueResp.class);
        if (!prometheusValueResp.getStatus().equals("success")) {
            log.warn("query prometheus error. url:{}, params:{}, response:{}", url, prometheusParams.toString(),
                    responseJson);
            throw new RuntimeException("prometheus response error.");
        }
        return prometheusValueResp;
    }

    private Long parsePrometheusValue(PrometheusValueResp prometheusValueResp) {
        List<PrometheusValueResp.Result> results = prometheusValueResp.getData().getResult();
        if (results.isEmpty()) {
            return null;
        }
        List<String> values = results.get(0).getValue();
        if (values.size() < 2) {
            return null;
        }
        return Long.parseLong(values.get(1));
    }
}

