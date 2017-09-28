package ru.evendate.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.google.gson.Gson;

import ru.evendate.android.models.City;
import ru.evendate.android.models.User;

/**
 * Created by Aedirn on 16.10.16.
 */

public class EvendatePreferences {
    public static final String KEY_NOTIFICATION = "key_notification";
    public static final String KEY_INDICATOR = "key_indicator";
    public static final String KEY_VIBRATION = "key_vibration";
    public static final String KEY_INDICATOR_COLOR = "key_indicator_color";
    public static final String KEY_DEVICE_TOKEN_SYNCED = "key_device_token_synced";
    public static final boolean KEY_DEVICE_TOKEN_SYNCED_DEFAULT = false;
    public static final String KEY_DEVICE_TOKEN = "key_device_token";
    public static final boolean KEY_USER_CITY_SELECTED_DEFAULT = false;
    private static final boolean KEY_NOTIFICATION_DEFAULT = true;
    private static final boolean KEY_INDICATOR_DEFAULT = true;
    private static final boolean KEY_VIBRATION_DEFAULT = true;
    private static final int KEY_INDICATOR_COLOR_DEFAULT = 0xffff17a8;
    private static final String KEY_USER_CITY_SELECTED = "key_user_city_synced";
    private static final String KEY_USER_CITY = "key_user_city";
    private static final String DEFAULT_USER_CITY_JSON =
            "{'id':1,'en_name':'Moscow','country_id':1,'local_name':'Москва'}";
    private static final String KEY_USER = "key_user";

    Context mContext;

    private EvendatePreferences() {}

    public static EvendatePreferences newInstance(Context context) {
        EvendatePreferences preferences = new EvendatePreferences();
        preferences.mContext = context;
        return preferences;
    }

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

    public boolean getUserCitySelected() {
        return getPreferences(mContext).getBoolean(KEY_USER_CITY_SELECTED, KEY_USER_CITY_SELECTED_DEFAULT);
    }

    public void putUserCity(City userCity) {
        SharedPreferences.Editor editor = getPreferences(mContext).edit();
        Gson gson = new Gson();
        String json = gson.toJson(userCity);
        editor.putString(KEY_USER_CITY, json);
        editor.putBoolean(KEY_USER_CITY_SELECTED, true);
        editor.apply();
    }

    public City getUserCity() {
        Gson gson = new Gson();
        String json = getPreferences(mContext).getString(KEY_USER_CITY, DEFAULT_USER_CITY_JSON);
        return gson.fromJson(json, City.class);
    }

    public void putUser(User user) {
        SharedPreferences.Editor editor = getPreferences(mContext).edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    public User getUser() {
        Gson gson = new Gson();
        String json = getPreferences(mContext).getString(KEY_USER, null);
        return gson.fromJson(json, User.class);
    }
}
