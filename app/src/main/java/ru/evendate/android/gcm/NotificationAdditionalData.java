package ru.evendate.android.gcm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onesignal.OSNotificationPayload;

import org.json.JSONObject;

/**
 * Created by Aedirn on 15.10.16.
 */
//{"data":{
//  "message":"Testing long text:\nСияла призрачно луна,\nМерцаньем звёзд окружена.\nОставив крепость ту, Аврора\nСкорее поспешила к морю.",
//  "event_id":1211,
//  "image_url":"http://evendate.ru/organizations_images/logos/small/1.png",
//  "organization_id":null,
//  "type":"events"
// },
// "to":"/topics/global"}
class NotificationAdditionalData {

    static final String EVENT_TYPE = "events";
    static final String ORGANIZATION_TYPE = "organizations";
    static final String USER_TYPE = "users";
    static final String RECOMMENDATION_TYPE = "recommendations_organizations";

    int eventId;
    int orgId;
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
