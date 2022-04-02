// Copyright 2022 hujian05
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

const dorisAuthProvider = {
    isAuthenticated: false,
    checkInitialized(): boolean {
        const initialized: boolean = JSON.parse(localStorage.getItem('initialized') as string);
        if (initialized) {
            return true;
        }
        return false;
    },
    checkLogin(): boolean {
        const login: any = JSON.parse(localStorage.getItem('login') as string);
        if (login) {
            return true;
        }
        return false;
    },
    signIn(callback: VoidFunction) {
        dorisAuthProvider.isAuthenticated = true;
        setTimeout(callback, 100);
    },
    signOut(callback: VoidFunction) {
        dorisAuthProvider.isAuthenticated = false;
        setTimeout(callback, 100);
    },
};

export { dorisAuthProvider };
