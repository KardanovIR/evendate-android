package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class Tag extends DataModel {
    @SerializedName("id")
    int tagId;
    String name;
    String eventsCount;

    @Override
    public int getEntryId() {
        return this.tagId;
    }

    public String getName() {
        return name;
    }

    public String getEventsCount() {
        return eventsCount;
    }
}