package org.systronik.blecommunication;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.blueradios.Brsp;
import com.blueradios.BrspCallback;


// Cordova
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Process;
import android.util.Log;

public class BLECommunication extends CordovaPlugin {

  private static final String TAG = "BLECOM";
  private static final String ACTION_SCAN = "scanDevices";
  private static final String ACTION_STOPSCAN = "stopScanDevices";
  private static final String ACTION_CONNECT = "connectDevice";
  	

private static final String ACTION_SENDDATA = "sendData";
  private BluetoothAdapter _bluetoothAdapter;
  
  private CallbackContext deviceFoundCallback;
  private CallbackContext connectedCallback;
  private CallbackContext dataAvailableCallback;
  
  private Brsp _brsp;
  private BluetoothDevice _selectedDevice;
  
  @Override
	public void onPause(boolean multitasking) {
	  Log.d(TAG, "Application Pause");
	  doDisconnect();
	  _bluetoothAdapter.stopLeScan(mLeScanCallback);  
	  super.onPause(multitasking);
	}
	
	@Override
	public void onDestroy() {
		if (_bluetoothAdapter != null){
			  _bluetoothAdapter.stopLeScan(mLeScanCallback);  
		}
		super.onDestroy();
	}
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_SCAN.equals(action)) {
    	  //JSONObject arg_object = args.getJSONObject(0);
    	  //OutString = arg_object.getString("dataToSend");
    	  	Log.d(TAG, "Scanning for devices ");
    	  	deviceFoundCallback = callbackContext;
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
    		startDiscovery(args,callbackContext);
    	  return true;
      
      } 
      else if (ACTION_STOPSCAN.equals(action)) {
    	  //JSONObject arg_object = args.getJSONObject(0);
    	  //OutString = arg_object.getString("dataToSend");
    	  Log.d(TAG, "Stop scanning");
    	  stopDiscovery();	
    	  callbackContext.success("Scanning stopped");
    	  return true;
      
      }
      else if (ACTION_CONNECT.equals(action)) {
    	  JSONObject arg_object = args.getJSONObject(0);
    	  String address = arg_object.getString("address");
    	  connectedCallback = callbackContext; 
    	  stopDiscovery();	
    	  Log.d(TAG, "Connecting...");
    	  doConnect(address);
    	  return true;
      
      }
      else if (ACTION_SENDDATA.equals(action)) {
    	  JSONObject arg_object = args.getJSONObject(0);
    	  String data = arg_object.getString("data");
    	  dataAvailableCallback = callbackContext; 
    	  Log.d(TAG, "Connecting...");
    	  _brsp.writeBytes(data.getBytes());
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

  	private BrspCallback _brspCallback = new BrspCallback() {

		@Override
		public void onSendingStateChanged(Brsp obj) {
		    Log.d(TAG, "onSendingStateChanged thread id:" + Process.myTid());
		}

		@Override
		public void onConnectionStateChanged(Brsp obj) {
			Log.d(TAG, "Connection state changed: " + obj.getConnectionState());
			PluginResult result = new PluginResult(PluginResult.Status.OK,obj.getConnectionState());
            result.setKeepCallback(true);
            connectedCallback.sendPluginResult(result);
		}
		public void onDataReceived(final Brsp obj) {
		    Log.d(TAG, "onDataReceived thread id:" + Process.myTid());
		    byte[] bytes = obj.readBytes();
		    if (bytes != null) {
			String input = new String(bytes);
			PluginResult result = new PluginResult(PluginResult.Status.OK,input);
            result.setKeepCallback(true);
            dataAvailableCallback.sendPluginResult(result);
		    } else {
			// This occasionally happens but no data should be lost
		    }
		}
  	};
  	
	private void stopDiscovery() {
	// TODO Auto-generated method stub
		if (_bluetoothAdapter != null) {		    
		    if (!_bluetoothAdapter.isDiscovering()) {
			_bluetoothAdapter.stopLeScan(mLeScanCallback);
			deviceFoundCallback = null;
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
			PluginResult result = new PluginResult(PluginResult.Status.OK, device.getAddress() + ";" + device.getName() + "\r\n");
            result.setKeepCallback(true);
            deviceFoundCallback.sendPluginResult(result);
		    Log.d(TAG, "Scan found device: " + device.getAddress());    
		};
	};
	
	private void doConnect(String address) {
		_brsp = new Brsp(_brspCallback, 10000, 10000);
		if (address != null && _brsp.getConnectionState() == BluetoothGatt.STATE_DISCONNECTED) {
		    boolean result = false;
		    _selectedDevice = _bluetoothAdapter.getRemoteDevice(address); 
		    String bondStateText = "";
		    switch (_selectedDevice.getBondState()) {
		    case BluetoothDevice.BOND_BONDED:
			bondStateText = "BOND_BONDED";
			break;
		    case BluetoothDevice.BOND_BONDING:
			bondStateText = "BOND_BONDING";
			break;
		    case BluetoothDevice.BOND_NONE:
			bondStateText = "BOND_NONE";
			break;
		    }
		    Log.d(TAG, "Bond State:" + bondStateText);

		    result = _brsp.connect(this.cordova.getActivity().getApplicationContext(), _selectedDevice);
		    Log.d(TAG, "Connect result:" + result);
		}
	}
	

	    private void doDisconnect() {
		Log.d(TAG, "Atempting to disconnect");
		if (_brsp.getConnectionState() != BluetoothGatt.STATE_DISCONNECTED)
		    _brsp.disconnect();
	    }
}