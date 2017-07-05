package ru.evendate.android.models;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface OrganizationSubscription extends Organization {
    String FIELDS_LIST = "img_small_url,subscribed_count,is_subscribed,new_events_count";
    String SEARCH_ORDER_BY = "-search_score";

    String getLogoSmallUrl();

    int getSubscribedCount();

    boolean isSubscribed();

    int getNewEventsCount();
}
