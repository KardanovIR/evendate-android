package ru.evendate.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.List;

import ru.evendate.android.models.StatisticsEvent;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Aedirn on 21.06.16.
 */
public class Statistics {
    private static final String LOG_TAG = "Statistics";
    static Context sContext;

    public static final String INTENT_TYPE = "type";
    public static final String NOTIFICATION = "notification";

    public static void init(Context context) {
        sContext = context;
    }

    public static void sendEventAction(Intent intent, String id) {
        sendActionToStat(intent, id, sContext.getString(R.string.stat_category_event));
    }

    public static void sendOrganizationAction(Intent intent, String id) {
        sendActionToStat(intent, id, sContext.getString(R.string.stat_category_organization));
    }

    private static void sendActionToStat(Intent intent, String id, String category) {
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setLabel(id);
        String actionType = getActionType(intent);
        event.setAction(actionType);
        EvendateApplication.getTracker().send(event.build());
        if (actionType.equals(sContext.getString(R.string.stat_action_notification)))
            sendOpenNotification(Integer.valueOf(id), category);
    }

    private static String getActionType(Intent intent) {
        Bundle intent_extras = intent.getExtras();
        if (intent_extras != null && intent_extras.containsKey(INTENT_TYPE) &&
                intent.getStringExtra(INTENT_TYPE).equals(NOTIFICATION)) {
            return sContext.getString(R.string.stat_action_notification);
        } else {
            return sContext.getString(R.string.stat_action_view);
        }
    }

    /**
     * Evendate stats
     */
    public static void sendEventOpenMap(int eventId) {
        String entity = StatisticsEvent.ENTITY_EVENT;
        String event = StatisticsEvent.EVENT_OPEN_MAP;
        postEvent(createList(eventId, entity, event));
    }

    public static void sendOrgOpenMap(int orgId) {
        String entity = StatisticsEvent.ENTITY_ORG;
        String event = StatisticsEvent.EVENT_OPEN_MAP;
        postEvent(createList(orgId, entity, event));
    }

    public static void sendEventOpenSite(int eventId) {
        String entity = StatisticsEvent.ENTITY_EVENT;
        String event = StatisticsEvent.EVENT_OPEN_SITE;
        postEvent(createList(eventId, entity, event));
    }

    public static void sendOrgOpenSite(int organizationId) {
        String entity = StatisticsEvent.ENTITY_ORG;
        String event = StatisticsEvent.EVENT_OPEN_SITE;
        postEvent(createList(organizationId, entity, event));
    }

    public static void sendEventView(int eventId) {
        String entity = StatisticsEvent.ENTITY_EVENT;
        String event = StatisticsEvent.EVENT_OPEN_SITE;
        postEvent(createList(eventId, entity, event));
    }

    public static void sendOpenNotification(int id, String category) {
        if (category.equals(sContext.getString(R.string.stat_category_event)))
            sendEventOpenNotification(id);
        //if(category.equals(sContext.getString(R.string.stat_category_organization)))
        //    sendOrgOpenNotification(id);
    }

    public static void sendEventOpenNotification(int eventId) {
        String entity = StatisticsEvent.ENTITY_EVENT;
        String event = StatisticsEvent.EVENT_OPEN_NOTIFICATION;
        postEvent(createList(eventId, entity, event));
    }

    public static void sendOrgOpenNotification(int eventId) {
        String entity = StatisticsEvent.ENTITY_ORG;
        String event = StatisticsEvent.EVENT_OPEN_NOTIFICATION;
        postEvent(createList(eventId, entity, event));
    }

    public static void sendEventHideNotification(int eventId) {
        String entity = StatisticsEvent.ENTITY_EVENT;
        String event = StatisticsEvent.EVENT_HIDE_NOTIFICATION;
        postEvent(createList(eventId, entity, event));
    }

    private static List<StatisticsEvent> createList(long entryId, String entityType, String eventType) {
        Log.d(LOG_TAG, "posting " + entityType + " " + entryId + " " + eventType);
        ArrayList<StatisticsEvent> events = new ArrayList<>();
        events.add(new StatisticsEvent(entryId, entityType, eventType));
        return events;
    }

    private static void postEvent(List<StatisticsEvent> events) {
        ApiService apiService = ApiFactory.getService(sContext);
        Observable<Response> eventObservable =
                apiService.postStat(EvendateAccountManager.peekToken(sContext), events);
        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isOk())
                        Log.d(LOG_TAG, "posted");
                    else
                        Log.e(LOG_TAG, "error");
                }, error -> Log.e(LOG_TAG, error.getMessage()));
    }
}
