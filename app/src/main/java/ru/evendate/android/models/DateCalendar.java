package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Dmitry on 08.02.2016.
 */
@Parcel
@SuppressWarnings("WeakerAccess")
public class DateCalendar extends EventDate {
    @SerializedName("events_count")
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
