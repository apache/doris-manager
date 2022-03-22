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

const COLORS = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'];

const COLORS_MAP = COLORS.reduce((memo, current) => {
    memo[current] = 0;
    return memo;
}, {});

class ColorPicker {
    colors: string[] = COLORS;
    colorsMap: Record<string, number> = COLORS_MAP;
    index: number = 0;
    restCount: number = COLORS.length;
    pickInOrder() {
        if (this.restCount === 0) {
            const color = this.colors[this.index % this.colors.length];
            this.colorsMap[color]++;
            this.index++;
            return color;
        }
        const color = Object.keys(this.colorsMap).find(color => this.colorsMap[color] === 0) as string;
        this.colorsMap[color]++;
        this.restCount--;
        this.index = this.colors.indexOf(color) + 1;
        return color;
    }
    pick(color: string) {
        this.colorsMap[color]++;
        if (this.colorsMap[color] === 1) {
            this.restCount--;
        }
        this.index = this.colors.indexOf(color) + 1;
    }
    reset() {
        Object.keys(this.colorsMap).forEach(key => {
            this.colorsMap[key] = 0;
        });
        this.index = 0;
        this.restCount = this.colors.length;
    }
}

export const colorPicker = new ColorPicker();
