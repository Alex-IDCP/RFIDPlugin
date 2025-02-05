package com.idcpmobile.rfidplugin;

import android.content.Context;
import android.util.Log;

import com.century.sdk.rfid.common.connect.ConnectType;
import com.century.sdk.rfid.common.connect.HandsetConnectParam;
import com.century.sdk.rfid.protocol.CReader;
import com.century.sdk.rfid.protocol.factory.ReaderAdapterFactory;
import com.century.sdk.rfid.protocol.listener.KeyboardEventListener;
import com.century.sdk.rfid.protocol.listener.ReaderListener;
import com.century.sdk.rfid.protocol.model.bo.DeviceParam;
import com.century.sdk.rfid.protocol.model.bo.EpcTag;
import com.century.sdk.rfid.protocol.model.enums.DeviceType;
import com.century.sdk.rfid.protocol.model.enums.OperateResult;
import com.getcapacitor.PluginMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RFIDPlugin {

  private static final String TAG = "RFIDPlugin";
  private final List<RfidData> scannedTag = new ArrayList<>();
  private CReader cReader;
  private boolean isConnected = false;
  ReaderListener readerListener = null;
  KeyboardEventListener keyboardEventListener;

  public RFIDPlugin(Context context){
    // Initialize the RFID Reader
    cReader = ReaderAdapterFactory.create(DeviceType.CER78, ConnectType.HANDSET, new HandsetConnectParam(context));
  }

  public boolean initReader(){
    if (cReader != null){
      Log.i(TAG, "Reader initialized successfully");
      return true;
    }
    else{
      Log.e(TAG, "Failed to initialize the RFID Reader");
      return false;
    }
  }

  public boolean connect(){
    if (cReader == null)
      return false;

    OperateResult result = cReader.connect();
    isConnected = result.isSuccess();

    if (isConnected){
      Log.i(TAG, "Connected to RFID Reader");
    }
    else {
      Log.e(TAG, "Failed to connect to RFID Reader : " + result.name());
    }

    return isConnected;
  }

  public void disconnect(){
    if (isConnected && cReader != null){
      cReader.disconnect();
      isConnected = false;
      Log.i(TAG, "Disconnected from RFID Reader");
    }
    else {
      Log.e(TAG, "RFID Reader is not connected");
    }
  }

  public boolean startInventory(){
    if (!isConnected || cReader == null){
      Log.e(TAG, "RFID Reader is not connected");
      return false;
    }
    else {
      cReader.startInventory(true);
      Log.i(TAG, "Inventory Starting to scan...");
      return true;
    }
  }

  public boolean stopInventory(){
    scannedTag.clear();
    if(!isConnected || cReader == null){
      Log.e(TAG, "RFID Reader is not connected");
      return false;
    }
    else {
      cReader.stopInventory();

      Log.i(TAG, "Inventory stopped to scan...");
      return true;
    }
  }

  public List<RfidData> getScannedTags(){
    // Return a copy to avoid modification
    return new ArrayList<>(scannedTag);
  }

  public DeviceParam getDeviceParams(){
    if (!isConnected || cReader == null){
      Log.e(TAG, "RFID Reader is not connected");
      return null;
    }
    else {
      return cReader.getDeviceParam();
    }
  }

  public boolean setDeviceParams(DeviceParam params){
    if (!isConnected || cReader == null){
      Log.e(TAG, "RFID Reader is not connected");
      return false;
    }
    else {
      OperateResult result = cReader.setDeviceParam(params);
      if (result.isSuccess()){
        Log.i(TAG, "Device Parameters set successfully");
        return true;
      }
      else {
        Log.e(TAG, "Failed to set device parameters : " + result.name());
        return false;
      }
    }
  }

  public String getDeviceID(){
    if (!isConnected || cReader == null){
      Log.e(TAG, "RFID Reader is not connected");
      return null;
    }
    else
      return cReader.getDeviceId();
  }

  public boolean isConnected(){
    return cReader.isConnected();
  }

  public void setupReaderListener(boolean isCon){
    if (!isCon && readerListener != null){
      Log.e(TAG, "RFID Reader is not connected");
      cReader.removeReaderListener(readerListener);
      readerListener = null;
      return;
    }

    if (readerListener == null){
      readerListener = new ReaderListener() {
        @Override
        public void readEpcTag(EpcTag epcTag) {
          String epc = epcTag.getEpc();
          int index = -1;

          // Check if the tag already exists in the list
          for (int i = 0; i < scannedTag.size(); i++) {
            if (scannedTag.get(i).getEpc().equals(epc)) {
              index = i;
              break;
            }
          }

          if (index != -1) {
            // Tag already exists, update its count
            RfidData existingTag = scannedTag.get(index);
            existingTag.setNum(existingTag.getNum() + 1); // Increment the count
            existingTag.setRssi(epcTag.getRssi()); // Update RSSI
            scannedTag.set(index, existingTag); // Update the list
          } else {
            // Add new tag to the list
            RfidData newTag = new RfidData();
            newTag.setEpc(epc);
            newTag.setTid(epcTag.getTid());
            newTag.setNum(1); // Initial count is 1
            newTag.setRssi(epcTag.getRssi());
            scannedTag.add(newTag);
          }

          // Log the tag and show a toast
          Log.d(TAG, "Scanned tag: " + epc);
          // Post the Toast display to the main thread:
//        new Handler(Looper.getMainLooper()).post(() ->
//          Toast.makeText(getContext(), "Scanned tag: " + epc, Toast.LENGTH_SHORT).show()
//        );
        }

        @Override
        public void deviceOffline() {
          isConnected = false;
          Log.e(TAG, "Device went offline.");
        }
      };
      // Add the listener to the reader
      cReader.addReaderListener(readerListener);
    }
  }

  public void setupKeyboardListener(boolean isCon){
    if (!isCon){
      if (keyboardEventListener != null){
        cReader.removeKeyboardEventListener(keyboardEventListener);
        keyboardEventListener = null;
      }
      return;
    }
    if (keyboardEventListener == null){
      keyboardEventListener = new KeyboardEventListener() {
        @Override
        public void onKeyDown(int keyCode) {
          // Optionally log key down events
          Log.d(TAG, "Key down at " + new Date() + " : " + keyCode);
        }

        @Override
        public void onKeyUp(int keyCode) {
          Log.d(TAG, "Key up at " + new Date() + " : " + keyCode);
          if (keyCode == 250) {
            // Toggle the inventory state
            if (!cReader.isInventory()) {
              startInventory(); // Start scanning if not already scanning
            } else {
              stopInventory(); // Stop scanning if already scanning
            }
          }
        }
      };
      cReader.addKeyboardEventListener(keyboardEventListener);
    }
  }

  public boolean singleRead(){
    if (!isConnected || cReader == null){
      Log.e(TAG, "RFID Readeer is not connected");
      return false;
    }

    scannedTag.clear();
    cReader.singleInventory(true);
    Log.i(TAG, "Single Inventory Initiated");
    return true;
  }

}
