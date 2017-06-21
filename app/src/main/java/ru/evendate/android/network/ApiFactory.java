package ru.evendate.android.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.evendate.android.ui.settings.SettingsActivity;

public class ApiFactory {

    private static final int CONNECT_TIMEOUT = 15;
    private static final int WRITE_TIMEOUT = 60;
    private static final int TIMEOUT = 60;
    private static final String HOST_NAME = "evendate.io";

    private static final OkHttpClient CLIENT;


    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …
        // add logging as last interceptor
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        httpClient.readTimeout(TIMEOUT, TimeUnit.SECONDS);
        httpClient.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        CLIENT = httpClient.build();
    }

    @NonNull
    public static ApiService getService(Context context) {
        return getRetrofit(context).create(ApiService.class);
    }

    @NonNull
    private static Retrofit getRetrofit(Context context) {
        return new Retrofit.Builder().baseUrl(getHostName(context))
                .addConverterFactory(GsonConverterFactory.create()
                ).addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(CLIENT).build();
    }

    public static String getHostName(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean https = sp.getBoolean(SettingsActivity.KEY_HTTPS, SettingsActivity.KEY_HTTPS_DEFAULT);
        if (https) {
            return "https://" + HOST_NAME;
        } else {
            return "http://" + HOST_NAME;
        }
    }
}
