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

/**
 * Created by Dmitry on 29.11.2015.
 */
public class EvendateGCMListenerService extends GcmListenerService {

    private static final String LOG_TAG = EvendateGCMListenerService.class.getSimpleName();

    private final String EVENT_ID = "event_id";
    private final String MESSAGE = "message";
    private final String ORGANIZATION_LOGO = "organization_logo";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    //{"data":{"message":"Testing long text:\nСияла призрачно луна,\nМерцаньем звёзд окружена.
    // \nОставив крепость ту, Аврора\nСкорее поспешила к морю.","event_id":1211,
    // "organization_logo":"http://evendate.ru/organizations_images/logos/small/1.png"},"to":"/topics/global"}
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(MESSAGE);
        int eventId = Integer.valueOf(data.getString(EVENT_ID));
        String orgLogoUrl = data.getString(ORGANIZATION_LOGO);
        Log.d(LOG_TAG, "From: " + from);
        Log.d(LOG_TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
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
        sendNotification(message, eventId, orgLogoUrl);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, int eventId, String logoUrl) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.setData(EvendateContract.EventEntry.getContentUri(eventId));
        intent.putExtra(EventDetailActivity.INTENT_TYPE, EventDetailActivity.NOTIFICATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        //load icon
        Bitmap logo;
        try {
            logo = Picasso.with(getBaseContext())
                    .load(logoUrl)
                    .error(R.drawable.default_background)
                    .get();
        } catch (IOException e) {
            logo = null;
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setLargeIcon(logo)
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
}