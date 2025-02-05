import { PluginListenerHandle } from "@capacitor/core";

export interface RFIDPluginPlugin {

  echo(options: { value: string }): Promise<{ value: string }>;

  initReader(): Promise<{ success: boolean; message: string }>;

  connect(): Promise<{ status: string }>;

  disconnect(): Promise<{ status: string }>;

  startInventory(): Promise<{ status: string }>;

  stopInventory(): Promise<{ status: string }>;

  getScannedTags(): Promise<{ tags: string[] }>;

  getDeviceID(): Promise<{ deviceID: string }>;

  getDeviceParams(): Promise<{
    readPower: number;
    writePower: number;
    q: number;
    frequency: string;
    session: number;
    target: number;
  }>;

  setDeviceParams(options: {
    readPower?: number;
    writePower?: number;
    q?: number;
    frequency?: string;
    session?: number;
    target?: number;
  }): Promise<{ success: boolean; message: string }>;

  getConnectState(): Promise<{ isConnected: boolean; status: string }>;

  singleRead(): Promise<{ status: string; tags: string[] }>;

  // Add the listener for 'inventoryAction'
  addListener(
    eventName: 'inventoryAction',
    listenerFunc: (data: { status: string }) => void
  ): PluginListenerHandle;
  
}
