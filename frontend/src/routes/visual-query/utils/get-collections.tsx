import React from 'react';
import { FolderOutlined } from '@ant-design/icons';
import { CascaderItem } from '../types';

function getLocationPaths(location: string) {
    return location
        .split('/')
        .filter(item => item !== '')
        .map(item => Number(item));
}

function getParent(map: Map<number, CascaderItem>, locationPaths: number[]) {
    return locationPaths.reduce((memo, current) => {
        return memo.children.find(item => item.id === current)!;
    }, map.get(locationPaths.shift()!) as CascaderItem);
}

export function getCollections(data: CascaderItem[]) {
    const filteredData = data
        .filter(item => !item.archived && item.can_write)
        .map(item => ({
            ...item,
            children: [] as CascaderItem[],
            label: (
                <div>
                    <FolderOutlined style={{ marginRight: 5 }} />
                    {item.name}
                </div>
            ),
        }))
        .sort((a, b) => getLocationPaths(a.location).length - getLocationPaths(b.location).length);
    const map = new Map<number, CascaderItem>();
    filteredData.forEach(item => {
        const locationPaths = getLocationPaths(item.location);
        if (locationPaths.length === 0) {
            map.set(item.id, item);
            return;
        }
        const parent = getParent(map, locationPaths);
        parent.children.push(item);
    });
    return Array.from(map).map(item => item[1]);
}
