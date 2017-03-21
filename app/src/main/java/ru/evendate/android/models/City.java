package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Aedirn on 10.03.17.
 */

@Parcel
public class City extends DataModel {
    public static final String FIELDS_LIST = "distance";
    public static final String ORDER_BY_DISTANCE = "distance";

    @SerializedName("id")
    int cityId;
    @SerializedName("en_name")
    String name;
    @SerializedName("country_id")
    String countryId;
    @SerializedName("local_name")
    String nameLocally;
    String distance;

    @Override
    public int getEntryId() {
        return cityId;
    }

    public String getName() {
        return name;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getNameLocally() {
        return nameLocally;
    }

    public String getDistance() {
        return distance;
    }
}
