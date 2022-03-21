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
