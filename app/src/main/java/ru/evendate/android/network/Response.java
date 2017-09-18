package ru.evendate.android.network;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Dmitry on 18.10.2015.
 */
@Parcel
@SuppressWarnings("WeakerAccess")
public class Response {
    boolean status;
    String text;

    @SerializedName("response_id")
    String responseId;
    @SerializedName("request_time")
    String requestTime;

    public boolean isOk() {
        return status;
    }

    public String getText() {
        return text;
    }

    public String getResponseId() {
        return responseId;
    }

    public String getRequestTime() {
        return requestTime;
    }
}
