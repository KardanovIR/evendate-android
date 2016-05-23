package ru.evendate.android.models;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import ru.evendate.android.data.EvendateContract;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
public class Action extends DataModel implements ActionTarget, Comparable<Action> {
    public static final String FIELDS_LIST = "name,created_at,event,organization,user{fields:'" + User.FIELDS_LIST + "'}";
    public static final String ORDER_BY = "-created_at";

    public enum Type {
        ACTION_LIKE(5),
        ACTION_DISLIKE(4),
        ACTION_SUBSCRIBE(3),
        ACTION_UNSUBSCRIBE(6),
        ACTION_VIEW_EVENT(2),
        ACTION_VIEW_ORGANIZATION(1),
        ACTION_VIEW_EVENT_INFO(7);

        final int type;

        Type(int type) {
            this.type = type;
        }

        public int type() {
            return type;
        }
    }

    @SerializedName("stat_type_id")
    long statTypeId;
    @SerializedName("organization_id")
    Long organizationId;
    @SerializedName("event_id")
    Long eventId;
    @SerializedName("user_id")
    long userId;
    @SerializedName("entity")
    String entity;

    String name;
    @SerializedName("created_at")
    long createdAt;

    Event event;
    Organization organization;
    User user;

    @Override
    public int getEntryId() {
        throw new IllegalArgumentException("Action has not id cause api restrictions");
    }

    public long getTypeId() {
        return statTypeId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getEventId() {
        return eventId;
    }

    public long getUserId() {
        return userId;
    }

    public String getEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }

    public long getDate() {
        return createdAt;
    }

    public Event getEvent() {
        return event;
    }

    public Organization getOrganization() {
        return organization;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getTargetName() {
        if (statTypeId == Type.ACTION_DISLIKE.type() || statTypeId == Type.ACTION_LIKE.type())
            return event.getTitle();
        else if (statTypeId == Type.ACTION_SUBSCRIBE.type() || statTypeId == Type.ACTION_UNSUBSCRIBE.type())
            return organization.getShortName();
        else
            return null;
    }

    @Override
    public Uri getTargetUri() {
        if (statTypeId == Type.ACTION_DISLIKE.type() || statTypeId == Type.ACTION_LIKE.type())
            return EvendateContract.EventEntry.getContentUri(event.getEntryId());
        else if (statTypeId == Type.ACTION_SUBSCRIBE.type() || statTypeId == Type.ACTION_UNSUBSCRIBE.type())
            return EvendateContract.OrganizationEntry.getContentUri(organization.getEntryId());
        else
            return null;
    }

    @Override
    public String getTargetImageLink() {
        if (statTypeId == Type.ACTION_DISLIKE.type() || statTypeId == Type.ACTION_LIKE.type())
            return getEvent().getImageHorizontalUrl();
        else if (statTypeId == Type.ACTION_SUBSCRIBE.type() || statTypeId == Type.ACTION_UNSUBSCRIBE.type())
            return organization.getLogoUrl();
        else
            return null;
    }

    @Override
    public int getTargetType() {
        if (statTypeId == Type.ACTION_DISLIKE.type() || statTypeId == Type.ACTION_LIKE.type())
            return ActionTarget.TYPE_EVENT;
        else if (statTypeId == Type.ACTION_SUBSCRIBE.type() || statTypeId == Type.ACTION_UNSUBSCRIBE.type())
            return ActionTarget.TYPE_ORGANIZATION;
        return 0;
    }

    @Override
    public int compareTo(Action another) {
        return createdAt > another.createdAt ? 1 : createdAt == another.createdAt ? 0 : -1;
    }
}
