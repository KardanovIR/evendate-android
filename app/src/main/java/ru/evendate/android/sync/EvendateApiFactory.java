package ru.evendate.android.sync;

import android.support.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Dmitry on 18.10.2015.
 */
public class EvendateApiFactory {

    private static final int CONNECT_TIMEOUT = 15;
    private static final int WRITE_TIMEOUT = 60;
    private static final int TIMEOUT = 60;
    public static final String HOST_NAME = "http://test.evendate.ru";

    private static final OkHttpClient CLIENT = new OkHttpClient();

    static {
        CLIENT.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        CLIENT.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        CLIENT.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
    }

    @NonNull
    public static EvendateService getEvendateService() {
        return getRetrofit().create(EvendateService.class);
    }

    @NonNull
    private static Retrofit getRetrofit(){
        return new Retrofit.Builder().baseUrl(HOST_NAME)
                .addConverterFactory(GsonConverterFactory.create()
                ).client(CLIENT).build();
    }
}
