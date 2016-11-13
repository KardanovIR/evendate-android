package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 07.02.2016.
 */
public class OrganizationFull extends OrganizationModel implements OrganizationDetail {
    private String FIELDS_LIST = "";

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
    @SerializedName("new_events_count")
    int newEventsCount;

    @SerializedName("subscribed")
    ArrayList<UserDetail> mSubscribedUsersList;
    @SerializedName("events")
    ArrayList<EventDetail> mEventsList;

    @Override
    public ArrayList<UserDetail> getSubscribedUsersList() {
        return mSubscribedUsersList;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLogoMediumUrl() {
        return logoMediumUrl;
    }

    @Override
    public String getLogoSmallUrl() {
        return logoSmallUrl;
    }

    @Override
    public String getBackgroundMediumUrl() {
        return backgroundMediumUrl;
    }

    @Override
    public String getSiteUrl() {
        return siteUrl;
    }

    @Override
    public int getSubscribedCount() {
        return subscribedCount;
    }


    @Override
    public boolean isSubscribed() {
        return isSubscribed;
    }

    @Override
    public void changeSubscriptionState() {
        isSubscribed = !isSubscribed;
        subscribedCount += isSubscribed ? 1 : -1;
    }

    @Override
    public String getDefaultAddress() {
        return defaultAddress;
    }

    public ArrayList<EventFeed> getEventsList() {
        return new ArrayList<>(mEventsList);
    }

    public int getNewEventsCount() {
        return newEventsCount;
    }
}
