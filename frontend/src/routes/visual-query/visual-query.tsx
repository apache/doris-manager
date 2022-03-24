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

import React, { useRef, useState } from 'react';
import VisualQueryProvider from './context';
import VisualContent from './components/visual-content';
import VisualHeader from './components/visual-header';
import SaveQueryModal from './components/save-query-modal';
import SaveDashboardModal from './components/save-dashboard-modal';
import { useCollections } from './hooks';

export default function VisualQuery() {
    const [saveQueryModalVisible, setSaveQueryModalVisible] = useState(false);
    const [saveDashboardModalVisible, setSaveDashboardModalVisible] = useState(false);
    const visualContentRef = useRef<{ refreshTableWidth: () => void }>(null);
    const { collections, loading } = useCollections();

    const onAnimated = () => {
        if (visualContentRef.current) {
            visualContentRef.current.refreshTableWidth();
        }
    };

    return (
        <VisualQueryProvider>
            <VisualHeader setSaveQueryModalVisible={setSaveQueryModalVisible} />
            <div style={{ display: 'flex' }}>
                <VisualContent ref={visualContentRef} />
            </div>
        </VisualQueryProvider>
    );
}
