import React, { useContext, useCallback } from 'react';
import { TabType, Widget, WidgetTypeEnum } from '../types';
import { ChartContext, DataContext } from '../context';

export function useRenderWidgets(currentTab?: TabType) {
    const { changeChartOptions, chartOptions } = useContext(ChartContext);
    const { data: resultData } = useContext(DataContext);
    const { data } = resultData!;

    const renderWidgets = useCallback(
        (widgets: Widget[], extraArgs?: any) => {
            let currentWidgets = widgets;
            if (currentTab) {
                currentWidgets = widgets.filter(widget => {
                    if (widget.tab == null) return true;
                    if (typeof widget.tab === 'string') {
                        return widget.tab === currentTab.name;
                    }
                    return widget.tab.includes(currentTab.name);
                });
            }
            return currentWidgets.map((widget, index) => {
                const { getHidden } = widget;
                const hidden = getHidden && getHidden(chartOptions);
                if (hidden) return null;
                switch (widget.type) {
                    case WidgetTypeEnum.RENDER:
                        return widget.render({ key: widget.name, widget: widget });
                    case WidgetTypeEnum.COMP:
                        widget = widget.preTransform ? widget.preTransform(widget, chartOptions, data) : widget;
                        const { Comp, onChange, getResetValue, getValue, getDefaultValue } = widget;
                        const resetValue = getResetValue?.(chartOptions, data);
                        const value = getValue(chartOptions, extraArgs);
                        const defaultValue = getDefaultValue?.(chartOptions);
                        return (
                            <Comp
                                key={index}
                                value={value}
                                resetValue={resetValue}
                                defaultValue={defaultValue}
                                onChange={value => {
                                    onChange({ value, chartOptions, changeChartOptions, data, extraArgs });
                                }}
                                disabled={widget.disabled}
                                placeholder={widget.placeholder}
                                label={widget.label}
                                items={widget.items}
                                options={widget.options}
                            />
                        );
                }
            });
        },
        [changeChartOptions, chartOptions, data, currentTab],
    );

    return {
        renderWidgets,
    };
}
