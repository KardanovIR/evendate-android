package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by Dmitry on 08.02.2016.
 */
@Parcel
@SuppressWarnings("WeakerAccess")
public class OrganizationCategory extends DataModel {
    public static final String FIELDS_LIST = "organizations{fields:'" + OrganizationSubscription.FIELDS_LIST + "'}";

    @SerializedName("id")
    int typeId;
    String name;
    @SerializedName("order_position")
    String orderPosition;

    ArrayList<OrganizationFull> organizations;

    @Override
    public int getEntryId() {
        return this.typeId;
    }

    public String getName() {
        return name;
    }

    public String getOrderPosition() {
        return orderPosition;
    }

    public ArrayList<OrganizationFull> getOrganizations() {
        return new ArrayList<>(organizations);
    }
}
