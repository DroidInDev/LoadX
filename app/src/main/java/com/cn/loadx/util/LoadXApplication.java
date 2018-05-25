package com.cn.loadx.util;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Admin on 27-01-2018.
 */

public class LoadXApplication extends Application{

    private static LoadXApplication mInstance;
    public static Context appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        mInstance = this;
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)           // Enables Crashlytics debugger
                .build();
        Fabric.with(fabric);
    }

    public static synchronized LoadXApplication getInstance() {
        return mInstance;
    }
    public static LoadXApplication getAppContext() {
        return get(appContext);
    }

    public static LoadXApplication get(Context context) {
        return (LoadXApplication) context.getApplicationContext();
    }
    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
