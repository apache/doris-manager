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

package org.apache.doris.stack.dao;

import org.apache.doris.stack.control.ModelControlLevel;
import org.apache.doris.stack.control.ModelControlRequestType;
import org.apache.doris.stack.entity.ModelControlRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelControlRequestRepository extends JpaRepository<ModelControlRequestEntity, Long> {

    @Query("select c.id from ModelControlRequestEntity c where c.modelLevel = :modelLevel and "
            + "c.modelId = :modelId and c.completed = :completed")
    List<Long> getIdByModelLevelAndIdAndCompleted(@Param("modelLevel") ModelControlLevel modelLevel,
                                                    @Param("modelId") long modelId,
                                                    @Param("completed") boolean completed);

    @Query("select c from ModelControlRequestEntity c where c.modelLevel = :modelLevel and "
            + "c.modelId = :modelId and c.completed = :completed")
    List<ModelControlRequestEntity> getByModelLevelAndIdAndCompleted(@Param("modelLevel") ModelControlLevel modelLevel,
                                                                     @Param("modelId") long modelId,
                                                                     @Param("completed") boolean completed);

    @Query("select c from ModelControlRequestEntity c where c.requestType = :requestType and "
            + "c.completed = :completed")
    List<ModelControlRequestEntity> getByRequestTypeAndCompleted(@Param("requestType") ModelControlRequestType requestType,
                                                                 @Param("completed") boolean completed);
}
