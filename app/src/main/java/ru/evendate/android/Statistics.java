package ru.evendate.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;

/**
 * Created by Aedirn on 21.06.16.
 */
public class Statistics {
    static Context sContext;

    public static final String INTENT_TYPE = "type";
    public static final String NOTIFICATION = "notification";

    public static void init(Context context){
        sContext = context;
    }
    public static void sendEventAction(Intent intent, String id){
        sendActionToStat(intent, id, sContext.getString(R.string.stat_category_event));
    }

    public static void sendOrganizationAction(Intent intent, String id){
        sendActionToStat(intent, id, sContext.getString(R.string.stat_category_organization));
    }

    private static void sendActionToStat(Intent intent, String id, String category){
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setLabel(id);
        setActionType(intent, event);
        EvendateApplication.getTracker().send(event.build());
    }

    private static void setActionType(Intent intent, HitBuilders.EventBuilder event){
        Bundle intent_extras = intent.getExtras();
        if (intent_extras != null && intent_extras.containsKey(INTENT_TYPE) &&
                intent.getStringExtra(INTENT_TYPE).equals(NOTIFICATION)) {
            event.setAction(sContext.getString(R.string.stat_action_notification));
        } else {
            event.setAction(sContext.getString(R.string.stat_action_view));
        }
    }
}
