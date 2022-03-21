import { useReducer, useCallback } from 'react';

interface State<T> {
    loading: boolean;
    data: T | null;
    error: Error | null;
}

const defaultState = {
    loading: false,
    data: null,
    error: null,
};

export function useAsync<T>(initialState?: Partial<State<T>>) {
    const [state, dispatch] = useReducer((state: State<T>, action: Partial<State<T>>) => ({ ...state, ...action }), {
        ...defaultState,
        ...initialState,
    });

    const run = useCallback(
        (promise: Promise<T>) => {
            dispatch({ loading: true });
            return promise
                .then(data => {
                    dispatch({ loading: false, data, error: null });
                    return data;
                })
                .catch(error => {
                    dispatch({ loading: false, data: null, error });
                    return Promise.reject(error);
                });
        },
        [dispatch],
    );

    return {
        run,
        dispatch,
        ...state,
    };
}
