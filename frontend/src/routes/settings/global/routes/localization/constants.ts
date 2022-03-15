export const DEFAULT_DATE_STYLE_OPTIONS = [
    { value: 'MMMM D, YYYY', completeLabel: '一月 7, 2018', compressedLabel: '1月, 7, 2018' },
    { value: 'D MMMM, YYYY', completeLabel: '7 一月, 2018', compressedLabel: '7 1月, 2018' },
    { value: 'dddd, MMMM D, YYYY', completeLabel: '星期日, 一月 7, 2018', compressedLabel: '周日, 1月 7, 2018' },
    { value: 'M/D/YYYY', label: [1, 7, 2018] },
    { value: 'D/M/YYYY', label: [7, 1, 2018] },
    { value: 'YYYY/M/D', label: [2018, 1, 7] },
];

export const DATE_SEPARATOR_OPTIONS = [
    { value: '/', label: 'M/D/YYYY' },
    { value: '-', label: 'M-D-YYYY' },
    { value: '.', label: 'M.D.YYYY' },
];

export const TIME_STYLE_OPTIONS = [
    { value: 'h:mm A', label: '5:24 下午 (12小时制)' },
    { value: 'k:mm', label: '17:24 (24小时制)' },
];

export const NUMBER_SEPARATORS_OPTIONS = [
    { value: '.,', label: '100,000.00' },
    { value: ', ', label: '100 000,00' },
    { value: ',.', label: '100.000,00' },
    { value: '.', label: '100000.00' },
];
