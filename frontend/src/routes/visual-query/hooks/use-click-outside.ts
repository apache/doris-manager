import { useEffect } from 'react';

export function useClickOutside(domRef: React.MutableRefObject<HTMLElement | null>, callback: () => void) {
    useEffect(() => {
        const dom = domRef.current;
        const handler = (e: MouseEvent) => {
            if (dom && dom.contains(e.target as HTMLElement)) {
                return;
            }
            callback();
        };
        document.addEventListener('click', handler);
        return () => {
            document.removeEventListener('click', handler);
        };
    }, []);
}
