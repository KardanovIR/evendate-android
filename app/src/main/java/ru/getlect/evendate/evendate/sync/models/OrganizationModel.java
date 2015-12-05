package ru.getlect.evendate.evendate.sync.models;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import org.chalup.microorm.annotations.Column;

import java.util.ArrayList;

import ru.getlect.evendate.evendate.data.EvendateContract.OrganizationEntry;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class OrganizationModel extends DataModel {

    @SerializedName("subscribed_friends")
    ArrayList<FriendModel> mSubscribedUsersList;

    @Column(OrganizationEntry.COLUMN_ORGANIZATION_ID)
    @SerializedName("id")
    int organizationId;
    @Column(OrganizationEntry.COLUMN_NAME)
    String name;
    @Column(OrganizationEntry.COLUMN_SHORT_NAME)
    @SerializedName("short_name")
    String shortName;
    @Column(OrganizationEntry.COLUMN_DESCRIPTION)
    String description;
    @Column(OrganizationEntry.COLUMN_SITE_URL)
    @SerializedName("site_url")
    String siteUrl;

    @SerializedName("img_url")
    String logoLargeUrl;
    @SerializedName("img_medium_url")
    String logoMediumUrl;
    @Column(OrganizationEntry.COLUMN_LOGO_URL)
    @SerializedName("img_small_url")
    String logoSmallUrl;

    @SerializedName("background_img_url")
    String backgroundLargeUrl;
    @Column(OrganizationEntry.COLUMN_BACKGROUND_URL)
    @SerializedName("background_medium_img_url")
    String backgroundMediumUrl;
    @SerializedName("background_small_img_url")
    String backgroundSmallUrl;

    @Column(OrganizationEntry.COLUMN_TYPE_ID)
    @SerializedName("type_id")
    int typeId;
    @Column(OrganizationEntry.COLUMN_TYPE_NAME)
    @SerializedName("type_name")
    String typeName;

    @Column(OrganizationEntry.COLUMN_SUBSCRIBED_COUNT)
    @SerializedName("subscribed_count")
    int subscribedCount;
    @Column(OrganizationEntry.COLUMN_SUBSCRIPTION_ID)
    @SerializedName("subscription_id")
    Integer subscriptionId;
    @Column(OrganizationEntry.COLUMN_IS_SUBSCRIBED)
    @SerializedName("is_subscribed")
    boolean isSubscribed;

    @Column(OrganizationEntry.COLUMN_UPDATED_AT)
    @SerializedName("timestamp_updated_at")
    long updatedAt;
    @Column(OrganizationEntry.COLUMN_CREATED_AT)
    @SerializedName("timestamp_created_at")
    long createdAt;

    public ArrayList<FriendModel> getSubscribedFriends() {
        return mSubscribedUsersList;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDescription() {
        return description;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getSubscribedCount() {
        return subscribedCount;
    }

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setIsSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public void setSubscribedCount(int subscribedCount) {
        this.subscribedCount = subscribedCount;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getLogoLargeUrl() {
        return logoLargeUrl;
    }

    public String getLogoMediumUrl() {
        return logoMediumUrl;
    }

    public String getLogoSmallUrl() {
        return logoSmallUrl;
    }

    public String getBackgroundLargeUrl() {
        return backgroundLargeUrl;
    }

    public String getBackgroundMediumUrl() {
        return backgroundMediumUrl;
    }

    public String getBackgroundSmallUrl() {
        return backgroundSmallUrl;
    }

    public long updatedAt() {
        return updatedAt;
    }

    public void setSubscriptionId(Integer subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public int getEntryId() {
        return this.organizationId;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!(getClass() == obj.getClass()))
            return false;

        OrganizationModel tmp = (OrganizationModel) obj;
        return (this.name.equals(tmp.name) &&
                this.shortName.equals(tmp.shortName) &&
                this.description.equals(tmp.description) &&
                (this.siteUrl != null ? this.siteUrl.equals(tmp.siteUrl) : tmp.siteUrl == null) &&
                this.typeId == tmp.typeId &&
                this.typeName.equals(tmp.typeName) &&

                this.subscribedCount == tmp.subscribedCount &&
                (this.subscriptionId != null ? this.subscriptionId.equals(tmp.subscriptionId) : tmp.subscriptionId == null) &&
                this.isSubscribed == tmp.isSubscribed &&

                this.updatedAt == tmp.updatedAt &&
                this.createdAt == tmp.createdAt
        );
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return fillWithData(ContentProviderOperation.newInsert(ContentUri))
                .withValue(OrganizationEntry.COLUMN_ORGANIZATION_ID, this.organizationId)
                .build();
    }

    protected ContentProviderOperation.Builder fillWithData(ContentProviderOperation.Builder operation){
        return operation
                .withValue(OrganizationEntry.COLUMN_NAME, this.name)
                .withValue(OrganizationEntry.COLUMN_SHORT_NAME, this.shortName)
                .withValue(OrganizationEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(OrganizationEntry.COLUMN_SITE_URL, this.siteUrl)

                .withValue(OrganizationEntry.COLUMN_LOGO_URL, this.logoMediumUrl)
                .withValue(OrganizationEntry.COLUMN_BACKGROUND_URL, this.backgroundMediumUrl)

                .withValue(OrganizationEntry.COLUMN_TYPE_ID, this.typeId)
                .withValue(OrganizationEntry.COLUMN_TYPE_NAME, this.typeName)

                .withValue(OrganizationEntry.COLUMN_SUBSCRIBED_COUNT, this.subscribedCount)
                .withValue(OrganizationEntry.COLUMN_SUBSCRIPTION_ID, this.subscriptionId)
                .withValue(OrganizationEntry.COLUMN_IS_SUBSCRIBED, this.isSubscribed)

                .withValue(OrganizationEntry.COLUMN_UPDATED_AT, this.updatedAt)
                .withValue(OrganizationEntry.COLUMN_CREATED_AT, this.createdAt);
    }
    public ContentValues getContentValues(){
        ContentValues contentValues =  new ContentValues();
        contentValues.put(OrganizationEntry.COLUMN_NAME, this.name);
        contentValues.put(OrganizationEntry.COLUMN_SHORT_NAME, this.shortName);
        contentValues.put(OrganizationEntry.COLUMN_DESCRIPTION, this.description);
        contentValues.put(OrganizationEntry.COLUMN_SITE_URL, this.siteUrl);
        contentValues.put(OrganizationEntry.COLUMN_LOGO_URL, this.logoMediumUrl);
        contentValues.put(OrganizationEntry.COLUMN_BACKGROUND_URL, this.backgroundMediumUrl);
        contentValues.put(OrganizationEntry.COLUMN_TYPE_ID, this.typeId);
        contentValues.put(OrganizationEntry.COLUMN_TYPE_NAME, this.typeName);
        contentValues.put(OrganizationEntry.COLUMN_SUBSCRIBED_COUNT, this.subscribedCount);
        contentValues.put(OrganizationEntry.COLUMN_SUBSCRIPTION_ID, this.subscriptionId);
        contentValues.put(OrganizationEntry.COLUMN_IS_SUBSCRIBED, this.isSubscribed);
        contentValues.put(OrganizationEntry.COLUMN_UPDATED_AT, this.updatedAt);
        contentValues.put(OrganizationEntry.COLUMN_CREATED_AT, this.createdAt);
        return contentValues;
    }
}
