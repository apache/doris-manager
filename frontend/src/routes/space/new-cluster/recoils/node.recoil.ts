import {atom, selector} from 'recoil'
import API from '../new-cluster.api'
import * as types from '../types/index.type'
import {} from './result.recoil'

export const nodeHardwareQuery = selector<any[]>({
    key: "NodeHardwareQuery",
    get: async ( {get} ) => {
        const response = await API.getNodeHardware({clusterId: 0}); // todo
        if(response.code === 0){
            return response.data
        }else{
            return []
        }
    }
})