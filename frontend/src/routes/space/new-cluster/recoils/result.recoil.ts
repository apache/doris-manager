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
