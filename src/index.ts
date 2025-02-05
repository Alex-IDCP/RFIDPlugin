import { registerPlugin } from '@capacitor/core';
import type { RFIDPluginPlugin } from './definitions';

const RFIDPlugin = registerPlugin<RFIDPluginPlugin>('RFIDPlugin', {
  web: () => import('./web').then(m => new m.RFIDPluginWeb()), // Optional web implementation
});

export * from './definitions';
export { RFIDPlugin };
