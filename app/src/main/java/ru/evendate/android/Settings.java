package ru.evendate.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import ru.evendate.android.ui.SettingsActivity;

/**
 * Created by Aedirn on 16.10.16.
 */

public class Settings {

    public static boolean isVibrateOn(Context context) {
        return getPreferences(context).getBoolean(SettingsActivity.KEY_VIBRATION, SettingsActivity.KEY_VIBRATION_DEFAULT);
    }

    public static boolean isNotificationOn(Context context) {
        return getPreferences(context).getBoolean(SettingsActivity.KEY_NOTIFICATION, SettingsActivity.KEY_NOTIFICATION_DEFAULT);
    }

    public static boolean isLedOn(Context context) {
        return getPreferences(context).getBoolean(SettingsActivity.KEY_INDICATOR, SettingsActivity.KEY_INDICATOR_DEFAULT);
    }

    public static int getLedColor(Context context) {
        return getPreferences(context).getInt(SettingsActivity.KEY_INDICATOR_COLOR, SettingsActivity.KEY_INDICATOR_COLOR_DEFAULT);
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
