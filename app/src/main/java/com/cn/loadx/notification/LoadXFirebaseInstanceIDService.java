package com.cn.loadx.notification;

import android.content.Intent;
import android.util.Log;

import com.cn.loadx.util.SharedPrefsUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN;
import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN_REGISTERED;


public class LoadXFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = LoadXFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,refreshedToken);
        // Saving reg id to shared preferences

        SharedPrefsUtils.setStringPreference(LoadXFirebaseInstanceIDService.this, KEY_FCM_TOKEN, null);
        boolean isTokenRegistered =  SharedPrefsUtils.getBooleanPreference(LoadXFirebaseInstanceIDService.this,KEY_FCM_TOKEN_REGISTERED,false);
        if(!isTokenRegistered)
        {
            //send token to server
        }
        // sending reg id to your server
       // sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        /*Intent registrationComplete = new Intent(FCMConfig.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);*/

        try {
            Intent registrationComplete = new Intent();
            registrationComplete.putExtra("token", refreshedToken);
            registrationComplete.setAction(FCMConfig.REGISTRATION_COMPLETE);
            sendBroadcast(registrationComplete);
        }catch (Exception ex){
            Log.e(TAG,"FCM Exception:"+ex.getLocalizedMessage());
        }

    }


}