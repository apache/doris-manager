import { CascaderItem } from '../types';

export function processCollections(collections: CascaderItem[]): CascaderItem[] {
    return collections.map(collection => {
        return {
            ...collection,
            isLeaf: false,
            isFetched: false,
            children: processCollections(collection.children),
        };
    });
}
