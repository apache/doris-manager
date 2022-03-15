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

import passwordGenerator from 'password-generator';

function isStrongEnough(password: string) {
    return /^(?![a-zA-Z]+$)(?![A-Z\d]+$)(?![A-Z_]+$)(?![a-z\d]+$)(?![a-z_]+$)(?![\d_]+$)[a-zA-Z\d_]{6,12}$/.test(
        password,
    );
}

export function generatePassword() {
    let password = passwordGenerator(12, false, /[a-zA-Z\d_]/);
    while (!isStrongEnough(password)) {
        password = passwordGenerator(12, false, /[a-zA-Z\d_]/);
    }
    return password;
}

export function copyText(text: string) {
    const textArea = document.createElement('textarea');
    textArea.style.opacity = '0';
    document.body.appendChild(textArea);
    textArea.value = text;
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
}
