package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ds_gordeev on 16.03.2016.
 */
public class DateFull extends Date {
    public static final String FIELDS_LIST = "start_time,end_time";


    @SerializedName("start_time")
    String startTime;
    @SerializedName("end_time")
    String endTime;

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
