package org.apache.cordova.sayang;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static org.apache.cordova.engine.SystemWebViewEngine.TAG;

/**
 * This class echoes a string called from JavaScript.
 */
public class MeidaSayang extends CordovaPlugin {
  private CallbackContext callbackContext;
  PluginResult result;
  DatabaseHelper myDb;

  @Override
  public Bundle onSaveInstanceState() {
    return super.onSaveInstanceState();
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    String data = cordova.getActivity().getIntent().getStringExtra("zemmy");
    if (data != null){
      result = new PluginResult(PluginResult.Status.OK, data);
    //callbackContext.success(data);
    result.setKeepCallback(true);
    callbackContext.sendPluginResult(result);
    }
  }
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    myDb = new DatabaseHelper(cordova.getContext());

    this.callbackContext = callbackContext;

    if (action.equals("coolMethod")) {
      String param = args.getString(0);
      coolMethod(param);
      return true;
    }
    if (action.equals("insertUser")) {
      String param = args.getString(0);
      insertUser(param);
      return true;
    }
    if (action.equals("deleteUser")) {
      deleteUser();
      return true;
    }
    if (action.equals("getUser")) {
      getUser();
      return true;
    }

    return false;
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    String data = intent.getStringExtra("zemmy");
    if (data != null){
      result = new PluginResult(PluginResult.Status.OK, data);
      //callbackContext.success(data);
      result.setKeepCallback(true);
    callbackContext.sendPluginResult(result);
    }
  }

  private void coolMethod(String message) throws JSONException {
    myDb.truncateDataPusher();
    myDb.insertDataPusher(message);
    Intent intentServive = new Intent(cordova.getActivity(), service.class);
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//      cordova.getContext().startForegroundService(intentServive);
//    } else {
//      cordova.getContext().startService(intentServive);
//    }
    try {
      cordova.getContext().startService(intentServive);
    }catch ( Exception e1){
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        cordova.getContext().startForegroundService(intentServive);
      }else {
        //Crashlytics.log("crash for first time, trying another.");
        cordova.getContext().startService(intentServive);
      }
    }

    Intent intent = new Intent(cordova.getActivity(), Receiver.class);
    intent.setAction("org.apache.cordova.sayang.BroadcastReceiver");
    PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 1, intent, 0);
    cordova.getActivity().sendBroadcast(intent);
  }

  private void insertUser(String message) {
    boolean isInserted = myDb.insertData(message);
    if (isInserted == true)
      callbackContext.success("success");
    else
      callbackContext.error("failed");
  }
  private void deleteUser() {
    myDb.truncateData();
    callbackContext.success("success");
  }
  private void getUser() {

    JSONArray arr = cur2Json(myDb.getAllData());
    callbackContext.success(arr.toString());
  }
  public JSONArray cur2Json(Cursor cursor) {

    JSONArray resultSet = new JSONArray();
    cursor.moveToFirst();
    while (cursor.isAfterLast() == false) {
      int totalColumn = cursor.getColumnCount();
      JSONObject rowObject = new JSONObject();
      for (int i = 0; i < totalColumn; i++) {
        if (cursor.getColumnName(i) != null) {
          try {
            rowObject.put(cursor.getColumnName(i),
              cursor.getString(i));
          } catch (Exception e) {
            Log.d(TAG, e.getMessage());
          }
        }
      }
      resultSet.put(rowObject);
      cursor.moveToNext();
    }
    cursor.close();
    return resultSet;
  }
}
