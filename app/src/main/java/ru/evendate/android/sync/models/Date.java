package ru.evendate.android.sync.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 07.02.2016.
 */
public class Date extends DataModel{
    @SerializedName("event_date")
    long eventDate;

    public long getEventDate() {
        return eventDate;
    }

    @Override
    public int getEntryId() {
        return 0;
    }
}