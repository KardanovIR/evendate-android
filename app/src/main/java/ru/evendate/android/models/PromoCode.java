package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by dmitry on 23.08.17.
 */

@Parcel
public class PromoCode extends DataModel {

    @SerializedName("uuid")
    String uuid;
    @SerializedName("code")
    String code;
    @SerializedName("is_fixed")
    boolean isFixed;
    @SerializedName("is_percentage")
    boolean isPercentage;
    float effort;
    @SerializedName("start_date")
    int startDate;
    @SerializedName("end_date")
    int endDate;

    @SerializedName("enabled")
    boolean enabled;

    @Override
    public int getEntryId() {
        return 0;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCode() {
        return code;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean isPercentage() {
        return isPercentage;
    }

    public float getEffort() {
        return effort;
    }

    public int getStartDate() {
        return startDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
