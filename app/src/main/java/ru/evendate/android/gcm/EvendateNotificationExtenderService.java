package ru.evendate.android.gcm;


import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OSNotificationReceivedResult;

import ru.evendate.android.R;
import ru.evendate.android.Settings;

import static ru.evendate.android.gcm.EvendateNotificationOpenedHandler.FAVE_EVENT_ACTION_ID;


/**
 * Created by Aedirn on 15.10.16.
 */
public class EvendateNotificationExtenderService extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        NotificationAdditionalData addData = NotificationAdditionalData.obtainFromPayload(receivedResult.payload);
        if(!Settings.isNotificationOn(getBaseContext()))
            //todo handle stat?
            return true;
        if (addData == null)
            return false;
        OverrideSettings overrideSettings = new OverrideSettings();

        overrideSettings.extender = (NotificationCompat.Builder builder) -> {
            builder.setDeleteIntent(getHideIntent(addData));
            int accentColor = ContextCompat.getColor(getBaseContext(), R.color.accent);

            builder.setColor(accentColor);
            if (Settings.isVibrateOn(getBaseContext())) {
                builder.setVibrate(new long[]{200, 500, 200, 500});
            }
            if (Settings.isLedOn(getBaseContext())) {
                builder.setLights(Settings.getLedColor(getBaseContext()), 1000, 200);
            }
            if (addData.type.equals(NotificationAdditionalData.EVENT_TYPE)) {
                addFaveButton(receivedResult.payload);
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

    private void addFaveButton(OSNotificationPayload payload) {
        OSNotificationPayload.ActionButton action = new OSNotificationPayload.ActionButton();
        action.id = FAVE_EVENT_ACTION_ID;
        //todo
        action.icon = "ic_star";
        action.text = "fave";
        payload.actionButtons.add(action);
    }

}