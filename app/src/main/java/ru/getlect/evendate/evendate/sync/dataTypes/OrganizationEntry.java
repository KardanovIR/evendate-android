package ru.getlect.evendate.evendate.sync.dataTypes;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class OrganizationEntry extends DataEntry {
    @SerializedName("id")
    public final int organizationId;
    public final String name;
    @SerializedName("short_name")
    public final String shortName;
    public final String description;
    @SerializedName("site_url")
    public String siteUrl;

    @SerializedName("img_url")
    public final String logoLargeUrl;
    @SerializedName("img_medium_url")
    public String logoMediumUrl;
    @SerializedName("img_small_url")
    public String logoSmallUrl;

    @SerializedName("background_img_url")
    public String backgroundLargeUrl;
    @SerializedName("background_medium_img_url")
    public String backgroundMediumUrl;
    @SerializedName("background_small_img_url")
    public String backgroundSmallUrl;

    @SerializedName("type_id")
    public int typeId;
    @SerializedName("type_name")
    public final String typeName;

    @SerializedName("subscribed_count")
    public final int subscribedCount;
    @SerializedName("subscription_id")
    public Integer subscriptionId;
    @SerializedName("is_subscribed")
    public final boolean isSubscribed;

    public boolean status;
    @SerializedName("timestamp_updated_at")
    public int timestampUpdatedAt;

    public OrganizationEntry(int organizationId, String name, String img_url, String short_name,
                      String description, String typeName, int subscribedCount, Integer subscriptionId,
                             boolean isSubscribed, String backgroundLargeUrl, int updated_at) {
        this.organizationId = organizationId;
        this.name = name;
        this.logoLargeUrl = img_url;
        this.shortName = short_name;
        this.description = description;
        this.typeName = typeName;
        this.subscribedCount = subscribedCount;
        this.subscriptionId = subscriptionId;
        this.isSubscribed = isSubscribed;
        this.timestampUpdatedAt = updated_at;
        this.backgroundLargeUrl = backgroundLargeUrl;
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

    public int updatedAt() {
        return timestampUpdatedAt;
    }

    @Override
    public int getEntryId() {
        return this.organizationId;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

    /* obj ссылается на null */
        if (obj == null)
            return false;

    /* Удостоверимся, что ссылки имеют тот же самый тип */
        if (!(getClass() == obj.getClass()))
            return false;
        OrganizationEntry tmp = (OrganizationEntry) obj;
        return (this.name.equals(tmp.name) &&
                this.logoLargeUrl.equals(tmp.logoLargeUrl) &&
                this.shortName.equals(tmp.shortName) &&
                this.typeName.equals(tmp.typeName) &&
                this.description.equals(tmp.description) &&
                this.subscribedCount == tmp.subscribedCount &&
                this.subscriptionId.equals(tmp.subscriptionId) &&
                this.isSubscribed == tmp.isSubscribed
        );
    }

    @Override
    public ContentProviderOperation getUpdate(final Uri ContentUri) {
        return ContentProviderOperation.newUpdate(ContentUri)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_NAME, this.name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IMG_URL, this.logoLargeUrl)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME, this.shortName)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME, this.typeName)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIPTION_ID, this.subscriptionId)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IS_SUBSCRIBED, this.isSubscribed)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT, this.isSubscribed)
                .build();
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return ContentProviderOperation.newInsert(ContentUri)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID, this.organizationId)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_NAME, this.name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IMG_URL, this.logoLargeUrl)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME, this.shortName)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME, this.typeName)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIPTION_ID, this.subscriptionId)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IS_SUBSCRIBED, this.isSubscribed)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT, this.isSubscribed)
                .build();
    }
}
