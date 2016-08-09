package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

public class EventNotification extends DataModel {
    public static final String FIELDS_LIST = "notification_type";

    @SerializedName("uuid")
    String uuid;
    @SerializedName("event_id")
    int eventId;
    @SerializedName("notification_time")
    long notificationTime;

    @SerializedName("notification_type")
    String notificationType;


    public int getEventId() {
        return eventId;
    }

    public long getNotificationTime() {
        return notificationTime;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public int getEntryId() {
        return 0;
    }
}
