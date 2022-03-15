import { useReducer, useCallback, useRef, useEffect } from 'react';
import * as _ from 'lodash-es';

export interface State<T = any> {
    loading: boolean;
    data: T | undefined;
    error: Error | null;
}

const defaultState = {
    loading: false,
    data: undefined,
    error: null,
};

const defaultConfig = {
    setStartLoading: true,
    setEndLoading: true,
};

export type RunFunction<T = any> = (promise: Promise<T>, config?: Partial<typeof defaultConfig>) => Promise<T>;

function useSafeDispatch<T = any>(dispatch: (...args: T[]) => void) {
    const isMountedRef = useRef(false);
    useEffect(() => {
        isMountedRef.current = true;
        return () => {
            isMountedRef.current = false;
        };
    }, []);
    const safeDispatch = useCallback(
        (...args: T[]) => {
            if (isMountedRef.current) {
                dispatch(...args);
            }
        },
        [dispatch],
    );
    return {
        safeDispatch,
        isMountedRef,
    };
}

export function useAsync<T = any>(initialState?: Partial<State<T>>) {
    const finalState = {
        ...defaultState,
        ...initialState,
    };
    const [state, dispatch] = useReducer((state: State<T>, action: Partial<State<T>>) => ({ ...state, ...action }), {
        ...finalState,
    });

    const { safeDispatch, isMountedRef } = useSafeDispatch(dispatch);

    const run = useCallback<RunFunction<T>>(
        (promise, config) => {
            const finalConfig = {
                ...defaultConfig,
                ...config,
            };
            const { setStartLoading, setEndLoading } = finalConfig;
            if (setStartLoading) {
                safeDispatch({ loading: true });
            }
            return promise
                .then(data => {
                    safeDispatch({ data, error: _.cloneDeep(finalState.error) });
                    return data;
                })
                .catch(error => {
                    safeDispatch({ data: _.cloneDeep(finalState.data), error });
                    return Promise.reject(error);
                })
                .finally(() => {
                    if (setEndLoading) {
                        safeDispatch({ loading: false });
                    }
                });
        },
        [safeDispatch],
    );

    return {
        run,
        dispatch: safeDispatch,
        isMountedRef,
        ...state,
    };
}
