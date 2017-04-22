package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class DateCalendar extends EventDate {
    @SerializedName("eventsCount")
    int eventCount;
    @SerializedName("favorites_count")
    int favoredCount;

    public int getEventCount() {
        return eventCount;
    }

    public int getFavoredCount() {
        return favoredCount;
    }
}
