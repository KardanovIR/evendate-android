package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
public class Action extends DataModel {
    public static final String FIELDS_LIST = "name,created_at";

    @SerializedName("stat_type_id")
    long statTypeId;
    @SerializedName("organization_id")
    long organizationId;
    @SerializedName("event_id")
    long eventId;
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

    public long getStatTypeId() {
        return statTypeId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public long getEventId() {
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
}
