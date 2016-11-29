package ru.evendate.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

/**
 * Created by Aedirn on 16.10.16.
 */

public class EvendatePreferences {
    public static final String KEY_NOTIFICATION = "key_notification";
    private static final boolean KEY_NOTIFICATION_DEFAULT = true;

    public static final String KEY_INDICATOR = "key_indicator";
    private static final boolean KEY_INDICATOR_DEFAULT = true;

    public static final String KEY_VIBRATION = "key_vibration";
    private static final boolean KEY_VIBRATION_DEFAULT = true;

    public static final String KEY_INDICATOR_COLOR = "key_indicator_color";
    private static final int KEY_INDICATOR_COLOR_DEFAULT = 0xffff17a8;

    public static final String KEY_DEVICE_TOKEN_SYNCED = "key_device_token_synced";
    public static final boolean KEY_DEVICE_TOKEN_SYNCED_DEFAULT = false;

    public static final String KEY_DEVICE_TOKEN = "key_device_token";

    public static boolean isVibrateOn(Context context) {
        return getPreferences(context).getBoolean(KEY_VIBRATION, KEY_VIBRATION_DEFAULT);
    }

    public static boolean isNotificationOn(Context context) {
        return getPreferences(context).getBoolean(KEY_NOTIFICATION, KEY_NOTIFICATION_DEFAULT);
    }

    public static boolean isLedOn(Context context) {
        return getPreferences(context).getBoolean(KEY_INDICATOR, KEY_INDICATOR_DEFAULT);
    }

    public static int getLedColor(Context context) {
        return getPreferences(context).getInt(KEY_INDICATOR_COLOR, KEY_INDICATOR_COLOR_DEFAULT);
    }

    public static void setLedColor(Context context, int color) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_INDICATOR_COLOR, color);
        editor.apply();
    }

    public static boolean getDeviceTokenSynced(Context context) {
        return getPreferences(context).getBoolean(KEY_DEVICE_TOKEN_SYNCED, KEY_DEVICE_TOKEN_SYNCED_DEFAULT);
    }

    public static void setDeviceTokenSynced(Context context, boolean synced) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(KEY_DEVICE_TOKEN_SYNCED, synced);
        editor.apply();
    }

    public static String getDeviceToken(Context context) {
        return getPreferences(context).getString(KEY_DEVICE_TOKEN, "");
    }

    public static void setDeviceToken(Context context, String token) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_DEVICE_TOKEN, token);
        editor.apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
