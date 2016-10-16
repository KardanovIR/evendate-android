package ru.evendate.android.statistics;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.evendate.android.models.StatisticsEvent;
import ru.evendate.android.models.StatisticsEvent.EntityType;
import ru.evendate.android.models.StatisticsEvent.EventType;

import static ru.evendate.android.network.ServiceImpl.postEvent;

/**
 * Evendate stats
 */
class EvendateStatisticsImpl implements EvendateStatistics {
    private static final String LOG_TAG = "EvendateStatisticsImpl";
    private Context mContext;

    EvendateStatisticsImpl(Context context) {
        mContext = context;
    }

    @Override
    public void sendEventViewFromFeed(int eventId) {
        postEvent(mContext, createList(eventId, EntityType.ENTITY_EVENT, EventType.EVENT_VIEW));
    }

    @Override
    public void sendEventOpenMap(int eventId) {
        postEvent(mContext, createList(eventId, EntityType.ENTITY_EVENT, EventType.EVENT_OPEN_MAP));
    }

    @Override
    public void sendOrgOpenMap(int orgId) {
        postEvent(mContext, createList(orgId, EntityType.ENTITY_ORG, EventType.EVENT_OPEN_MAP));
    }

    @Override
    public void sendEventClickLinkAction(int eventId) {
        postEvent(mContext, createList(eventId, EntityType.ENTITY_EVENT, EventType.EVENT_OPEN_SITE));
    }

    @Override
    public void sendOrganizationClickLinkAction(int organizationId) {
        postEvent(mContext, createList(organizationId, EntityType.ENTITY_ORG, EventType.EVENT_OPEN_SITE));
    }


    @Override
    public void sendEventOpenNotification(int eventId) {
        postEvent(mContext, createList(eventId, EntityType.ENTITY_EVENT, EventType.EVENT_OPEN_NOTIFICATION));
    }

    @Override
    public void sendEventHideNotification(int eventId) {
        postEvent(mContext, createList(eventId, EntityType.ENTITY_EVENT, EventType.EVENT_HIDE_NOTIFICATION));
    }

    /*
        create stat event list for api consistent
     */
    private List<StatisticsEvent> createList(long entryId, EntityType entityType, EventType eventType) {
        Log.d(LOG_TAG, "posting " + entityType + " " + entryId + " " + eventType);
        ArrayList<StatisticsEvent> events = new ArrayList<>();
        events.add(new StatisticsEvent(entryId, entityType, eventType));
        return events;
    }

}
