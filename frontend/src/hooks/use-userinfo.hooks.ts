import { CommonAPI } from "@src/common/common.api";
import { isSuccess } from "@src/utils/http";
import { Dispatch, SetStateAction, useState, useEffect } from "react";

export function useUserInfo(): [any, Dispatch<SetStateAction<any[]>>] {
    const [userInfo, setUserInfo] = useState<any>({});
    useEffect(() => {
        getUserInfo();
    }, []);

    async function getUserInfo() {
        const res = await CommonAPI.getUserInfo();
        if (isSuccess(res)) {
            setUserInfo(res.data);
        }
    }
    return [userInfo, setUserInfo];
}