package org.apache.cordova.sayang;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;

import java.util.List;

import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

public class service extends Service {
  private NotificationManager notifManager;
  Context context = this;
  PusherOptions options;
  Pusher pusher;
  Channel channel;
  Class mainActivity;
  Resources resources;
  String packageName;

  @Override
  public IBinder onBind(Intent intent) {

    return null;
  }

  @Override
  public void onCreate() {
    resources = context.getResources();
    if (Build.VERSION.SDK_INT >= 26) {
//      String CHANNEL_ID = "my_channel_01";
//      NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
//        "Channel human readable title",
//        NotificationManager.IMPORTANCE_DEFAULT);
//      channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
//      Intent intent;
//      PendingIntent pendingIntent;
//      ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
//      intent = new Intent(context, Receiver.class);
//      intent.setAction("org.apache.cordova.sayang.BroadcastReceiver");
//      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//      intent.putExtra("pertamax", "pertamax");
//      Log.d("isiIntent", intent.hasExtra("pushnotification") + "");
//      //pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//      pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//      Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//        .setContentTitle("")
//        .setContentText("")
//        .setPriority(PRIORITY_MIN)
//        .setSmallIcon(resources.getIdentifier("ic_launcher", "mipmap", packageName))
//        .setAutoCancel(true)
//        .setContentIntent(pendingIntent)
//        .setCategory(Notification.CATEGORY_SERVICE).build();
//
//      startForeground(1, notification);
    }
    packageName = context.getPackageName();
    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    String className = launchIntent.getComponent().getClassName();

    try {
      mainActivity = Class.forName(className);
    } catch (Exception e) {
      e.printStackTrace();
    }
    DatabaseHelper dbPusher = new DatabaseHelper(context);
    Cursor p = dbPusher.getAllDataPusher();
    Helper h = new Helper();

    if (p.getCount() > 0) {
      p.moveToFirst();
      try {
        String cluster = h.strToJSON(p.getString(1), "cluster");
        String apikey = h.strToJSON(p.getString(1), "apikey");
        String event = h.strToJSON(p.getString(1), "event");
        String channelNm = h.strToJSON(p.getString(1), "channelNm");
        Log.d("masuk", cluster + "/" + apikey + "/" + event + "/" + channelNm);
        //pusher("ap1","e327dc39f0ae164632ea","my-channel","test-event");
        pusher(cluster, apikey, channelNm, event);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }


  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  private boolean isAppIsInBackground(Context context) {
    boolean isInBackground = true;
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
      List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
      for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
          for (String activeProcess : processInfo.pkgList) {
            if (activeProcess.equals(context.getPackageName())) {
              isInBackground = false;
            }
          }
        }
      }
    } else {
      List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
      ComponentName componentInfo = taskInfo.get(0).topActivity;
      if (componentInfo.getPackageName().equals(context.getPackageName())) {
        isInBackground = false;
      }
    }

    return isInBackground;
  }

  private void pusher(String cluster, String apikey, String channelNm, String event) {
    options = new PusherOptions();
    options.setCluster(cluster);
    pusher = new Pusher(apikey, options);
    pusher.connect();
    channel = pusher.subscribe(channelNm);
    Log.d("masuk", "masuk1");
    channel.bind(event, new SubscriptionEventListener() {
      @Override
      public void onEvent(PusherEvent event) {
        DatabaseHelper myDb;
        Helper h = new Helper();
        myDb = new DatabaseHelper(context);
        Cursor c = myDb.getAllData();
        if (c.getCount() > 0) {
          c.moveToFirst();
          try {
            String typeNotif = h.strToJSON(event.getData(), "type");
            Log.d("masuk", "masuk2");
            String dataLocal = h.strToJSON(c.getString(1), "uuid");
            String userLocal = h.strToJSON(c.getString(1), "id");
            if (typeNotif.equals("cekUser")) {
              String dataNotif = h.strToJSON(event.getData(), "uuid");
              String userNotif = h.strToJSON(event.getData(), "id");
              if (!dataLocal.equals(dataNotif) && userLocal.equals(userNotif)) {
                myDb.truncateData();
                createNotification("Akun yg saat ini aktif telah digunakan di device lain", context, event.getData(), "akun");
              }
            } else if (typeNotif.equals("notifModul")) {
              String toNotif = h.strToJSON(event.getData(), "to");
              String msgNotif = h.strToJSON(event.getData(), "message");
              if (userLocal.equals(toNotif)) {
                //if (isAppIsInBackground(context))
                createNotification(msgNotif, context, event.getData(), "new Approval");
              }
            }

          } catch (JSONException e) {
            e.printStackTrace();
          }
          //createNotification(event.getData(), context);
        }

      }
    });


  }

  public void createNotification(String aMessage, Context context, String json, String type) {
    final int NOTIFY_ID = 0; // ID of notification
    String id = "zemmuwa"; // default_channel_id
    String title = "test"; // Default Channel
    Intent intent;
    PendingIntent pendingIntent;
    NotificationCompat.Builder builder;

    if (notifManager == null) {
      notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel mChannel = notifManager.getNotificationChannel(id);
      if (mChannel == null) {
        mChannel = new NotificationChannel(id, title, importance);
//        mChannel.enableVibration(true);
//        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notifManager.createNotificationChannel(mChannel);
      }
      builder = new NotificationCompat.Builder(context, id);
      //intent = new Intent(context, MainActivity.class);
      intent = new Intent(context, Receiver.class);
      intent.setAction("org.apache.cordova.sayang.BroadcastReceiver");
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("pushnotification", json);
      Log.d("isiIntent", intent.hasExtra("pushnotification") + "");
      //pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder
        .setContentTitle(aMessage)                            // required
        .setSmallIcon(resources.getIdentifier("ic_launcher", "mipmap", packageName))
        .setContentText(context.getString(resources.getIdentifier("app_name", "string", packageName)))
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(aMessage)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
        .setNumber(1);
    } else {
      builder = new NotificationCompat.Builder(context, id);
      intent = new Intent(context, Receiver.class);
      intent.setAction("org.apache.cordova.sayang.BroadcastReceiver");
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("pushnotification", json);
      pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.setContentTitle(aMessage)                            // required
        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
        .setContentText(context.getString(resources.getIdentifier("app_name", "string", packageName))) // required
        //.setContentText(context.getString(R.string.app_name))
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(aMessage)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
        .setPriority(Notification.PRIORITY_HIGH);
    }
    Notification notification = builder.build();
    notifManager.notify((int) System.currentTimeMillis()
      , notification);
  }


}
