package ru.evendate.android.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dmitry on 29.11.2015.
 */
public class RegistrationGCMIntentService extends IntentService {
    private static final String LOG_TAG = RegistrationGCMIntentService.class.getSimpleName();
    private static final String[] TOPICS = {"global"};

    public RegistrationGCMIntentService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(LOG_TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);
            subscribeTopics(token);
            Log.d(LOG_TAG, "GCM REGISTERED");
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token refresh", e);
        }
    }

    private void sendRegistrationToServer(String token) {
        ApiService apiService = ApiFactory.getService(getBaseContext());
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        Observable<Response> deviceObservable =
                apiService.putDeviceToken(EvendateAccountManager.peekToken(getBaseContext()),
                        token, "android", deviceModel, Build.VERSION.RELEASE);

        deviceObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isOk())
                        Log.i(LOG_TAG, "registered device token");
                    else
                        Log.e(LOG_TAG, "not registered device token");
                }, error -> {
                    Log.e(LOG_TAG, "not registered device token");
                    Log.e(LOG_TAG, error.getMessage());
                });
    }


    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
