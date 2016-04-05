package ru.evendate.android.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.ui.EventDetailActivity;
import ru.evendate.android.ui.OrganizationDetailActivity;

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
    //{"data":{"message":"Testing long text:\nСияла призрачно луна,\nМерцаньем звёзд окружена.
    // \nОставив крепость ту, Аврора\nСкорее поспешила к морю.","event_id":1211,
    // "image_url":"http://evendate.ru/organizations_images/logos/small/1.png"},"to":"/topics/global"}
    @Override
    public void onMessageReceived(String from, Bundle data) {
        final String EVENT_ID = "event_id";
        final String ORGANIZATION_ID = "organization_id";
        final String USER_ID = "user_id";
        final String MESSAGE = "message";
        final String IMAGE_URL = "image_url";

        final String EVENT_CHANNEL = "events";
        final String ORGANIZATION_CHANNEL = "organizations";
        final String USER_CHANNEL = "users";
        final String DEBUG_CHANNEL = "debug";

        String message = data.getString(MESSAGE);
        String imageUrl = data.getString(IMAGE_URL);
        Log.d(LOG_TAG, "From: " + from);
        Log.d(LOG_TAG, "Message: " + message);
        Intent intent;
        if (from.startsWith("/topics/")) {
            switch (from.substring(from.lastIndexOf("/"), from.length())) {
                //todo optimize
                case EVENT_CHANNEL:
                    intent = new Intent(this, EventDetailActivity.class);
                    int eventId = Integer.valueOf(data.getString(EVENT_ID));
                    intent.setData(EvendateContract.EventEntry.getContentUri(eventId));
                    intent.putExtra(EventDetailActivity.INTENT_TYPE, EventDetailActivity.NOTIFICATION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    break;
                case ORGANIZATION_CHANNEL:
                    intent = new Intent(this, OrganizationDetailActivity.class);
                    int orgId = Integer.valueOf(data.getString(ORGANIZATION_ID));
                    intent.setData(EvendateContract.OrganizationEntry.getContentUri(orgId));
                    intent.putExtra(OrganizationDetailActivity.INTENT_TYPE, OrganizationDetailActivity.NOTIFICATION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    break;
                case USER_CHANNEL:
                    intent = new Intent(this, EventDetailActivity.class);
                    int userId = Integer.valueOf(data.getString(USER_ID));
                    intent.setData(EvendateContract.UserEntry.getContentUri(userId));
                    intent.putExtra(EventDetailActivity.INTENT_TYPE, EventDetailActivity.NOTIFICATION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    break;
                case DEBUG_CHANNEL:
                    //if (!BuildConfig.DEBUG)
                    return;
                default:
                    return;
            }
        } else {
            // normal downstream message.
            return;
        }
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message, intent, imageUrl);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, Intent intent, String imageUrl) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setLargeIcon(loadIcon(imageUrl))
                .setContentTitle("Evendate")
                .setContentText(message)
                //just expand message to multi row
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private Bitmap loadIcon(String imageUrl) {
        Bitmap icon;
        try {
            icon = Picasso.with(getBaseContext())
                    .load(imageUrl)
                    .error(R.drawable.default_background)
                    .get();
        } catch (IOException e) {
            icon = null;
        }
        return icon;
    }
}