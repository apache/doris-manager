import React from 'react';
import { UserInfo } from './common.interface';

export const UserInfoContext = React.createContext<UserInfo | null>(null);
export const NewSpaceInfoContext = React.createContext<any>(null);
