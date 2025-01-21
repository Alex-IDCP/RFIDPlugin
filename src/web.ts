import { WebPlugin } from '@capacitor/core';

import type { RFIDPluginPlugin } from './definitions';

export class RFIDPluginWeb extends WebPlugin implements RFIDPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
