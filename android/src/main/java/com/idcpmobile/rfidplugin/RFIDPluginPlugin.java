package com.idcpmobile.rfidplugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.century.sdk.rfid.common.connect.ConnectType;
import com.century.sdk.rfid.common.connect.HandsetConnectParam;
import com.century.sdk.rfid.protocol.factory.ReaderAdapterFactory;
import com.century.sdk.rfid.protocol.model.enums.DeviceType;

import com.century.sdk.rfid.protocol.CReader;
import com.century.sdk.rfid.protocol.listener.KeyboardEventListener;
import com.century.sdk.rfid.protocol.listener.ReaderListener;
import com.century.sdk.rfid.protocol.model.bo.DeviceParam;
import com.century.sdk.rfid.protocol.model.bo.EpcTag;
import com.century.sdk.rfid.protocol.model.enums.Frequency;
import com.century.sdk.rfid.protocol.model.enums.OperateResult;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CapacitorPlugin(name = "RFIDPlugin")
public class RFIDPluginPlugin extends Plugin {

  private static final String TAG = "RFIDMobile";
  private final List<RfidData> scannedTags = new ArrayList<>();
  private CReader cReader;
  private boolean isConnected = false;
  private ReaderListener readerListener;
  private KeyboardEventListener keyboardEventListener;
//  private Toast currentToast;

  @Override
  public void load() {
    super.load();
  }

  @PluginMethod
  public void initReader(PluginCall call) {
    Context context = getContext();
    if (cReader == null) {
      cReader = ReaderAdapterFactory.create(DeviceType.CER78, ConnectType.HANDSET, new HandsetConnectParam(context));
    }

    if (cReader != null) {
      call.resolve(new JSObject().put("success", true).put("message", "Reader initialized successfully"));
    } else {
      call.reject("Failed to initialize the RFID reader.");
    }
  }

  @PluginMethod
  public void connect(PluginCall call) {
    OperateResult result = cReader.connect();
    isConnected = result.isSuccess();

    if (isConnected) {
      setupReaderListener(true);
      setupKeyboardListener(true);
      JSObject response = new JSObject();
      response.put("status", "connected");
      call.resolve(response);
      Log.d(TAG, "Connected to RFID reader.");
    } else {
      call.reject("Failed to connect to RFID reader.");
      Log.e(TAG, "Failed to connect to RFID reader: " + result.name());
    }
  }

  @PluginMethod
  public void disconnect(PluginCall call) {
    if (isConnected && cReader != null) {
      cReader.disconnect();
      scannedTags.clear();
      setupReaderListener(false);
      setupKeyboardListener(false);
      isConnected = false;
      JSObject response = new JSObject();
      response.put("status", "disconnected");
      call.resolve(response);
      Log.d(TAG, "Disconnected from RFID reader.");
    } else {
      call.reject("RFID reader is not connected.");
    }
  }

  @PluginMethod
  public void startInventory(PluginCall call) {
    if (!isConnected) {
      call.reject("RFID reader is not connected.");
      return;
    }
    // Ensure scannedTags is empty before starting inventory
    scannedTags.clear();

    cReader.startInventory(true); // Set to `false` if no TID required
    JSObject response = new JSObject();
    response.put("status", "inventory_started");
    call.resolve(response);
    Log.d(TAG, "Inventory started.");
  }

  @PluginMethod
  public void stopInventory(PluginCall call) {
    if (!isConnected) {
      call.reject("RFID reader is not connected.");
      return;
    }

    cReader.stopInventory();
    scannedTags.clear();
    JSObject response = new JSObject();
    response.put("status", "inventory_stopped");
    call.resolve(response);
    Log.d(TAG, "Inventory stopped.");
  }

  @PluginMethod
  public void getScannedTags(PluginCall call) {
    JSArray tagsArray = new JSArray();

    for (RfidData tag : scannedTags) {
      JSObject tagObject = new JSObject();
      tagObject.put("epc", tag.getEpc());
      tagObject.put("tid", tag.getTid());
      tagObject.put("rssi", tag.getRssi());
      tagObject.put("num", tag.getNum());
      tagsArray.put(tagObject);
    }

    JSObject result = new JSObject();
    result.put("tags", tagsArray);
    call.resolve(result);
  }

