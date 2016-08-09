package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

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
