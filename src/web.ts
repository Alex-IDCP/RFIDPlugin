import { WebPlugin } from '@capacitor/core';
import type { RFIDPluginPlugin } from './definitions';

export class RFIDPluginWeb extends WebPlugin implements RFIDPluginPlugin {

  async echo(options: { value: string }): Promise<{ value: string }> {
    throw new Error('RFIDPlugin is not supported on the web.');
  }

  async initReader(): Promise<{ success: boolean; message: string }> {
    throw new Error('Web platform not supported.');
  }

  async connect(): Promise<{ status: string }> {
    throw new Error('Web platform not supported.');
  }

  async disconnect(): Promise<{ status: string }> {
    throw new Error('Web platform not supported.');
  }

  async startInventory(): Promise<{ status: string }> {
    throw new Error('Web platform not supported.');
  }

  async stopInventory(): Promise<{ status: string }> {
    throw new Error('Web platform not supported.');
  }

  async getScannedTags(): Promise<{ tags: string[] }> {
    throw new Error('Web platform not supported.');
  }

  async getDeviceID(): Promise<{ deviceID: string }> {
    throw new Error('Web platform not supported.');
  }

  async getDeviceParams(): Promise<{
    readPower: number;
    writePower: number;
    q: number;
    frequency: string;
    session: number;
    target: number;
  }> {
    throw new Error('Web platform not supported.');
  }

  async setDeviceParams(options: {
    readPower?: number;
    writePower?: number;
    q?: number;
    frequency?: string;
    session?: number;
    target?: number;
  }): Promise<{ success: boolean; message: string }> {
    throw new Error('Web platform not supported.');
  }

  async getConnectState(): Promise<{ isConnected: boolean; status: string }> {
    throw new Error('Web platform not supported.');
  }

  async singleRead(): Promise<{ status: string; tags: string[] }> {
    throw new Error('Web platform not supported.');
  }

  addListener(): any {
    throw new Error('RFIDPlugin is not supported on the web.');
  }
}

