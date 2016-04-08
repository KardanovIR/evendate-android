package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class DateCalendar extends Date {
    @SerializedName("events_count")
    long eventCount;
    @SerializedName("favorites_count")
    long favoredCount;

    public long getEventCount() {
        return eventCount;
    }

    public long getFavoredCount() {
        return favoredCount;
    }
}
