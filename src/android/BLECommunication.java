package org.systronik.blecommunication;

import org.apache.cordova.CallbackContext;


// Cordova
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BLECommunication extends CordovaPlugin {

  private static final String TAG = "BLECOM";
  private static final String ACTION_SCAN = "scanDevices";
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_SCAN.equals(action)) {
    	  //JSONObject arg_object = args.getJSONObject(0);
    	  //OutString = arg_object.getString("dataToSend");
    	  Log.d(TAG, "Scanning for devices ");
    	  return true;
      
      } else {
        callbackContext.error("Function: " + action + " is not a supported function");
        return false;
      }
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
      return false;
    }
  }
}