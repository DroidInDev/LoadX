package com.cn.loadx.notification;

/**
 * Created by Admin on 06-02-2018.
 */

public class FCMConfig {
    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "com.cn.loadx.FCMregistrationComplete";
    public static final String PUSH_NOTIFICATION = "com.cn.loadx.pushNotification";
    public static final String PUSH_NOTIFICATION_COUNT = "com.cn.loadx.pushNotificationCount";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_FCM_TOKEN_REGISTERED = "fcm_token_sent_to_server";
}
