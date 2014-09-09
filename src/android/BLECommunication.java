package org.systronik.blecommunication;

import java.util.Formatter;

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
import android.widget.Toast;

public class BLECommunication extends CordovaPlugin {

  private static final String TAG = "BLECOM";
  private static final String ACTION_CHECKAVAILABILITY = "checkAvailability";
  private static final String ACTION_SCAN = "scanDevices";
  private static final String ACTION_STOPSCAN = "stopScanDevices";
  private static final String ACTION_CONNECT = "connectDevice"; 
  private static final String ACTION_DISCONNECT = "disconnectDevice"; 
  private static final String ACTION_SENDDATA = "sendData";

  
  
  private BluetoothAdapter _bluetoothAdapter;
  
  private CallbackContext deviceFoundCallback;
  private CallbackContext connectionCallback;
  private CallbackContext dataAvailableCallback;
  
  private Brsp _brsp;
  private BluetoothDevice _selectedDevice;
  
  @Override
	public void onPause(boolean multitasking) {
	  Log.d(TAG, "Application Pause");
	  if (_bluetoothAdapter != null){
		  _bluetoothAdapter.stopLeScan(mLeScanCallback);  
	  }
	  
	  super.onPause(multitasking);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "Application Destroyed");
		if (_bluetoothAdapter != null){
			_bluetoothAdapter.stopLeScan(mLeScanCallback);  
		}
		super.onDestroy();
	}
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
		if (ACTION_CHECKAVAILABILITY.equals(action)) {
		  	Log.d(TAG, "Init and check BLE");
		  	_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		  	if (!_bluetoothAdapter.isEnabled()) {
		  		Log.d(TAG, "Adapter disabled");
		  		callbackContext.success("BLE disabled");
	//        	         Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	//        	         this.cordova.getActivity().startActivityForResult(turnOn, 0);
	//        	         Toast.makeText(this.cordova.getActivity().getApplicationContext(),"Turned on" 
	//        	         ,Toast.LENGTH_LONG).show();
		      }
		      else{
		    	 Log.d(TAG, "Adapter enabled");
		    	 callbackContext.success("BLE enabled");
	//        	         Toast.makeText(getApplicationContext(),"Already on",
	//        	         Toast.LENGTH_LONG).show();
		      }
	    	  return true;
	    }
		else if (ACTION_SCAN.equals(action)) {
    	  	Log.d(TAG, "Scanning for devices ");
    	  	if (_bluetoothAdapter != null){
    	  		if (!_bluetoothAdapter.isEnabled()){
    	  			callbackContext.error("BLE Disabled");
    	  		}
    	  		else{
    	  			deviceFoundCallback = callbackContext;  	  	
    	    		startDiscovery();
    	  		}
    	  	}
    	  	else{
    	  		callbackContext.error("BLE not initialised");
    	  	}
    	  	return true;
    	  	
	    } 
		else if (ACTION_STOPSCAN.equals(action)) {
			Log.d(TAG, "Stop scanning");
			stopDiscovery();	
			callbackContext.success("Scanning stopped");
			return true;
		
		}
		else if (ACTION_CONNECT.equals(action)) {
			 JSONObject arg_object = args.getJSONObject(0);
			 String address = arg_object.getString("address");
			 connectionCallback = callbackContext; 
			 stopDiscovery();	
			 Log.d(TAG, "Connecting...");
			 doConnect(address);
			 return true;
		  
		}
		else if (ACTION_DISCONNECT.equals(action)) {
			connectionCallback = callbackContext; 
			stopDiscovery();	
			doDisconnect();
			Log.d(TAG, "Disconnect");
			return true;
  
		}
		else if (ACTION_SENDDATA.equals(action)) {
			JSONObject arg_object = args.getJSONObject(0);
			String data = arg_object.getString("data");
			dataAvailableCallback = callbackContext;
			sendData(data);
			return true;
		}
		else {
			callbackContext.error("Function: " + action + " is not a supported function");
			return false;
		}
       
    } 
    catch (Exception e) {
      callbackContext.error(e.getMessage());
      return false;
    }
  }

  private BrspCallback _brspCallback = new BrspCallback() {
	@Override
	public void onSendingStateChanged(Brsp obj) {
	    Log.d(TAG, "Sending state changed");
	}
	@Override
	public void onConnectionStateChanged(Brsp obj) {
		int connectionState = obj.getConnectionState(); 
		String stateString = "";
		
		if (connectionState == BluetoothGatt.STATE_CONNECTED){
			stateString = "Connected";
		}
		else if (connectionState == BluetoothGatt.STATE_CONNECTING){
			stateString = "Connecting...";
		}
		else if (connectionState == BluetoothGatt.STATE_DISCONNECTED){
			stateString = "Disconnected";
		}
		else if (connectionState == BluetoothGatt.STATE_DISCONNECTING){
			stateString = "Disconnecting";
		}
		Log.d(TAG, "Connection state changed: " + stateString);
		
		PluginResult result = new PluginResult(PluginResult.Status.OK,stateString);
	    result.setKeepCallback(true);
	    connectionCallback.sendPluginResult(result);
	}
	public void onDataReceived(final Brsp obj) {
	    Log.d(TAG, "Data received");
		byte[] bytes = obj.readBytes();
		if (bytes != null) {
			StringBuilder sb = new StringBuilder(bytes.length * 2);  
			Formatter formatter = new Formatter(sb);  
			for (int x = 0; x < bytes.length; x++) {  
				formatter.format("%02x", bytes[x]);  
			}  
			String data = new String();
			data = sb.toString();
			formatter.close();
			PluginResult result = new PluginResult(PluginResult.Status.OK,data);
		    result.setKeepCallback(true);
		    dataAvailableCallback.sendPluginResult(result);
		} 
		else {
		// This occasionally happens but no data should be lost
		}
	}
  };
  	
  private void stopDiscovery() {
	if (_bluetoothAdapter != null) {		    
	    if (!_bluetoothAdapter.isDiscovering()) {
			_bluetoothAdapter.stopLeScan(mLeScanCallback);
			deviceFoundCallback = null;
	    }
	} else {
	    Log.e(TAG, "Bluetooth adapter is null");
	}
  }

  private void startDiscovery() throws JSONException {
	if (_bluetoothAdapter != null) {
	    if (!_bluetoothAdapter.isEnabled())
		_bluetoothAdapter.enable();
	    if (!_bluetoothAdapter.isDiscovering()) {
	    	_bluetoothAdapter.stopLeScan(mLeScanCallback);
	    }
	    Log.d(TAG, "Starting scan");
	    _bluetoothAdapter.startLeScan(mLeScanCallback);   
	} 
	else {
	    Log.e(TAG, "Bluetooth adapter is null");
	}

    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    deviceFoundCallback.sendPluginResult(result);
  }
		
  private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
	@Override
	public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
		String deviceInfo = device.getAddress() + ";" + device.getName();
		PluginResult result = new PluginResult(PluginResult.Status.OK, deviceInfo);
        result.setKeepCallback(true);
        deviceFoundCallback.sendPluginResult(result);
	    Log.d(TAG, "Device found: " + deviceInfo);    
	};
  };
	
  private void doConnect(String address) {
	_brsp = new Brsp(_brspCallback, 10000, 10000);
	if (_bluetoothAdapter != null && address != null && _brsp.getConnectionState() == BluetoothGatt.STATE_DISCONNECTED) {
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
	    Log.d(TAG, "Device " + address + " Bond State:" + bondStateText);
	    result = _brsp.connect(this.cordova.getActivity().getApplicationContext(), _selectedDevice);
	    Log.d(TAG, "Connect result:" + result);
	}
  }
	
  private void doDisconnect() {
	Log.d(TAG, "Atempting to disconnect");
	if (_brsp.getConnectionState() != BluetoothGatt.STATE_DISCONNECTED)
	    _brsp.disconnect();
	}
  private void sendData(String data){
	  if (_bluetoothAdapter != null && data != null && _brsp.getConnectionState() == BluetoothGatt.STATE_CONNECTED) {
		  _brsp.writeBytes(data.getBytes());    
	  }	  
  }
}

