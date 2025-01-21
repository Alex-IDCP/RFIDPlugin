export interface RFIDPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