  private void setupReaderListener(boolean isCon) {

    if (!isCon && readerListener != null){
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
          for (int i = 0; i < scannedTags.size(); i++) {
            if (scannedTags.get(i).getEpc().equals(epc)) {
              index = i;
              break;
            }
          }

          if (index != -1) {
            // Tag already exists, update its count
            RfidData existingTag = scannedTags.get(index);
            existingTag.setNum(existingTag.getNum() + 1); // Increment the count
            existingTag.setRssi(epcTag.getRssi()); // Update RSSI
            scannedTags.set(index, existingTag); // Update the list
          } else {
            // Add new tag to the list
            RfidData newTag = new RfidData();
            newTag.setEpc(epc);
            newTag.setTid(epcTag.getTid());
            newTag.setNum(1); // Initial count is 1
            newTag.setRssi(epcTag.getRssi());
            scannedTags.add(newTag);
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

  private void setupKeyboardListener(boolean isCon) {
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

  private void startInventory() {
    Log.d(TAG, "Starting inventory...");
    // Start inventory; the boolean parameter may depend on whether you need TID reading, etc.
    cReader.startInventory(true);
    // Optionally, notify your application/UI that scanning has started.
    JSObject data = new JSObject();
    data.put("status", "started");
    notifyListeners("inventoryAction", data);
  }

  private void stopInventory() {
    Log.d(TAG, "Stopping inventory...");
    scannedTags.clear();
    cReader.stopInventory();
    // Optionally, notify your application/UI that scanning has stopped.
    JSObject data = new JSObject();
    data.put("status", "stopped");
    notifyListeners("inventoryAction", data);
  }

  private void removeListeners() {
    if (readerListener != null) {
      cReader.removeReaderListener(readerListener);
      readerListener = null;
    }
    if (keyboardEventListener != null) {
      cReader.removeKeyboardEventListener(keyboardEventListener);
      keyboardEventListener = null;
    }
  }

  @PluginMethod
  public void getDeviceID(PluginCall call) {
    if (cReader == null || !cReader.isConnected()) {
      call.reject("RFID reader is not connected.");
      return;
    }

    String deviceID = cReader.getDeviceId();
    if (deviceID != null) {
      JSObject response = new JSObject();
      response.put("deviceID", deviceID);
      call.resolve(response);
    } else {
      call.reject("Failed to retrieve device ID.");
    }
  }

  @PluginMethod
  public void getConnectState(PluginCall call) {
    JSObject response = new JSObject();
    if (cReader != null && cReader.isConnected()) {
      response.put("isConnected", true);
      response.put("status", "connected");
    } else {
      response.put("isConnected", false);
      response.put("status", "disconnected");
    }
    call.resolve(response);
  }

  @PluginMethod
  public void getDeviceParams(PluginCall call) {
    if (cReader == null || !cReader.isConnected()) {
      call.reject("RFID reader is not connected.");
      return;
    }

    DeviceParam params = cReader.getDeviceParam();
    if (params != null) {
      JSObject response = new JSObject();
      response.put("readPower", params.getReadPower());
      response.put("writePower", params.getWritePower());
      response.put("q", params.getQ());
      response.put("frequency", params.getFrequency().name());
      response.put("session", params.getSession());
      response.put("target", params.getTarget());
      call.resolve(response);
    } else {
      call.reject("Failed to retrieve device parameters.");
    }
  }

  @PluginMethod
  public void setDeviceParams(PluginCall call) {
    if (cReader == null || !cReader.isConnected()) {
      call.reject("RFID reader is not connected.");
      return;
    }

    // Safely retrieve values with null checks
    Integer readPower = call.getInt("readPower");
    Integer writePower = call.getInt("writePower");
    Integer q = call.getInt("q");
    String frequency = call.getString("frequency", "US_BAND");
    Integer session = call.getInt("session");
    Integer target = call.getInt("target");

    // Provide default values if null
    if (readPower == null) readPower = 30; // Default to 30
    if (writePower == null) writePower = 30; // Default to 30
    if (q == null) q = 0; // Default to 0
    if (session == null) session = 0; // Default to 0
    if (target == null) target = 0; // Default to 0

    // Create and set device parameters
    DeviceParam params = new DeviceParam();
    params.setReadPower(readPower);
    params.setWritePower(writePower);
    params.setQ(q);
    params.setFrequency(Frequency.valueOf(frequency));
    params.setSession(session);
    params.setTarget(target);

    OperateResult result = cReader.setDeviceParam(params);

    if (result.isSuccess()) {
      JSObject response = new JSObject();
      response.put("success", true);
      response.put("message", "Device parameters set successfully.");
      call.resolve(response);
    } else {
      call.reject("Failed to set device parameters: " + result.name());
    }
  }

  @PluginMethod
  public void singleRead(PluginCall call) {
    if (cReader == null || !cReader.isConnected()) {
      call.reject("RFID reader is not connected.");
      return;
    }

    // Clear previously scanned tags
    scannedTags.clear();

    // Initiate single read.
    // Adjust the boolean parameter as needed (true if TID is required).
    cReader.singleInventory(true);
    Log.d(TAG, "Single inventory initiated.");

    // Use a Handler to delay the response so that the readEpcTag callback can capture the result.
    // You may adjust the delay (e.g., 1000 ms) based on your hardware timing.
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      JSArray tagsArray = new JSArray();
      for (RfidData tag : scannedTags) {
        JSObject response = new JSObject();
        response.put("epc", tag.getEpc());
        response.put("tid", tag.getTid());
        response.put("rssi", tag.getRssi());
        response.put("num", tag.getNum());
        tagsArray.put(response);
      }
      JSObject result = new JSObject();
      result.put("status", "single_read_complete");
      result.put("tags", tagsArray);
      call.resolve(result);
    }, 1000); // Delay of 1 second
  }



}
