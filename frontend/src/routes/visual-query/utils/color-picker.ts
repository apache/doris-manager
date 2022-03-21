const COLORS = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'];

const COLORS_MAP = COLORS.reduce((memo, current) => {
    memo[current] = 0;
    return memo;
}, {});

class ColorPicker {
    colors: string[] = COLORS;
    colorsMap: Record<string, number> = COLORS_MAP;
    index: number = 0;
    restCount: number = COLORS.length;
    pickInOrder() {
        if (this.restCount === 0) {
            const color = this.colors[this.index % this.colors.length];
            this.colorsMap[color]++;
            this.index++;
            return color;
        }
        const color = Object.keys(this.colorsMap).find(color => this.colorsMap[color] === 0) as string;
        this.colorsMap[color]++;
        this.restCount--;
        this.index = this.colors.indexOf(color) + 1;
        return color;
    }
    pick(color: string) {
        this.colorsMap[color]++;
        if (this.colorsMap[color] === 1) {
            this.restCount--;
        }
        this.index = this.colors.indexOf(color) + 1;
    }
    reset() {
        Object.keys(this.colorsMap).forEach(key => {
            this.colorsMap[key] = 0;
        });
        this.index = 0;
        this.restCount = this.colors.length;
    }
}

export const colorPicker = new ColorPicker();
