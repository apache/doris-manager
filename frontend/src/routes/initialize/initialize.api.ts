import { IResult } from "@src/interfaces/http.interface";
import { http } from "@src/utils/http";

function setAuthType(data?: any): Promise<IResult<any>> {
    return http.post(`/api/setting/init/authType`, data);
}

function resetAuthType(data?: any): Promise<IResult<any>> {
    return http.get(`/api/setting/reset`, data);
}

function setAdmin(data?: any): Promise<IResult<any>> {
    return http.post(`/api/setting/admin`, data);
}

function setLDAP(data?: any): Promise<IResult<any>> {
    return http.post(`/api/setting/init/ldapStudio`, data);
}

function getInitProperties(): Promise<IResult<any>>  {
    return http.get(`/api/session/initProperties`);
}

function getLDAPUser(): Promise<IResult<any>>  {
    return http.get(`/api/setting/ldapUsers`);
}

export const InitializeAPI = {
    setAuthType,
    resetAuthType,
    setAdmin,
    setLDAP,
    getInitProperties,
    getLDAPUser
}
