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

import { isSuccess } from "@src/utils/http";
import { atom, selector } from "recoil";
import { SpaceAPI } from "../space.api";
import { ACCESS_CLUSTER_REQUEST_INIT_PARAMS } from "./access-cluster.data";

export const nextStepDisabledState = atom({
    key: 'nextStepDisabled',
    default: false,
});

export const requestInfoState = atom<any>({
    key: 'requestInfoState',
    default: ACCESS_CLUSTER_REQUEST_INIT_PARAMS,
});

export const stepDisabledState = atom<any>({
    key: 'stepDisabledState',
    default: {
        next: false,
        prev: false,
    },
});

// export const requestInfoQuery = selector({
//     key: 'requestInfoQuery',
//     get: async ({get}) => {
//         const requestInfo = get(requestInfoState);
//         console.log(requestInfo);
//         // if (+requestInfo.clusterId === 0) {
//         //     return JSON.parse(localStorage.getItem('requestInfo') || JSON.stringify(ACCESS_CLUSTER_REQUEST_INIT_PARAMS));
//         // }
//         const res = await SpaceAPI.spaceGet(requestInfo.clusterId);
//         console.log(res);
//         if (isSuccess(res)) {
//             localStorage.setItem('requestInfo', JSON.stringify(res.data));
//             return res.data;
//         }
//         return ACCESS_CLUSTER_REQUEST_INIT_PARAMS;
//     },
//     set: ({set}, newValue: any) => {
//         set(requestInfoState, newValue);
//     }
// });
