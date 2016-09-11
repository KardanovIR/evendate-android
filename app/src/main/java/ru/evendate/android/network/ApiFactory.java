package ru.evendate.android.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import ru.evendate.android.ui.SettingsActivity;

/**
 * Created by Dmitry on 18.10.2015.
 */
public class ApiFactory {

    private static final int CONNECT_TIMEOUT = 15;
    private static final int WRITE_TIMEOUT = 60;
    private static final int TIMEOUT = 60;
    private static final String HOST_NAME = "test.evendate.ru";

    private static final OkHttpClient CLIENT = new OkHttpClient();


    static {
        CLIENT.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        CLIENT.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        CLIENT.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
    }

    @NonNull
    public static ApiService getService(Context context) {
        return getRetrofit(context).create(ApiService.class);
    }

    @NonNull
    private static Retrofit getRetrofit(Context context) {
        return new Retrofit.Builder().baseUrl(getHostName(context))
                .addConverterFactory(GsonConverterFactory.create()
                ).addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(CLIENT).build();
    }

    public static String getHostName(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean https = sp.getBoolean(SettingsActivity.KEY_HTTPS, SettingsActivity.KEY_HTTPS_DEFAULT);
        if(https) {
            return "https://" + HOST_NAME;
        }
        else {
            return "http://" + HOST_NAME;
        }
    }
}
