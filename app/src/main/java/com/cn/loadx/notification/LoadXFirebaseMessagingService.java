package com.cn.loadx.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.cn.loadx.R;
import com.cn.loadx.activity.HomeActivity;
import com.cn.loadx.activity.SplashActivity;
import com.cn.loadx.activity.UserProfileActivity;
import com.cn.loadx.util.ApplicationUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class LoadXFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = LoadXFirebaseMessagingService.class.getSimpleName();
    private static final String CHANNEL_ID ="channel_01" ;

    private NotificationUtils notificationUtils;
    private String tittle;
    private String message;
    private JSONObject payload;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Log.d(TAG, "From: " + from);

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, remoteMessage.getNotification().getBody()+" "+remoteMessage.getNotification().getTitle());
            Log.d(TAG, remoteMessage.getNotification().toString());
            if(remoteMessage.getNotification().getBody()!=null&&remoteMessage.getNotification().getTitle()!=null)
            {
                String pagetittle = remoteMessage.getNotification().getTitle();
                String action = remoteMessage.getNotification().getBody();
                if(pagetittle.equals("home")) {
                    if (action.equals("reload")){
                        broadcastPushNotification(pagetittle, action);
                    return;
                }
                }
            }
            try {
                JSONObject notificationObject = new JSONObject(remoteMessage.getData());
                 message=notificationObject.getString("body");
                 tittle = notificationObject.getString("title");
            } catch (JSONException e) {
                Log.d(TAG," Not "+e.getMessage());
                e.printStackTrace();
            }
        }
        if (remoteMessage == null)
            return;

        // Check if message contains a data payload.
        //if (remoteMessage.getData().size() > 0) {
        Log.d(TAG,"size "+remoteMessage.getData().size());
            Map<String, String> mapNotify = remoteMessage.getData();

            String payload_extra = mapNotify.get("extra");
            // String tittle =mapNotify.get("title");
            String payloadStr=remoteMessage.getData().toString();
            if (payload_extra != null) {
                String page = null;
                try {
                    payload = new JSONObject(payload_extra);
                    if(ApplicationUtil.contains(payload,"page"))
                        page = payload.getString("page");
                    if(ApplicationUtil.contains(payload,"body"))
                        message = payload.getString("body");
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "Data Payload: " + payload_extra);
            Log.d(TAG, "Message: " + message);
            if(payload_extra!=null)
            handleDataMessage(tittle, message, payload_extra);

      //  }
    }


    private void broadcastPushNotification(String page, String message) {
        try {
            Intent pushNotification = new Intent();
            pushNotification.putExtra("page",page );
            pushNotification.putExtra("message", message);
            pushNotification.setAction(FCMConfig.PUSH_NOTIFICATION);
            sendBroadcast(pushNotification);
        } catch (Exception ex) {
            Log.e(TAG, "Exception:" + ex.getLocalizedMessage());
        }
    }

    private void handleDataMessage(String title, String message, String payload_extra) {
        // Log.e(TAG, "push json: " + jsonExtra.toString());
        payload = null;
        String page = null;
        try {
            payload = new JSONObject(payload_extra);

        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }

        try {
            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                // Get an instance of the Notification manager
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                String type = null;
                String notifyMessage = null;
                String notifyPage = null;
                if (payload != null) {
                    if(ApplicationUtil.contains(payload,"type"))
                    type = payload.getString("type");
                    if(ApplicationUtil.contains(payload,"body"))
                    notifyMessage = payload.getString("body");
                    if(ApplicationUtil.contains(payload,"page"))
                    notifyPage = payload.getString("page");
                    Log.d(TAG, " message FGND " + message);
                    Log.d(TAG, " type FGND " + type);

                }
                // Create an explicit content Intent that starts the main Activity.
                Intent notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
                if(type!=null&&notifyPage!=null) {
                    notificationIntent.putExtra("type", type);
                    notificationIntent.putExtra("page", notifyPage);
                }
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // Construct a task stack.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

                // Add the main Activity to the task stack as the parent.
                stackBuilder.addParentStack(SplashActivity.class);

                // Push the content Intent onto the stack.
                stackBuilder.addNextIntent(notificationIntent);

                // Get a PendingIntent containing the entire back stack.
                PendingIntent notificationPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

                // Get a notification builder that's compatible with platform versions >= 4
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

                // Set Vibrate, Sound and Light
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_LIGHTS;
                //	defaults = defaults | Notification.DEFAULT_VIBRATE;
                defaults = defaults | Notification.DEFAULT_SOUND;
                // Define the notification settings.
                builder.setSmallIcon(R.drawable.loadx_appicon)
                        // In a real app, you may want to use a library like Volley
                        // to decode the Bitmap.
                       /* .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.drawable.loadx_appicon))*/
                        .setColor(Color.RED)
                        .setDefaults(defaults)
                        .setShowWhen(false)
                        .setContentTitle("LoadX")
                        .setContentText(notifyMessage)
                        .setContentIntent(notificationPendingIntent);

                // Set the Channel ID for Android O.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setChannelId(CHANNEL_ID); // Channel ID
                }

                // Dismiss notification once the user touches it.
                builder.setAutoCancel(true);

                // Issue the notification
                mNotificationManager.notify(0, builder.build());
               /* Intent resultIntent = new Intent(getApplicationContext(), SplashActivity.class);
                resultIntent.putExtra("type", type);
                resultIntent.putExtra("page", notifyPage);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this.getApplicationContext(),
                        0,
                        resultIntent, PendingIntent.FLAG_ONE_SHOT);


                sendNotification(getApplicationContext(), "LoadX", motifyMessage, pendingIntent);*/
            } else {
                // app is in background, show the notification in notification tray
                //Intent resultIntent = getResultPendingIntent(page,message,room_id,message_id,post_id);
                // Intent resultIntent = getResultPendingIntent(page,message,room_id,message_id,post_id);
                Intent resultIntent = null;
                PendingIntent pendingIntent;
                Log.d(TAG, "Message: " + message);
               /* if(page.equals("profile")) {
                     resultIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                     resultIntent.putExtra("UPDATE","PROFILE");
                    resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(
                            this.getApplicationContext(),
                            0,
                            resultIntent, PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_ONE_SHOT);
                    Log.d(TAG, "Message: " + "profile intent");
                }else{*/
                resultIntent = new Intent(getApplicationContext(), SplashActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(
                        this.getApplicationContext(),
                        0,
                        resultIntent, PendingIntent.FLAG_ONE_SHOT);
                //  }


                //showSmallNotification(getApplicationContext(), "TaDa Life", message, pendingIntent);
                sendNotification(getApplicationContext(), title, message, pendingIntent);
                // }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }


    private void sendNotification(Context mContext, String title, String message, PendingIntent resultPendingIntent) {
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.loadx_appicon)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        // Set pending intent
        notificationBuilder.setContentIntent(resultPendingIntent);

        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        //	defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        notificationBuilder.setDefaults(defaults);
        // Set the content for Notification
        notificationBuilder.setContentText(message);
        // Set autocancel
        notificationBuilder.setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
  /*  private void showSmallNotification(Context mContext, String title, String message, PendingIntent resultPendingIntent) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext);
       *//* NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.addLine(message);*//*

        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.app_icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                //.setSound(alarmSound)
               // .setStyle(inboxStyle)
                //.setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(getNotificationIcon())
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(FCMConfig.NOTIFICATION_ID, notification);
    }*/

}