package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class OrganizationModel extends DataModel implements Organization {

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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getLogoLargeUrl() {
        return logoLargeUrl;
    }

    @Override
    public String getBackgroundLargeUrl() {
        return backgroundLargeUrl;
    }

    @Override
    public int getTypeId() {
        return typeId;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String getTypeOrder() {
        return typeOrder;
    }
}
