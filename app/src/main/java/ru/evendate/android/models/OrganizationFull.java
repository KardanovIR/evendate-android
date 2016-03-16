package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 07.02.2016.
 */
public class OrganizationFull extends Organization implements OrganizationDetail, OrganizationSubscription{
    public static final String FIELDS_LIST = "description,background_medium_img_url,background_small_img_url," +
            "img_medium_url,img_small_url,site_url,subscribed_count,is_subscribed,subscription_id,default_address" +
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
    @SerializedName("default_address")
    String defaultAddress;

    @SerializedName("subscribed")
    ArrayList<UserDetail> mSubscribedUsersList;
    @SerializedName("events")
    ArrayList<Event> mEventsList;

    public ArrayList<UserDetail> getSubscribedUsersList() {
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

    public String getDefaultAddress() {
        return defaultAddress;
    }
}
