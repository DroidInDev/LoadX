package com.cn.loadx.util;

/**
 * Created by Admin on 23-06-2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * A pack of helpful getter and setter methods for reading/writing to {@link SharedPreferences}.
 */
final public class SharedPrefsUtils {
    private SharedPrefsUtils() {}
    // The SharedPreferences object in which geofences are stored
    private static SharedPreferences mPrefs = null;

    // The name of the resulting SharedPreferences
    private static final String SHARED_PREFERENCE_NAME =
            LoadXApplication.getInstance().getPackageName();

    // Create the SharedPreferences storage with private access only
    public static SharedPreferences getSharedPrefs(Context context) {
         mPrefs =
                context.getSharedPreferences(
                        SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
        return mPrefs;
    }
    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public static String getStringPreference(Context context, String key) {
        return getSharedPrefs(context).getString(key,AppConstants.EMPTY_STRING);
    }

    /**
     * Helper method to write a String value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static void setStringPreference(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Helper method to retrieve a float value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static float getFloatPreference(Context context, String key, float defaultValue) {
        return getSharedPrefs(context).getFloat(key, defaultValue);
    }

    /**
     * Helper method to write a float value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static void setFloatPreference(Context context, String key, float value) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    /**
     * Helper method to retrieve a long value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static long getLongPreference(Context context, String key, long defaultValue) {
        return getSharedPrefs(context).getLong(key, defaultValue);
    }

    /**
     * Helper method to write a long value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static void  setLongPreference(Context context, String key, long value) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Helper method to retrieve an integer value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static int getIntegerPreference(Context context, String key, int defaultValue) {
        return getSharedPrefs(context).getInt(key, defaultValue);
    }

    /**
     * Helper method to write an integer value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static void setIntegerPreference(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Helper method to retrieve a boolean value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
        return getSharedPrefs(context).getBoolean(key, defaultValue);
    }

    /**
     * Helper method to write a boolean value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static void setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public static void clearPreferences(Context context){
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.clear();
        editor.apply();
    }
}
