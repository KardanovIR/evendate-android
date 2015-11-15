package ru.getlect.evendate.evendate.sync.dataTypes;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class EventEntry extends DataEntry {
    @SerializedName("tags")
    ArrayList<TagEntry> mTagList;
    @SerializedName("favorite_friends")
    ArrayList<FriendEntry> mFriendList;
    @SerializedName("id")
    int event_id;
    String title;
    String description;
    String location;
    String location_uri;
    String event_start_date;
    String notifications_schema_json;
    int organization_id;
    double latitude;
    double longitude;
    String event_end_date;
    String detail_info_url;
    String begin_time;
    String end_time;
    String location_object;
    boolean can_edit;
    String event_type_latin_name;
    boolean is_favorite;
    String image_horizontal_url;
    String image_vertical_url;
    int timestamp_updated_at;


    public EventEntry(int event_id, String title, String description, String location,
                      String location_uri, String event_start_date, String notifications_schema_json,
                      int organization_id, double latitude, double longitude, String event_end_date,
                      String detail_info_url, String begin_time, String end_time, String location_object,
                      boolean can_edit, String event_type_latin_name, boolean is_favorite,
                      String image_horizontal_url, String image_vertical_url, int timestamp_updated_at){
        this.event_id = event_id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.location_uri = location_uri;
        this.event_start_date = event_start_date;
        this.notifications_schema_json = notifications_schema_json;
        this.organization_id = organization_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.event_end_date = event_end_date;
        this.detail_info_url = detail_info_url;
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.location_object = location_object;
        this.can_edit = can_edit;
        this.event_type_latin_name = event_type_latin_name;
        this.is_favorite = is_favorite;
        this.image_horizontal_url = image_horizontal_url;
        this.image_vertical_url = image_vertical_url;
        this.timestamp_updated_at = timestamp_updated_at;
    }

    public String getImageHorizontalUrl() {
        return image_horizontal_url;
    }

    public int getUpdatedAt() {
        return timestamp_updated_at;
    }

    public ArrayList<TagEntry> getTagList() {
        return mTagList;
    }

    public void setTagList(ArrayList<TagEntry> tagList) {
        this.mTagList = tagList;
    }

    public ArrayList<FriendEntry> getFriendList() {
        return mFriendList;
    }

    public void setFriendList(ArrayList<FriendEntry> mFriendList) {
        this.mFriendList = mFriendList;
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public void setId(int id) {
        super.setId(id);
    }

    @Override
    public int getEntryId() {
        return event_id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

    /* obj ссылается на null */
        if (obj == null)
            return false;

    /* Удостоверимся, что ссылки имеют тот же самый тип */
        if (!(getClass() == obj.getClass()))
            return false;
        EventEntry tmp = (EventEntry) obj;
        return (this.title.equals(tmp.title) &&
                this.description.equals(tmp.description) &&
                this.location.equals(tmp.location) &&
                this.location_uri.equals(tmp.location_uri) &&
                this.event_start_date.equals(tmp.event_start_date) &&
                this.notifications_schema_json.equals(tmp.notifications_schema_json) &&
                this.organization_id == tmp.organization_id &&
                this.latitude == tmp.latitude &&
                this.longitude == tmp.longitude &&
                this.event_end_date.equals(tmp.event_end_date) &&
                this.detail_info_url.equals(tmp.detail_info_url) &&
                this.begin_time.equals(tmp.begin_time) &&
                this.end_time.equals(tmp.end_time) &&
                this.location_object.equals(tmp.location_object) &&
                this.can_edit == tmp.can_edit &&
                this.event_type_latin_name.equals(tmp.event_type_latin_name) &&
                this.is_favorite == tmp.is_favorite &&
                this.image_horizontal_url.equals(tmp.image_horizontal_url) &&
                this.image_vertical_url.equals(tmp.image_vertical_url)
        );
    }

    @Override
    public ContentProviderOperation getUpdate(Uri ContentUri) {

        return ContentProviderOperation.newUpdate(ContentUri)
                .withValue(EvendateContract.EventEntry.COLUMN_TITLE, this.title)
                .withValue(EvendateContract.EventEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(EvendateContract.EventEntry.COLUMN_LATITUDE, this.latitude)
                .withValue(EvendateContract.EventEntry.COLUMN_LONGITUDE, this.longitude)
                .withValue(EvendateContract.EventEntry.COLUMN_LOCATION_TEXT, this.location)
                .withValue(EvendateContract.EventEntry.COLUMN_LOCATION_URI, this.location_uri)
                .withValue(EvendateContract.EventEntry.COLUMN_LOCATION_JSON, this.location_object)
                .withValue(EvendateContract.EventEntry.COLUMN_NOTIFICATIONS, this.notifications_schema_json)
                .withValue(EvendateContract.EventEntry.COLUMN_START_DATE, this.event_start_date)
                .withValue(EvendateContract.EventEntry.COLUMN_BEGIN_TIME, this.begin_time)
                .withValue(EvendateContract.EventEntry.COLUMN_END_TIME, this.end_time)
                .withValue(EvendateContract.EventEntry.COLUMN_END_DATE, this.event_end_date)
                .withValue(EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID, this.organization_id)
                .withValue(EvendateContract.EventEntry.COLUMN_IMAGE_VERTICAL_URL, this.image_vertical_url)
                .withValue(EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL, this.detail_info_url)
                .withValue(EvendateContract.EventEntry.COLUMN_IMAGE_HORIZONTAL_URL, this.image_horizontal_url)
                .withValue(EvendateContract.EventEntry.COLUMN_CAN_EDIT, this.can_edit)
                .withValue(EvendateContract.EventEntry.COLUMN_EVENT_TYPE, this.event_type_latin_name)
                .withValue(EvendateContract.EventEntry.COLUMN_IS_FAVORITE, this.is_favorite)
                .build();
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return ContentProviderOperation.newInsert(ContentUri)
                .withValue(EvendateContract.EventEntry.COLUMN_EVENT_ID, this.event_id)
                .withValue(EvendateContract.EventEntry.COLUMN_TITLE, this.title)
                .withValue(EvendateContract.EventEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(EvendateContract.EventEntry.COLUMN_LATITUDE, this.latitude)
                .withValue(EvendateContract.EventEntry.COLUMN_LONGITUDE, this.longitude)
                .withValue(EvendateContract.EventEntry.COLUMN_LOCATION_TEXT, this.location)
                .withValue(EvendateContract.EventEntry.COLUMN_LOCATION_URI, this.location_uri)
                .withValue(EvendateContract.EventEntry.COLUMN_LOCATION_JSON, this.location_object)
                .withValue(EvendateContract.EventEntry.COLUMN_NOTIFICATIONS, this.notifications_schema_json)
                .withValue(EvendateContract.EventEntry.COLUMN_START_DATE, this.event_start_date)
                .withValue(EvendateContract.EventEntry.COLUMN_BEGIN_TIME, this.begin_time)
                .withValue(EvendateContract.EventEntry.COLUMN_END_TIME, this.end_time)
                .withValue(EvendateContract.EventEntry.COLUMN_END_DATE, this.event_end_date)
                .withValue(EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID, this.organization_id)
                .withValue(EvendateContract.EventEntry.COLUMN_IMAGE_VERTICAL_URL, this.image_vertical_url)
                .withValue(EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL, this.detail_info_url)
                .withValue(EvendateContract.EventEntry.COLUMN_IMAGE_HORIZONTAL_URL, this.image_horizontal_url)
                .withValue(EvendateContract.EventEntry.COLUMN_CAN_EDIT, this.can_edit)
                .withValue(EvendateContract.EventEntry.COLUMN_EVENT_TYPE, this.event_type_latin_name)
                .withValue(EvendateContract.EventEntry.COLUMN_IS_FAVORITE, this.is_favorite)
                .build();
    }
}
