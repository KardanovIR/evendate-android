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
    public final int organization_id;
    public final String name;
    public final String img_url; //logo
    public String img_medium_url;
    public String img_small_url;
    public final String short_name;
    public final String description;
    public final String type_name;
    public String background_img_url;
    public String background_medium_img_url;
    public String background_small_img_url;
    public boolean status;
    public int type_id;
    public final int subscribed_count;
    public Integer subscription_id;
    public final boolean is_subscribed;
    public int timestamp_updated_at;

    public OrganizationEntry(int organization_id, String name, String img_url, String short_name,
                      String description, String type_name, int subscribed_count, Integer subscription_id,
                             boolean is_subscribed, String background_img_url, int updated_at) {
        this.organization_id = organization_id;
        this.name = name;
        this.img_url = img_url;
        this.short_name = short_name;
        this.description = description;
        this.type_name = type_name;
        this.subscribed_count = subscribed_count;
        this.subscription_id = subscription_id;
        this.is_subscribed = is_subscribed;
        this.timestamp_updated_at = updated_at;
        this.background_img_url = background_img_url;
    }

    public String getLogoUrl() {
        return img_url;
    }

    public String getBackgroundImgUrl() {
        return background_img_url;
    }

    public int updatedAt() {
        return timestamp_updated_at;
    }

    @Override
    public int getEntryId() {
        return this.organization_id;
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
                this.img_url.equals(tmp.img_url) &&
                this.short_name.equals(tmp.short_name) &&
                this.type_name.equals(tmp.type_name) &&
                this.description.equals(tmp.description) &&
                this.subscribed_count == tmp.subscribed_count &&
                this.subscription_id.equals(tmp.subscription_id) &&
                this.is_subscribed == tmp.is_subscribed
        );
    }

    @Override
    public ContentProviderOperation getUpdate(final Uri ContentUri) {
        return ContentProviderOperation.newUpdate(ContentUri)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_NAME, this.name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IMG_URL, this.img_url)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME, this.short_name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME, this.type_name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIPTION_ID, this.subscription_id)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IS_SUBSCRIBED, this.is_subscribed)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT, this.is_subscribed)
                .build();
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return ContentProviderOperation.newInsert(ContentUri)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID, this.organization_id)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_NAME, this.name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IMG_URL, this.img_url)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME, this.short_name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION, this.description)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME, this.type_name)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIPTION_ID, this.subscription_id)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_IS_SUBSCRIBED, this.is_subscribed)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT, this.is_subscribed)
                .build();
    }
}
