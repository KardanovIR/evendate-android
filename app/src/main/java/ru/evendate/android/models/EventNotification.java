package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ali Abdulmadzhidov on 16.05.2016.
 */
public class EventNotification extends DataModel {

    @SerializedName("uuid")
    int uuid;
    @SerializedName("event_id")
    int eventId;
    @SerializedName("notification_time")
    long notificationTime;

    public int getEventId() {
        return eventId;
    }

    public long getNotificationTime() {
        return notificationTime;
    }


    @Override
    public int getEntryId() {
        return uuid;
    }
}
