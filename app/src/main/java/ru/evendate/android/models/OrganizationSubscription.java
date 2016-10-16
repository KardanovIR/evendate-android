package ru.evendate.android.models;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface OrganizationSubscription extends Organization {
    String FIELDS_LIST = "img_small_url,subscribed_count";

    String getLogoSmallUrl();

    int getSubscribedCount();
}
