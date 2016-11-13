package ru.evendate.android.models;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface OrganizationDetail extends OrganizationSubscription {
    String FIELDS_LIST = "description,site_url,img_small_url,img_medium_url,subscribed_count,is_subscribed,default_address," +
            "background_medium_img_url,subscribed";


    String getLogoMediumUrl();

    String getBackgroundMediumUrl();

    ArrayList<UserDetail> getSubscribedUsersList();

    String getDescription();

    String getSiteUrl();

    boolean isSubscribed();

    void changeSubscriptionState();

    String getDefaultAddress();
}
