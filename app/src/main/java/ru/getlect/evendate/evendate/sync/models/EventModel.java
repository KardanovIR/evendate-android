package ru.getlect.evendate.evendate.sync.models;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import org.chalup.microorm.annotations.Column;

import java.util.ArrayList;
import java.util.Date;

import ru.getlect.evendate.evendate.data.EvendateContract.EventEntry;
import ru.getlect.evendate.evendate.utils.Utils;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class EventModel extends DataModel {
    @SerializedName("tags")
    ArrayList<TagModel> mTagList;
    @SerializedName("favorite_friends")
    ArrayList<FriendModel> mFriendList;
    //"2015-10-26 00:00:00"
    @SerializedName("dates_range")
    ArrayList<String> mDataRangeList;

    @Column(EventEntry.COLUMN_EVENT_ID)
    @SerializedName("id")
    int eventId;
    @Column(EventEntry.COLUMN_TITLE)
    String title;
    @Column(EventEntry.COLUMN_DESCRIPTION)
    String description;
    @Column(EventEntry.COLUMN_ORGANIZATION_ID)
    @SerializedName("organization_id")
    int organizationId;

    //only for displaying events from server
    @SerializedName("organization_name")
    String organizationName;
    @SerializedName("organization_type_name")
    String organizationTypeName;
    @SerializedName("organization_short_name")
    String organizationShortName;
    @SerializedName("organization_img_url")
    String organizationLogoUrl;

    @Column(EventEntry.COLUMN_LOCATION_TEXT)
    String location;
    @Column(EventEntry.COLUMN_LOCATION_URI)
    @SerializedName("location_uri")
    String locationUri;
    @Column(EventEntry.COLUMN_LOCATION_JSON)
    @SerializedName("location_object")
    String locationObject;
    @Column(EventEntry.COLUMN_LATITUDE)
    double latitude;
    @Column(EventEntry.COLUMN_LONGITUDE)
    double longitude;


    @Column(EventEntry.COLUMN_DETAIL_INFO_URL)
    @SerializedName("detail_info_url")
    String detailInfoUrl;
    @Column(EventEntry.COLUMN_CAN_EDIT)
    @SerializedName("can_edit")
    boolean canEdit;
    @Column(EventEntry.COLUMN_IS_FAVORITE)
    @SerializedName("is_favorite")
    boolean isFavorite;
    @Column(EventEntry.COLUMN_LIKED_USERS_COUNT)
    @SerializedName("liked_users_count")
    int likedUsersCount;

    @Column(EventEntry.COLUMN_IMAGE_HORIZONTAL_URL)
    @SerializedName("image_horizontal_url")
    String imageHorizontalUrl;
    @Column(EventEntry.COLUMN_IMAGE_VERTICAL_URL)
    @SerializedName("image_vertical_url")
    String imageVerticalUrl;
    @Column(EventEntry.COLUMN_IMAGE_SQUARE_URL)
    @SerializedName("image_square_url")
    String imageSquareUrl;

    // TODO how to parse?
    @Column(EventEntry.COLUMN_NOTIFICATIONS)
    @SerializedName("notifications_schema_json")
    String notificationsSchemaJson;
    @Column(EventEntry.COLUMN_IS_FULL_DAY)
    @SerializedName("is_full_day")
    boolean isFullDay;
    @Column(EventEntry.COLUMN_BEGIN_TIME)
    @SerializedName("begin_time")
    String beginTime;
    @Column(EventEntry.COLUMN_END_TIME)
    @SerializedName("end_time")
    String endTime;
    @Column(EventEntry.COLUMN_FIRST_DATE)
    @SerializedName("first_date")
    String firstDate;
    @Column(EventEntry.COLUMN_START_DATE)
    @SerializedName("timestamp_event_start_date")
    long startDate;
    @Column(EventEntry.COLUMN_END_DATE)
    @SerializedName("timestamp_event_end_date")
    long endDate;
    @Column(EventEntry.COLUMN_UPDATED_AT)
    @SerializedName("timestamp_updated_at")
    long updatedAt;
    @Column(EventEntry.COLUMN_CREATED_AT)
    @SerializedName("timestamp_created_at")
    long createdAt;

    public int getOrganizationId() {
        return organizationId;
    }
    public String getImageHorizontalUrl() {
        return imageHorizontalUrl;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setTagList(ArrayList<TagModel> tagList) {
        this.mTagList = tagList;
    }
    public ArrayList<TagModel> getTagList() {
        return mTagList;
    }

    public void setFriendList(ArrayList<FriendModel> mFriendList) {
        this.mFriendList = mFriendList;
    }
    public ArrayList<FriendModel> getFriendList() {
        return mFriendList;
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
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public long getFirstDate() {
        Date date = Utils.formatDate(firstDate);
        if(date != null)
            return date.getTime();
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;
        if (!(getClass() == obj.getClass()))
            return false;

        EventModel tmp = (EventModel) obj;
        return (this.title.equals(tmp.title) &&
                this.description.equals(tmp.description) &&
                this.organizationId == tmp.organizationId &&

                this.location.equals(tmp.location) &&
                this.locationUri.equals(tmp.locationUri) &&
                this.latitude == tmp.latitude &&
                this.longitude == tmp.longitude &&
                this.locationObject.equals(tmp.locationObject) &&

                this.detailInfoUrl.equals(tmp.detailInfoUrl) &&
                this.canEdit == tmp.canEdit &&
                this.isFavorite == tmp.isFavorite &&
                this.likedUsersCount == tmp.likedUsersCount &&
                this.imageHorizontalUrl.equals(tmp.imageHorizontalUrl) &&
                this.imageVerticalUrl.equals(tmp.imageVerticalUrl) &&
                this.imageSquareUrl.equals(tmp.imageSquareUrl) &&

                this.notificationsSchemaJson.equals(tmp.notificationsSchemaJson) &&
                this.isFullDay == tmp.isFullDay &&
                this.beginTime != null ? this.beginTime.equals(tmp.beginTime) : tmp.beginTime == null &&
                this.endTime != null ? this.endTime.equals(tmp.endTime) : tmp.endTime == null &&
                this.getFirstDate() == tmp.getFirstDate() &&
                this.startDate == tmp.startDate &&
                this.endDate == tmp.endDate &&
                this.updatedAt == tmp.updatedAt &&
                this.createdAt == tmp.createdAt
        );
    }

    @Override
    public ContentProviderOperation getUpdate(Uri ContentUri) {
        return fillWithData(ContentProviderOperation.newUpdate(ContentUri)).build();
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return fillWithData(ContentProviderOperation.newInsert(ContentUri))
                .withValue(EventEntry.COLUMN_EVENT_ID, this.eventId)
                .build();
    }

    protected ContentProviderOperation.Builder fillWithData(ContentProviderOperation.Builder operation){
        return operation
                .withValue(EventEntry.COLUMN_TITLE, this.title)
                .withValue(EventEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(EventEntry.COLUMN_ORGANIZATION_ID, this.organizationId)

                .withValue(EventEntry.COLUMN_LOCATION_TEXT, this.location)
                .withValue(EventEntry.COLUMN_LOCATION_URI, this.locationUri)
                .withValue(EventEntry.COLUMN_LOCATION_JSON, this.locationObject)
                .withValue(EventEntry.COLUMN_LATITUDE, this.latitude)
                .withValue(EventEntry.COLUMN_LONGITUDE, this.longitude)

                .withValue(EventEntry.COLUMN_IMAGE_VERTICAL_URL, this.imageVerticalUrl)
                .withValue(EventEntry.COLUMN_IMAGE_HORIZONTAL_URL, this.imageHorizontalUrl)
                .withValue(EventEntry.COLUMN_IMAGE_SQUARE_URL, this.imageSquareUrl)

                .withValue(EventEntry.COLUMN_DETAIL_INFO_URL, this.detailInfoUrl)
                .withValue(EventEntry.COLUMN_CAN_EDIT, this.canEdit)
                .withValue(EventEntry.COLUMN_IS_FAVORITE, this.isFavorite)
                .withValue(EventEntry.COLUMN_LIKED_USERS_COUNT, this.likedUsersCount)

                .withValue(EventEntry.COLUMN_NOTIFICATIONS, this.notificationsSchemaJson)
                .withValue(EventEntry.COLUMN_IS_FULL_DAY, this.isFullDay)
                .withValue(EventEntry.COLUMN_BEGIN_TIME, this.beginTime)
                .withValue(EventEntry.COLUMN_END_TIME, this.endTime)
                .withValue(EventEntry.COLUMN_FIRST_DATE, this.getFirstDate())
                .withValue(EventEntry.COLUMN_START_DATE, this.startDate)
                .withValue(EventEntry.COLUMN_END_DATE, this.endDate)
                .withValue(EventEntry.COLUMN_UPDATED_AT, this.updatedAt)
                .withValue(EventEntry.COLUMN_CREATED_AT, this.createdAt);
    }
}
