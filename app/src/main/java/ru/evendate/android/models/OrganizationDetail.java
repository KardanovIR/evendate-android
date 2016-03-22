package ru.evendate.android.models;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface OrganizationDetail{
    String FIELDS_LIST = "description,site_url,img_medium_url,subscribed_count,is_subscribed,default_address," +
            "subscribed,events{fields:'" + EventFeed.FIELDS_LIST + "'}";
    int getEntryId();
    String getName();
    String getShortName();
    String getLogoUrl();
    String getLogoMediumUrl();
    String getBackgroundUrl();
    ArrayList<UserDetail> getSubscribedUsersList();
    String getDescription();
    String getSiteUrl();
    int getSubscribedCount();
    Integer getSubscriptionId();
    boolean isSubscribed();
    void subscribe();
    String getDefaultAddress();
    ArrayList<EventFeed> getEventsList();
}
