package ru.evendate.android.gcm;


import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;

import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;


/**
 * Created by Aedirn on 15.10.16.
 */
public class EvendateNotificationExtenderService extends NotificationExtenderService {
    private final String LOG_TAG = EvendateNotificationOpenedHandler.class.getSimpleName();

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        Log.d(LOG_TAG, "received notification");
        NotificationAdditionalData addData = NotificationAdditionalData.obtainFromPayload(receivedResult.payload);
        if (!EvendatePreferences.isNotificationOn(getBaseContext()))
            //todo handle stat?
            return true;
        OverrideSettings overrideSettings = new OverrideSettings();

        overrideSettings.extender = (NotificationCompat.Builder builder) -> {
            if (addData != null) {
                Log.d(LOG_TAG, receivedResult.payload.additionalData.toString());
                builder.setDeleteIntent(getHideIntent(addData));
            }
            builder.setSmallIcon(R.drawable.ic_stat_ic_notification);

            int accentColor = ContextCompat.getColor(getBaseContext(), R.color.primary);

            builder.setColor(accentColor);
            if (EvendatePreferences.isVibrateOn(getBaseContext())) {
                builder.setVibrate(new long[]{200, 500, 200, 500});
            }
            if (EvendatePreferences.isLedOn(getBaseContext())) {
                builder.setLights(EvendatePreferences.getLedColor(getBaseContext()), 1000, 200);
            }
            return builder;
        };

        OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
        Log.d("NotifyExtenderService", "Notification displayed with id: " + displayedResult.androidNotificationId);

        return true;
    }

    private PendingIntent getHideIntent(NotificationAdditionalData addData) {
        Intent intent = new Intent(getApplicationContext(), HideNotificationBroadcastReceiver.class);
        intent.putExtra(HideNotificationBroadcastReceiver.ENTRY_TYPE, addData.type);
        intent.putExtra(HideNotificationBroadcastReceiver.EVENT_ID, addData.eventId);
        intent.putExtra(HideNotificationBroadcastReceiver.ORG_ID, addData.orgId);
        intent.putExtra(HideNotificationBroadcastReceiver.USER_ID, addData.userId);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
    }
}