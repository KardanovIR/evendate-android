package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class Event extends DataModel {

    @SerializedName("id")
    int eventId;
    String title;
    @SerializedName("first_event_date")
    long firstDate;
    @SerializedName("last_event_date")
    long lastDate;
    @SerializedName("nearest_event_date")
    long nearestDate;
    @SerializedName("image_horizontal_url")
    String imageHorizontalUrl;
    @SerializedName("image_vertical_url")
    String imageVerticalUrl;
    @SerializedName("organization_id")
    int organizationId;

    @Override
    public int getEntryId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public long getFirstDate() {
        return firstDate;
    }

    public long getLastDate() {
        return lastDate;
    }

    public long getNearestDate() {
        return nearestDate;
    }

    public String getImageHorizontalUrl() {
        return imageHorizontalUrl;
    }

    public String getImageVerticalUrl() {
        return imageVerticalUrl;
    }

    public int getOrganizationId() {
        return organizationId;
    }
}
