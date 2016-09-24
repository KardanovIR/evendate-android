package ru.evendate.android.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import ru.evendate.android.R;
import ru.evendate.android.Statistics;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.ui.EventDetailActivity;
import ru.evendate.android.ui.MainActivity;
import ru.evendate.android.ui.OrganizationDetailActivity;
import ru.evendate.android.ui.SettingsActivity;
import ru.evendate.android.ui.UserProfileActivity;

/**
 * Created by Dmitry on 29.11.2015.
 */
public class EvendateGCMListenerService extends GcmListenerService {
    private static final String LOG_TAG = EvendateGCMListenerService.class.getSimpleName();

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    //{"data":{"message":"Testing long text:\nСияла призрачно луна,\nМерцаньем звёзд окружена.\nОставив крепость ту, Аврора\nСкорее поспешила к морю.","event_id":1211,"image_url":"http://evendate.ru/organizations_images/logos/small/1.png","organization_id":null,"type":"events"},"to":"/topics/global"}
    @Override
    public void onMessageReceived(String from, Bundle data) {
        final String EVENT_ID = "event_id";
        final String ORGANIZATION_ID = "organization_id";
        final String USER_ID = "user_id";
        final String MESSAGE = "message";
        final String IMAGE_URL = "image_url";
        final String NOTIFICATION_TYPE = "type";

        final String EVENT_TYPE = "events";
        final String ORGANIZATION_TYPE = "organizations";
        final String USER_TYPE = "users";
        final String RECOMMENDATION_TYPE = "recommendations_organizations";
        final String DEBUG_TYPE = "debug";

        String message = data.getString(MESSAGE);
        String imageUrl = data.getString(IMAGE_URL);
        Log.d(LOG_TAG, "From: " + from);
        Log.d(LOG_TAG, "Message: " + message);
        Intent intent;
        if (from.startsWith("/topics/")) {
            return;
        } else {
            String type = data.getString(NOTIFICATION_TYPE);
            if (type == null) {
                Log.e(LOG_TAG, "null type");
                return;
            }
            switch (type) {
                //todo optimize
                case EVENT_TYPE:
                    intent = new Intent(this, EventDetailActivity.class);
                    int eventId = Integer.valueOf(data.getString(EVENT_ID));
                    intent.setData(EvendateContract.EventEntry.getContentUri(eventId));
                    break;
                case ORGANIZATION_TYPE:
                    intent = new Intent(this, OrganizationDetailActivity.class);
                    int orgId = Integer.valueOf(data.getString(ORGANIZATION_ID));
                    intent.setData(EvendateContract.OrganizationEntry.getContentUri(orgId));
                    break;
                case USER_TYPE:
                    intent = new Intent(this, UserProfileActivity.class);
                    int userId = Integer.valueOf(data.getString(USER_ID));
                    intent.setData(EvendateContract.UserEntry.getContentUri(userId));
                    break;
                case RECOMMENDATION_TYPE:
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.SHOW_ONBOARDING, true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    sendNotification(message, intent, imageUrl);
                    return;
                case DEBUG_TYPE:
                    //if (!BuildConfig.DEBUG)
                    return;
                default:
                    return;
            }
        }
        intent.putExtra(Statistics.INTENT_TYPE, Statistics.NOTIFICATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendNotification(message, intent, imageUrl);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, Intent intent, String imageUrl) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setLargeIcon(loadIcon(imageUrl))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                //.setGroupSummary(false)
                //just expand message to multi row
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        int accentColor = ContextCompat.getColor(getBaseContext(), R.color.accent);

        notificationBuilder.setColor(accentColor);
        if (isVibrateOn()) {
            notificationBuilder.setVibrate(new long[]{200, 500, 200, 500});
        }
        if (isLedOn()) {
            notificationBuilder.setLights(getLedColor(), 1000, 200);
        }
        if (!isNotificationOn()) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = notificationBuilder.build();
        notificationManager.notify((int)System.currentTimeMillis(), notification);
    }

    private Bitmap loadIcon(String imageUrl) {
        Bitmap icon;
        try {
            icon = Picasso.with(getBaseContext())
                    .load(imageUrl)
                    .get();
        } catch (IOException | IllegalStateException e) {
            icon = null;
        }
        return icon;
    }

    private boolean isVibrateOn() {
        return getPreferences().getBoolean(SettingsActivity.KEY_VIBRATION, SettingsActivity.KEY_VIBRATION_DEFAULT);
    }

    private boolean isNotificationOn() {
        return getPreferences().getBoolean(SettingsActivity.KEY_NOTIFICATION, SettingsActivity.KEY_NOTIFICATION_DEFAULT);
    }

    private boolean isLedOn() {
        return getPreferences().getBoolean(SettingsActivity.KEY_INDICATOR, SettingsActivity.KEY_INDICATOR_DEFAULT);
    }

    private int getLedColor() {
        return getPreferences().getInt(SettingsActivity.KEY_INDICATOR_COLOR, SettingsActivity.KEY_INDICATOR_COLOR_DEFAULT);
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }
}