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

package org.apache.doris.stack.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.doris.stack.model.activity.ActivityInfoResp;
import org.apache.doris.stack.model.activity.ActivityModelType;

import org.apache.doris.stack.model.activity.Topic;
import org.apache.doris.stack.dao.ActivityRepository;
import org.apache.doris.stack.entity.ActivityEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
@Slf4j
public class UserActivityComponent {

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * Add the log of user login. If you log in for the first time, add two topics: user joined and user login.
     * If it is not the first time, update the time of user login
     * TODO:If it is a platform level operation, the space ID is 0 by default.
     * TODO:If a space is specified, the space ID is passed in
     * @param userId
     */
    public void userLoginActivity(int userId, long clusterId) {
        String model = ActivityModelType.user.name();
        List<ActivityEntity> activityEntities = activityRepository.getByModelAndUserId(model, userId);
        if (activityEntities == null || activityEntities.isEmpty()) {
            log.debug("The user {} first login, add user joined and login activity.", userId);
            // add join
            ActivityInfoResp.Details details = new ActivityInfoResp.Details();
            ActivityEntity joinedEntity = new ActivityEntity(Topic.USE_JOINED, model, JSON.toJSONString(details, SerializerFeature.DisableCircularReferenceDetect), userId, userId);
            activityRepository.save(joinedEntity);
            // add login
            ActivityEntity longinEntity = new ActivityEntity(Topic.USE_LOGIN, model, JSON.toJSONString(details, SerializerFeature.DisableCircularReferenceDetect), userId, userId);
            activityRepository.save(longinEntity);
        } else {
            for (ActivityEntity activityEntity : activityEntities) {
                if (activityEntity.getTopic().equals(Topic.USE_LOGIN)) {
                    log.debug("Update user {} login activity {}.", userId, activityEntity.getId());
                    activityEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    activityEntity.setClusterId(clusterId);
                    activityRepository.save(activityEntity);
                }
            }
        }
    }

    /**
     * User switching space record
     * @param userId
     * @param clusterId
     * @param topic
     */
    public void userSwitchSpace(int userId, long clusterId, String topic) {
        String model = ActivityModelType.user.name();
        log.debug("The user {} switch space {}, add topic {} activity.", userId, clusterId, topic);
        ActivityInfoResp.Details details = new ActivityInfoResp.Details();
        ActivityEntity entity = new ActivityEntity(topic, model, JSON.toJSONString(details, SerializerFeature.DisableCircularReferenceDetect), userId, userId);
        entity.setClusterId(clusterId);
        activityRepository.save(entity);
    }

}
