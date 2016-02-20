package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
public class Action extends DataModel {
    public static final String FIELDS_LIST = "name,created_at";

    public static final int ACTION_LIKE = 0;
    public static final int ACTION_DISLIKE = 1;
    public static final int ACTION_SUBSCRIBE = 2;
    public static final int ACTION_UNSUBSCRIBE = 3;

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
    //public DataModel getTarget(){
//
    //}
}
