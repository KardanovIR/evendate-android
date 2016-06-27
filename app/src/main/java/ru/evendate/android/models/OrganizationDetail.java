package ru.evendate.android.models;

import java.util.ArrayList;

import ru.evendate.android.ui.AdapterController;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface OrganizationDetail {
    String FIELDS_LIST = "description,site_url,img_medium_url,subscribed_count,is_subscribed,default_address," +
            "subscribed,events{filters:'future=true',fields:'" + EventFeed.FIELDS_LIST + "',length:" +
            AdapterController.EVENTS_LENGTH + ",order_by:'" + EventFeed.ORDER_BY_TIME + "'}";


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
