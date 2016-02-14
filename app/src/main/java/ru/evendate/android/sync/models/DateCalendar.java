package ru.evendate.android.sync.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class DateCalendar extends Date {
    public static final String FIELDS_LIST = "events_count,favored_count";
    @SerializedName("events_count")
    long eventCount;
    @SerializedName("favored_count")
    long favoredCount;

    public DateCalendar(long eventDate) {
        super(eventDate);
    }

    public long getEventCount() {
        return eventCount;
    }

    public long getFavoredCount() {
        return favoredCount;
    }
}
