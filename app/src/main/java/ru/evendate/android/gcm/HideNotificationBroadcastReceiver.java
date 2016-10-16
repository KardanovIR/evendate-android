package ru.evendate.android.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.evendate.android.statistics.Statistics;

import static ru.evendate.android.gcm.NotificationAdditionalData.EVENT_TYPE;
import static ru.evendate.android.gcm.NotificationAdditionalData.ORGANIZATION_TYPE;
import static ru.evendate.android.gcm.NotificationAdditionalData.USER_TYPE;

/**
 * process hiding notification by user explicitly
 */

public class HideNotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String EVENT_ID = "event_id";
    public static final String ORG_ID = "organization_id";
    public static final String USER_ID = "user_id";
    public static final String ENTRY_TYPE = "entry_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        int event_id = intent.getIntExtra(EVENT_ID, -1);
        int org_id = intent.getIntExtra(ORG_ID, -1);
        int user_id = intent.getIntExtra(USER_ID, -1);
        String entry_type = intent.getStringExtra(ENTRY_TYPE);
        if (entry_type == null) {
            Log.e("HideNotifyBroadcast", "no entry type");
            return;
        }

        Statistics statistics = new Statistics(context);
        switch (entry_type) {
            case EVENT_TYPE:
                if(event_id != -1)
                    statistics.sendEventHideNotification(event_id);
                break;
            case ORGANIZATION_TYPE:
                if(org_id != -1)
                    statistics.sendOrgHideNotification(org_id);
                break;
            case USER_TYPE:
                if(user_id != -1)
                    statistics.sendUserHideNotification(user_id);
                break;
        }
    }
}