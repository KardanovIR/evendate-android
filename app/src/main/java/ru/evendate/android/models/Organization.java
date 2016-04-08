package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class Organization extends DataModel {

    @SerializedName("id")
    int organizationId;
    String name;
    @SerializedName("short_name")
    String shortName;
    @SerializedName("img_url")
    String logoLargeUrl;
    @SerializedName("background_img_url")
    String backgroundLargeUrl;
    @SerializedName("type_id")
    int typeId;
    @SerializedName("type_name")
    String typeName;
    @SerializedName("organization_type_order")
    String typeOrder;

    @Override
    public int getEntryId() {
        return this.organizationId;
    }
    public String getName() {
        return name;
    }
    public String getShortName() {
        return shortName;
    }
    public String getLogoUrl() {
        return logoLargeUrl;
    }
    public String getBackgroundUrl() {
        return backgroundLargeUrl;
    }
    public int getTypeId() {
        return typeId;
    }
    public String getTypeName() {
        return typeName;
    }
    public String getTypeOrder() {
        return typeOrder;
    }
}
