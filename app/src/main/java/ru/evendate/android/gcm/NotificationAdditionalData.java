package ru.evendate.android.gcm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.onesignal.OSNotificationPayload;

import org.json.JSONObject;

/**
 * Created by Aedirn on 15.10.16.
 */
class NotificationAdditionalData {

    static final String EVENT_TYPE = "events";
    static final String ORGANIZATION_TYPE = "organizations";
    static final String USER_TYPE = "users";
    static final String RECOMMENDATION_TYPE = "recommendations_organizations";

    @SerializedName("event_id")
    int eventId;
    @SerializedName("organization_id")
    int orgId;
    @SerializedName("user_id")
    int userId;
    String type;

    static NotificationAdditionalData obtainFromPayload(OSNotificationPayload payload) {
        JSONObject data = payload.additionalData;
        if (data == null)
            return null;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(data.toString(), NotificationAdditionalData.class);
    }
}
