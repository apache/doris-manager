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

import Swal from 'sweetalert2';
import withReactContent from 'sweetalert2-react-content';

const DorisSwal = withReactContent(Swal);

class DorisModal {
    message(message) {
        DorisSwal.fire(message);
    }

    success(title, message = '') {
        return Swal.fire(title, message, 'success');
    }

    error(title = '', message = '') {
        return Swal.fire(title, message, 'error');
    }

    confirm(title, message = '', callback) {
        Swal.fire({
            title: title,
            text: message,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: '确定',
            cancelButtonText: '取消',
        }).then(async result => {
            if (result.isConfirmed && typeof callback === 'function') {
                callback();
            } else if (!result.isConfirmed) {
                Swal.close();
            } else {
                this.success('操作成功');
            }
        });
    }
}

export const modal = new DorisModal();
