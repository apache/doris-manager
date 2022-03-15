import React, { PropsWithChildren } from 'react';
import classnames from 'classnames';
import styles from './index.module.less';

type Status = 'success' | 'warning' | 'error' | 'info' | 'deactivated';

export interface StatusMarkProps {
    status: Status;
}

export default function StatusMark(props: PropsWithChildren<StatusMarkProps>) {
    const { status, children } = props;

    const markClassName = classnames(styles.mark, styles[`mark-${status}`]);

    return (
        <div className={styles.wrapper}>
            <div className={markClassName}></div>
            {children}
        </div>
    );
}
