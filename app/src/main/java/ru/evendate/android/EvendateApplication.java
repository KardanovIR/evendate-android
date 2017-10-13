package ru.evendate.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.ImageView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import ru.evendate.android.gcm.EvendateNotificationOpenedHandler;
import ru.evendate.android.network.ServiceImpl;

/**
 * Created by Dmitry on 23.11.2015.
 */
public class EvendateApplication extends MultiDexApplication {

    private static GoogleAnalytics analytics;
    private static Tracker tracker;

    private VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                EvendateAccountManager.invalidateToken(getApplicationContext());
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(R.xml.tracker_config);
        tracker.enableAdvertisingIdCollection(true);
        tracker.setAppVersion(BuildConfig.VERSION_NAME);

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);
        //AppEventsLogger.activateApp(this);

        initImageLoader();
        initOneSignal();

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

    //initialize and create the image loader logic
    private void initImageLoader() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });
    }

    private void initOneSignal() {
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new EvendateNotificationOpenedHandler(getApplicationContext()))
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();
        OneSignal.enableVibrate(true);
        OneSignal.enableSound(true);
        OneSignal.idsAvailable((String userId, String registrationId) -> {
            EvendatePreferences.setDeviceToken(this, userId);
            if (registrationId != null && EvendatePreferences.isDeviceTokenSynced(this))
                ServiceImpl.sendRegistrationToServer(getApplicationContext(), userId);

        });
    }
}