package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class TagModel extends DataModel {
    @SerializedName("id")
    int tagId;
    String name;
    String events_count;

    @Override
    public int getEntryId() {
        return this.tagId;
    }
    public String getName() {
        return name;
    }
    public String getEvents_count() {
        return events_count;
    }
}