import { atom, selector, selectorFamily } from 'recoil';
import { SpaceAPI } from './space.api';
import { IspaceUser } from './space.interface';

export const usersQuery = selector<IspaceUser[]>({
    key: 'UsersQuery',
    get: async () => {
        const response = await SpaceAPI.getUsers({ include_deactivated: true });
        if (response.code === 0) {
            return response.data;
        } else {
            return [];
        }
    },
});
