package ru.evendate.android.sync.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 07.02.2016.
 */
public class OrganizationDetail extends OrganizationModel {
    public static final String FIELDS_LIST = "description,backgroundMediumUrl,backgroundSmallUrl," +
            "logoMediumUrl,logoSmallUrl,siteUrl,subscribedCount,isSubscribed,subscriptionId," +
            "subscribed,events";

    String description;
    @SerializedName("background_medium_img_url")
    String backgroundMediumUrl;
    @SerializedName("background_small_img_url")
    String backgroundSmallUrl;
    @SerializedName("img_medium_url")
    String logoMediumUrl;
    @SerializedName("img_small_url")
    String logoSmallUrl;
    @SerializedName("site_url")
    String siteUrl;
    @SerializedName("subscribed_count")
    int subscribedCount;
    @SerializedName("is_subscribed")
    boolean isSubscribed;
    @SerializedName("subscription_id")
    Integer subscriptionId;

    @SerializedName("subscribed")
    ArrayList<UserModel> mSubscribedUsersList;
    @SerializedName("events")
    ArrayList<EventModel> mEventsList;

    public ArrayList<UserModel> getSubscribedUsersList() {
        return mSubscribedUsersList;
    }
    public String getDescription() {
        return description;
    }
    public String getLogoMediumUrl() {
        return logoMediumUrl;
    }
    public String getLogoSmallUrl() {
        return logoSmallUrl;
    }
    public String getBackgroundMediumUrl() {
        return backgroundMediumUrl;
    }
    public String getBackgroundSmallUrl() {
        return backgroundSmallUrl;
    }
    public String getSiteUrl() {
        return siteUrl;
    }
    public int getSubscribedCount() {
        return subscribedCount;
    }
    public Integer getSubscriptionId() {
        return subscriptionId;
    }
    public boolean isSubscribed() {
        return isSubscribed;
    }
    public void setSubscriptionId(Integer subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    public void subscribe(){
        isSubscribed = !isSubscribed;
        subscribedCount += isSubscribed ? 1 : -1;
    }
}
