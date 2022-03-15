import { ClassAttributes } from "react";

export interface CustomConfigProps extends ClassAttributes<any>{
    onChange?: (value: CustomConfigValue) => void;
}

export interface CustomConfigValue {
    showCustomConfig: boolean;
    value: string;
}