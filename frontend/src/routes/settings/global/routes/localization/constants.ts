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

export const DEFAULT_DATE_STYLE_OPTIONS = [
    { value: 'MMMM D, YYYY', completeLabel: '一月 7, 2018', compressedLabel: '1月, 7, 2018' },
    { value: 'D MMMM, YYYY', completeLabel: '7 一月, 2018', compressedLabel: '7 1月, 2018' },
    { value: 'dddd, MMMM D, YYYY', completeLabel: '星期日, 一月 7, 2018', compressedLabel: '周日, 1月 7, 2018' },
    { value: 'M/D/YYYY', label: [1, 7, 2018] },
    { value: 'D/M/YYYY', label: [7, 1, 2018] },
    { value: 'YYYY/M/D', label: [2018, 1, 7] },
];

export const DATE_SEPARATOR_OPTIONS = [
    { value: '/', label: 'M/D/YYYY' },
    { value: '-', label: 'M-D-YYYY' },
    { value: '.', label: 'M.D.YYYY' },
];

export const TIME_STYLE_OPTIONS = [
    { value: 'h:mm A', label: '5:24 下午 (12小时制)' },
    { value: 'k:mm', label: '17:24 (24小时制)' },
];

export const NUMBER_SEPARATORS_OPTIONS = [
    { value: '.,', label: '100,000.00' },
    { value: ', ', label: '100 000,00' },
    { value: ',.', label: '100.000,00' },
    { value: '.', label: '100000.00' },
];
