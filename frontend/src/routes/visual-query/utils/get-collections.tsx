// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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
