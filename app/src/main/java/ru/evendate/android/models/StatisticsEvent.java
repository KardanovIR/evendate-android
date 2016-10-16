package ru.evendate.android.models;

/**
 * Created by Aedirn on 24.06.16.
 */
public class StatisticsEvent {

    public enum EntityType {
        ENTITY_EVENT("event"),
        ENTITY_ORG("organization"),
        ENTITY_FRIEND("friend");

        final String name;

        EntityType(String name) {
            this.name = name;
        }
    }

    public enum EventType {
        EVENT_VIEW("view"),
        //EVENT_UNFAVE("unfave"),
        //EVENT_FAVE("fave"),
        //EVENT_VIEW_DETAIL("view_detail"),
        EVENT_SHARE_VK("share_vk"),
        EVENT_SHARE_FB("share_fb"),
        EVENT_SHARE_TW("share_tw"),
        EVENT_OPEN_SITE("open_site"),
        EVENT_OPEN_NOTIFICATION("open_notification"),
        EVENT_HIDE_NOTIFICATION("hide_notification"),
        EVENT_OPEN_MAP("open_map"),
        //EVENT_VIEW_SUBS("view_subscriptions"),
        //EVENT_VIEW_ACTIONS("view_actions"),
        EVENT_VIEW_EVENT("view_event_from_user"),
        //EVENT_SUBSCRIBE("subscribeOrgAndChangeState"),
        //EVENT_UNSUBSCRIBE("unsubscribe")
        ;

        final String name;

        EventType(String name) {
            this.name = name;
        }

    }


    final long entity_id;
    final String entity_type;
    final String event_type;

    public StatisticsEvent(long entity_id, EntityType entity_type, EventType event_type) {
        this.entity_id = entity_id;
        this.entity_type = entity_type.name;
        this.event_type = event_type.name;
    }
}
