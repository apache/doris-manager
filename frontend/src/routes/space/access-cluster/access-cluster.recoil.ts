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
