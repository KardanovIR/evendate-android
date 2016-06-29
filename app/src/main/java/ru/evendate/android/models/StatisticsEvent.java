package ru.evendate.android.models;

/**
 * Created by Aedirn on 24.06.16.
 */
public class StatisticsEvent {

    public static final String ENTITY_EVENT = "event";
    public static final String ENTITY_ORG = "organization";
    public static final String ENTITY_FRIEND = "friend";

    public static final String EVENT_VIEW = "view";
    public static final String EVENT_UNFAVE = "unfave";
    public static final String EVENT_FAVE = "fave";
    public static final String EVENT_VIEW_DETAIL = "view_detail";
    public static final String EVENT_SHARE_VK = "share_vk";
    public static final String EVENT_SHARE_FB = "share_fb";
    public static final String EVENT_SHARE_TW = "share_tw";
    public static final String EVENT_OPEN_SITE = "open_site";
    public static final String EVENT_OPEN_NOTIFICATION = "open_notification";
    public static final String EVENT_HIDE_NOTIFICATION = "hide_notification";
    public static final String EVENT_OPEN_MAP = "open_map";
    public static final String EVENT_VIEW_SUBS = "view_subscriptions";
    public static final String EVENT_VIEW_ACTIONS = "view_actions";
    public static final String EVENT_VIEW_EVENT = "view_event_from_user";
    public static final String EVENT_SUBSCRIBE = "subscribe";
    public static final String EVENT_UNSUBSCRIBE = "unsubscribe";


    final long entity_id;
    final String entity_type;
    final String event_type;

    public StatisticsEvent(long entity_id, String entity_type, String event_type) {
        this.entity_id = entity_id;
        this.entity_type = entity_type;
        this.event_type = event_type;
    }
}
