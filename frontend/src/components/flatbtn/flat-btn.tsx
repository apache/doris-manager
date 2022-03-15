import React, { HTMLAttributes } from 'react';
import classNames from 'classnames';
import './style.less';
import { Link } from 'react-router-dom';

interface FlatBtnProps extends HTMLAttributes<HTMLAnchorElement> {
    to?: string;
    type?: '' | 'danger' | 'warn';
    disabled?: boolean;
    children?: string | JSX.Element;
    className?: string;
    default?: string;
    key?: string | number;
    href?: string;
    [attr: string]: any;
}

const FlatBtn = (props: FlatBtnProps) => {
    if (props.to) {
        return (
            <Link
                {...props}
                to={props.to}
                className={classNames(
                    props.className && props.className,
                    { [`btn-${props.type}`]: props.type },
                    { 'flat-btn-disabled': props.disabled },
                    { 'flat-btn-default': props.default },
                )}
            >
                {props.children}
            </Link>
        );
    }
    return (
        <a
            {...props}
            onClick={e => {
                if (props.disabled) {
                    e.preventDefault();
                    e.stopPropagation();
                } else {
                    props.onClick && props.onClick(e);
                }
            }}
            className={classNames(
                props.className && props.className,
                { [`btn-${props.type}`]: props.type },
                { 'flat-btn-disabled': props.disabled },
                { 'flat-btn-default': props.default },
            )}
        >
            {props.children}
        </a>
    );
};

export default FlatBtn;
