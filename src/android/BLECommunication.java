package org.systronik.blecommunication;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

// Cordova
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class BLECommunication extends CordovaPlugin {

  private static final String TAG = "BLECOM";
  private static final String ACTION_SCAN = "scanDevices";
  private static final String ACTION_STOPSCAN = "stopScanDevices";
  private BluetoothAdapter _bluetoothAdapter;
  
  private CallbackContext deviceFoundCallback;
  private CallbackContext connectedCallback;
  private CallbackContext dataAvailableCallback;
  
    
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_SCAN.equals(action)) {
    	  JSONObject arg_object = args.getJSONObject(0);
    	  //OutString = arg_object.getString("dataToSend");
    	  	Log.d(TAG, "Scanning for devices ");
    	  	callbackContext.success("Hello");
    	  	_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	  	if (!_bluetoothAdapter.isEnabled()) {
    	  		Log.d(TAG, "Adapter disabled");
//    	         Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//    	         startActivityForResult(turnOn, 0);
//    	         Toast.makeText(getApplicationContext(),"Turned on" 
//    	         ,Toast.LENGTH_LONG).show();
    	      }
    	      else{
    	    	 Log.d(TAG, "Adapter enabled");
//    	         Toast.makeText(getApplicationContext(),"Already on",
//    	         Toast.LENGTH_LONG).show();
    	         }
    	  	callbackContext.success("System started");
    		startDiscovery(args,callbackContext);
    	  return true;
      
      } 
      else if (ACTION_STOPSCAN.equals(action)) {
    	  //JSONObject arg_object = args.getJSONObject(0);
    	  //OutString = arg_object.getString("dataToSend");
    	  Log.d(TAG, "Stop scanning");
    	  stopDiscovery();	
    	  return true;
      
      }
      
      else {
        callbackContext.error("Function: " + action + " is not a supported function");
        return false;
      }
       
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
      return false;
    }
  }

	private void stopDiscovery() {
	// TODO Auto-generated method stub
		if (_bluetoothAdapter != null) {		    
		    if (!_bluetoothAdapter.isDiscovering()) {
			_bluetoothAdapter.stopLeScan(mLeScanCallback);
		    }
		} else {
		    Log.e(TAG, "Bluetooth adapter is null");
		}
	}

	private void startDiscovery(JSONArray args, CallbackContext callbackContext) throws JSONException {
			deviceFoundCallback = callbackContext;
			if (_bluetoothAdapter != null) {
			    if (!_bluetoothAdapter.isEnabled())
				_bluetoothAdapter.enable();
			    if (!_bluetoothAdapter.isDiscovering()) {
				_bluetoothAdapter.stopLeScan(mLeScanCallback);
			    }
			    Log.d(TAG, "Starting scan");
			    _bluetoothAdapter.startLeScan(mLeScanCallback);
			} else {
			    Log.e(TAG, "Bluetooth adapter is null");
			}

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
    }
		
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
			//deviceFoundCallback.success(device.getAddress());
//			PluginResult result = new PluginResult(PluginResult.Status.OK, device.getAddress());
//            result.setKeepCallback(true);
//            deviceFoundCallback.sendPluginResult(result);
		    Log.d(TAG, "Scan found device: " + device.getAddress());    
		};
	};
}