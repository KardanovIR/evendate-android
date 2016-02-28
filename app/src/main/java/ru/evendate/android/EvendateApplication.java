package ru.evendate.android;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Dmitry on 23.11.2015.
 */
public class EvendateApplication extends MultiDexApplication {
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(R.xml.tracker_config);
        tracker.enableAdvertisingIdCollection(true);
        tracker.setAppVersion(BuildConfig.VERSION_NAME);
        if (BuildConfig.DEBUG)
            analytics.setDryRun(true);
    }

    public static Tracker getTracker() {
        return tracker;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}