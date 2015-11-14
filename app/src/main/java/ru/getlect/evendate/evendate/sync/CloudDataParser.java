package ru.getlect.evendate.evendate.sync;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventTagEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.FriendEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.TagEntry;
import ru.getlect.evendate.evendate.utils.Utils;

/**
 * Created by Dmitry on 10.09.2015.
 */
public class CloudDataParser {
    static String LOG_TAG = CloudDataParser.class.getSimpleName();

    @Deprecated
    public static ArrayList<DataEntry> getOrganizationDataFromJson(String JsonStr)
            throws JSONException, IOException {

        ArrayList<DataEntry> cVList = new ArrayList<>();

        final String STATUS = "status";
        final String DATA = "data";
        final String ORGANIZATION_ID = "id";
        final String DESCRIPTION = "description";
        final String NAME = "name";
        final String IMG_URL = "img_url";
        final String SHORT_NAME = "short_name";
        final String TYPE_NAME = "type_name";
        final String SUBSCRIBED_COUNT = "subscribed_count";
        final String SUBSCRIPTION_ID = "subscription_id";
        final String IS_SUBSCRIBED = "is_subscribed";
        final String BACKGROUND = "background_img_url";
        final String UPDATED_AT = "timestamp_updated_at";

        try {
            JSONObject organizationsJson = new JSONObject(JsonStr);
            boolean status = organizationsJson.getBoolean(STATUS);
            if(!status) throw new IOException("Response status is false");
            JSONArray organizationsArray = organizationsJson.getJSONArray(DATA);

            for(int i = 0; i < organizationsArray.length(); i++) {
                JSONObject organizationJson = organizationsArray.getJSONObject(i);
                int organization_id = organizationJson.getInt(ORGANIZATION_ID);
                String description = organizationJson.getString(DESCRIPTION);
                String name = organizationJson.getString(NAME);
                String img_url = Utils.optString(organizationJson, IMG_URL);
                String type_name = organizationJson.getString(TYPE_NAME);
                String short_name = organizationJson.getString(SHORT_NAME);
                int subscribed_count = organizationJson.getInt(SUBSCRIBED_COUNT);
                Integer subscription_id = Utils.optInt(organizationJson, SUBSCRIPTION_ID);
                boolean is_subscribed = organizationJson.getBoolean(IS_SUBSCRIBED);
                String background_img_url = Utils.optString(organizationJson, BACKGROUND);
                int timestamp_updated_at = organizationJson.getInt(UPDATED_AT);
                OrganizationEntry organizationEntry = new OrganizationEntry(
                    organization_id, name, img_url, short_name, description, type_name,
                        subscribed_count, subscription_id, is_subscribed, background_img_url,
                        timestamp_updated_at
                );
                cVList.add(organizationEntry);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        return cVList;
    }

    public static ArrayList<DataEntry> getTagsDataFromJson(String JsonStr)
            throws JSONException, IOException {

        ArrayList<DataEntry> cVList = new ArrayList<>();

        final String STATUS = "status";
        final String DATA = "data";
        final String TAGS = "tags";
        final String TAG_ID = "id";
        final String NAME = "name";

        try {
            JSONObject jsonResponse = new JSONObject(JsonStr);
            boolean status = jsonResponse.getBoolean(STATUS);
            if(!status) throw new IOException("Response status is false");
            JSONObject jsonData = jsonResponse.getJSONObject(DATA);
            JSONArray jsonDataArray = jsonData.getJSONArray(TAGS);

            for(int i = 0; i < jsonDataArray.length(); i++) {
                JSONObject jsonTag = jsonDataArray.getJSONObject(i);
                int tag_id = jsonTag.getInt(TAG_ID);
                String name = jsonTag.getString(NAME);
                TagEntry tagEntry = new TagEntry(tag_id, name);
                cVList.add(tagEntry);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        return cVList;
    }
    public static ArrayList<DataEntry> getEventsDataFromJson(String JsonStr)
            throws JSONException, IOException {

        ArrayList<DataEntry> cVList = new ArrayList<>();

        final String STATUS = "status";
        final String DATA = "data";

        final String EVENT_ID = "id";
        final String TITLE = "title";
        final String DESCRIPTION = "description";
        final String LOCATION_TEXT = "location";
        final String LOCATION_URI = "location_uri";
        final String LOCATION_JSON = "location_object";
        final String LATITUDE = "latitude";
        final String LONGITUDE = "longitude";
        final String START_DATE = "event_start_date";
        final String END_DATE = "event_end_date";
        final String NOTIFICATIONS = "notifications_schema_json";
        final String ORGANIZATION_ID = "organization_id";
        final String IMAGE_VERTICAL_URL = "image_vertical_url";
        final String IMAGE_HORIZONTAL_URL = "image_horizontal_url";
        final String DETAIL_INFO_URL = "detail_info_url";
        final String BEGIN_TIME = "begin_time";
        final String END_TIME = "end_time";
        final String CAN_EDIT = "can_edit";
        final String EVENT_TYPE = "event_type_latin_name";
        final String IS_FAVORITE = "is_favorite";
        final String UPDATED_AT = "timestamp_updated_at";

        final String FAVORITE_FRIENDS = "favorite_friends";
        final String TAGS = "tags";

        try {
            JSONObject jsonResponse = new JSONObject(JsonStr);
            boolean status = jsonResponse.getBoolean(STATUS);
            if(!status) throw new IOException("Response status is false");
            JSONArray jsonData = jsonResponse.getJSONArray(DATA);

            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonEvent = jsonData.getJSONObject(i);
                int event_id = jsonEvent.getInt(EVENT_ID);
                String title = jsonEvent.getString(TITLE);
                String description = jsonEvent.getString(DESCRIPTION);
                String location = jsonEvent.getString(LOCATION_TEXT);
                String location_uri = jsonEvent.getString(LOCATION_URI);
                String event_start_date = jsonEvent.getString(START_DATE);
                String notifications_schema_json = jsonEvent.getString(NOTIFICATIONS);
                int organization_id = jsonEvent.getInt(ORGANIZATION_ID);
                double latitude = jsonEvent.getDouble(LATITUDE);
                double longitude = jsonEvent.getDouble(LONGITUDE);
                String event_end_date = jsonEvent.getString(END_DATE);
                String detail_info_url = jsonEvent.getString(DETAIL_INFO_URL);
                String begin_time = jsonEvent.getString(BEGIN_TIME);
                String end_time = jsonEvent.getString(END_TIME);
                String location_object = jsonEvent.getString(LOCATION_JSON);
                boolean can_edit = jsonEvent.getBoolean(CAN_EDIT);
                String event_type_latin_name = jsonEvent.getString(EVENT_TYPE);
                int is_favorite = jsonEvent.getBoolean(IS_FAVORITE) ? 1 : 0;
                String image_horizontal_url = Utils.optString(jsonEvent, IMAGE_HORIZONTAL_URL);
                String image_vertical_url = Utils.optString(jsonEvent, IMAGE_VERTICAL_URL);
                int timestamp_updated_at = jsonEvent.getInt(UPDATED_AT);

                JSONArray friendList = jsonEvent.getJSONArray(FAVORITE_FRIENDS);
                JSONArray tagList = jsonEvent.getJSONArray(TAGS);

                EventEntry eventEntry = new EventEntry(event_id, title, description, location,
                        location_uri, event_start_date, notifications_schema_json, organization_id,
                        latitude, longitude, event_end_date, detail_info_url, begin_time, end_time,
                        location_object, can_edit, event_type_latin_name, is_favorite,
                        image_horizontal_url, image_vertical_url, timestamp_updated_at);
                eventEntry.setFriendList(getFriendDataFromJson(friendList));
                eventEntry.setTagList(getEventTagDataFromJson(tagList, event_id));
                cVList.add(eventEntry);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        return cVList;
    }
    private static ArrayList<DataEntry> getFriendDataFromJson(JSONArray jsonData)
            throws JSONException, IOException {

        ArrayList<DataEntry> cVList = new ArrayList<>();

        final String USER_ID = "id";
        final String LAST_NAME = "last_name";
        final String FIRST_NAME = "first_name";
        final String MIDDLE_NAME = "middle_name";
        final String AVATAR_URL = "avatar_url";
        final String FRIEND_UID = "friend_uid";
        final String TYPE = "type";
        final String LINK = "link";

        try {
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonEvent = jsonData.getJSONObject(i);
                int user_id = jsonEvent.getInt(USER_ID);
                String last_name = jsonEvent.getString(LAST_NAME);
                String first_name = jsonEvent.getString(FIRST_NAME);
                String middle_name = jsonEvent.getString(MIDDLE_NAME);
                String avatar_url = jsonEvent.getString(AVATAR_URL);
                int friend_uid = jsonEvent.getInt(FRIEND_UID);
                String type = jsonEvent.getString(TYPE);
                String link = jsonEvent.getString(LINK);

                FriendEntry friendEntry = new FriendEntry(user_id, last_name, first_name,
                        middle_name, avatar_url, type, friend_uid, link);
                cVList.add(friendEntry);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        return cVList;
    }
    private static ArrayList<DataEntry> getEventTagDataFromJson(JSONArray jsonData, int eventId)
            throws JSONException, IOException {

        ArrayList<DataEntry> cVList = new ArrayList<>();

        final String TAG_ID = "id";
        final String NAME = "name";

        try {
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonEvent = jsonData.getJSONObject(i);
                int tag_id = jsonEvent.getInt(TAG_ID);
                String name = jsonEvent.getString(NAME);

                EventTagEntry tagEntry = new EventTagEntry(eventId, tag_id, name);
                cVList.add(tagEntry);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        return cVList;
    }
}
