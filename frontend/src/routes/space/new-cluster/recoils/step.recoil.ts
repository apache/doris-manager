import {atom, selector} from 'recoil'
import API from '../new-cluster.api'
import * as types from '../types/index.type'


export const stepState = atom<number>({
    key: 'stepState',
    default: 0,
});

export const CurrentProcessQuery = selector<types.IcurrentProcess>({
    key: "CurrentProcessQuery",
    get: async ( { get } ) => {
        const curstep = get(stepState);
        const response = await API.getCurrentProcess();
        if(response.code === 0){
            return response.data
        }else{
            return {}
        }
    }
})
