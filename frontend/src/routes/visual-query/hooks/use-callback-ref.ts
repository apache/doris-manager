import { useRef, useCallback } from 'react';

export function useCallbackRef(fn: (...args: any[]) => any) {
    const fnRef = useRef(fn);
    fnRef.current = fn;
    return useCallback((...args: any[]) => fnRef.current(...args), []);
}
