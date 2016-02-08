package ru.evendate.android.sync.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class OrganizationType extends DataModel {
    public static final String FIELDS_LIST = "organizations";

    @SerializedName("id")
    int typeId;
    String name;
    @SerializedName("order_position")
    String orderPosition;

    ArrayList<OrganizationModel> organizations;

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
    public ArrayList<OrganizationModel> getOrganizations() {
        return organizations;
    }
}
