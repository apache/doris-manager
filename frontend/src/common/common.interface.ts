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
export interface UserInfo {
    email:          string;
    name:           string;
    authType:       string;
    id:             number;
    collectionId:   number;
    ldap_auth:      boolean;
    last_login:     Date;
    updated_at:     Date;
    group_ids:      number[];
    date_joined:    Date;
    common_name:    string;
    google_auth:    boolean;
    space_id:       number;
    space_complete: boolean;
    deploy_type:    string;
    manager_enable: boolean;
    is_active:      boolean;
    is_admin:       boolean;
    is_qbnewb:      boolean;
    is_super_admin: boolean;
}

export interface IRole {
    id: number;
    member_count: number;
    name: string;
}

export interface IMember {
    name: string;
    members: any[];
    id: number;
}