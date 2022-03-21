import React from 'react';

export interface DataType {
    cols: {
        source: string;
        name: string;
        display_name: string;
        base_type: string;
    }[];
    rows: any[][];
    native_form: {
        query: string;
    };
}

export type Dataset = { source: any[][] }[];

export const enum SidebarTypeEnum {
    TYPE = 'echartsType',
    SETTINGS = 'echartsSettings',
}

export const enum ChartTypeEnum {
    BAR_CHART = 'bar',
    LINE_CHART = 'line',
    AREA_CHART = 'area',
    SCATTER_CHART = 'scatter',
    HORIZONTAL_BAR_CHART = 'horizontal-bar',
    TABLE = 'table',
}

export const enum AxisTypeEnum {
    CATEGORY = 'category',
    VALUE = 'value',
}

export interface CascaderItem {
    name: string;
    id: number;
    archived: boolean;
    can_write: boolean;
    location: string;
    children: CascaderItem[];
    label: React.ReactNode;
    isLeaf?: boolean;
    isFetched?: boolean;
}
