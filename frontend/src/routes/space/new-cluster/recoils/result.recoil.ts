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

import {atom, selector} from 'recoil'
import API from '../new-cluster.api'
import * as types from '../types/index.type'
import { DorisNodeTypeEnum } from '../types/index.type';

export const fresh = atom({
    key: 'fresh',
    default: 1,
});

export const modalState = atom({
    key: 'modalState',
    default: {
        visible: false
    },
});

export const processId = atom({
    key: 'processId',
    default: 1,
});

export const nodeStatusQuery = selector<types.ItaskResult[]>({
    key: "NodeStatusQuery",
    get: async ( { get } ) => {
        const f = get(fresh);
        const g = get(modalState);
        const response = await API.getTaskStatus(get(processId));
        if(response.code === 0){
            return response.data
        }else{
            return []
        }
    }
})


export const roleListQuery = selector<{fe:string[], be: string[]}>({
    key: "RoleListQuery",
    get: async ( {get} ) => {
        const response = await API.getRoleList({ clusterId: 0});
        if(response.code === 0){
            const FE = response.data.filter(item => item.role === DorisNodeTypeEnum.FE).map(item => item.host);
            const BE = response.data.filter(item => item.role === DorisNodeTypeEnum.BE).map(item => item.host);
            return {
                fe: FE,
                be: BE
            }
        }else{
            return {
                fe: [],
                be: []
            }
        }
    }
})
