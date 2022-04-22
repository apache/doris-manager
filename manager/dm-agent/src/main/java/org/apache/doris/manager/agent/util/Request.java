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

package org.apache.doris.manager.agent.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.doris.manager.common.heartbeat.HeartBeatContext;
import org.apache.doris.manager.common.heartbeat.HeartBeatResult;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Request {
    public static HeartBeatContext getHeartBeatContext(String requestUrl) {
        String ctx = null;

        try {
            ctx = sendGetRequest(requestUrl, new HashMap<>());
            log.info("getHeartBeatContextResults:" + ctx);
        } catch (Exception e) {
            log.error("get heartbeat context error {}", e.getMessage());
        }

        if (ctx == null) {
            log.warn("no context return");
            return new HeartBeatContext();
        }
        return JSON.parseObject(ctx, HeartBeatContext.class);
    }

    public static String sendHeartBeatContextResult(String requestUrl, HeartBeatResult res) throws IOException {
        log.info("send heart beat context result {} to {}", JSON.toJSONString(res), requestUrl);
        return sendPostRequest(requestUrl, JSON.toJSONString(res));
    }

    public static String sendPostRequest(String requestUrl, String bodyJson) throws IOException {
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setConfig(timeout());

        httpPost.setEntity(new StringEntity(bodyJson, "utf-8"));
        httpPost.addHeader("Content-Type", "application/json");

        try {
            return request(httpPost);
        } catch (IOException e) {
            log.error("request url error:{},param:{}", requestUrl, bodyJson, e);
            throw e;
        }
    }

    public static String sendPostRequest(String requestUrl, Map<String, Object> params) {
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setConfig(timeout());

        httpPost.setEntity(new StringEntity(JSON.toJSONString(params), "utf-8"));
        httpPost.addHeader("Content-Type", "application/json");

        try {
            return request(httpPost);
        } catch (IOException e) {
            log.error("request url error:{},param:{}", requestUrl, params, e);
            throw new RuntimeException(e);
        }
    }

    public static String sendGetRequest(String requestUrl, Map<String, Object> params)
            throws URISyntaxException, IOException {
        URI url = null;
        try {
            URIBuilder uriBuilder = null;
            uriBuilder = new URIBuilder(requestUrl);
            for (Map.Entry<String, Object> param : params.entrySet()) {
                uriBuilder.addParameter(param.getKey(), String.valueOf(param.getValue()));
            }
            url = uriBuilder.build();
        } catch (URISyntaxException e) {
            log.error("{} syntax error {}", requestUrl, e.getMessage());
            throw e;
        }

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(timeout());

        try {
            return request(httpGet);
        } catch (IOException e) {
            log.error("request url error:{},param:{}", requestUrl, params, e);
            throw e;
        }
    }

    public static String request(HttpUriRequest request) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(request);
        String result = EntityUtils.toString(response.getEntity());
        close(response, httpclient);
        return result;
    }

    public static void close(CloseableHttpResponse response, CloseableHttpClient httpClient) {
        try {
            if (response != null) {
                response.close();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            log.error("close http connection failed", e);
        }
    }

    public static RequestConfig timeout() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000).build();
        return requestConfig;
    }
}
