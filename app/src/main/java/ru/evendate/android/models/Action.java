package ru.evendate.android.models;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.ui.utils.DateUtils;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
@Parcel
@SuppressWarnings("WeakerAccess")
public class Action extends DataModel implements ActionTarget, Comparable<Action> {
    public static final String FIELDS_LIST = "name,created_at,event{fields:'image_horizontal_small_url,image_horizontal_medium_url'},organization{fields:'" + OrganizationSubscription.FIELDS_LIST + "'},user{fields:'" + User.FIELDS_LIST + "'}";
    public static final String ORDER_BY = "-created_at";
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
    Event event;
    OrganizationFull organization;
    User user;
    @SerializedName("created_at")
    int createdAt;

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

    public Date getDate() {
        return DateUtils.date(createdAt);
    }

    public Event getEvent() {
        return event;
    }

    public OrganizationSubscription getOrganization() {
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
            return getEvent().getImageHorizontalSmallUrl();
        else if (statTypeId == Type.ACTION_SUBSCRIBE.type() || statTypeId == Type.ACTION_UNSUBSCRIBE.type())
            return organization.getLogoSmallUrl();
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
    public int compareTo(@NonNull Action another) {
        return createdAt > another.createdAt ? 1 : createdAt == another.createdAt ? 0 : -1;
    }
}
