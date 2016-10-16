package ru.evendate.android.models;

import java.util.ArrayList;

import ru.evendate.android.ui.AdapterController;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface OrganizationDetail extends OrganizationSubscription {
    String FIELDS_LIST = "description,site_url,img_small_url,img_medium_url,subscribed_count,is_subscribed,default_address," +
            "background_medium_img_url," +
            "subscribed,events{filters:'future=true',fields:'" + EventFeed.FIELDS_LIST + "',length:" +
            AdapterController.EVENTS_LENGTH + ",order_by:'" + EventFeed.ORDER_BY_TIME + "'}";


    String getLogoMediumUrl();

    String getBackgroundMediumUrl();

    ArrayList<UserDetail> getSubscribedUsersList();

    String getDescription();

    String getSiteUrl();

    boolean isSubscribed();

    void setSubscriptionState();

    String getDefaultAddress();

    ArrayList<EventFeed> getEventsList();
}
