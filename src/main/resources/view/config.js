import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltip-module/TooltipModule.js';
import { ToggleModule } from './toggle-module/ToggleModule.js';

export const modules = [
    GraphicEntityModule,
    TooltipModule,
    ToggleModule
];

export const gameName = 'UltimaDungeonDelver';
export const overlayAlpha = 0;

export const options = [
    ToggleModule.defineToggle({
        toggle: 'renderMode',
        title: 'RENDER MODE',
        values: {
            'RPG SPRITES': true,
            'GEOMETRIC DEBUG': false
        },
        default: true
    })
];
