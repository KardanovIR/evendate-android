package ru.evendate.android.models;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface OrganizationSubscription {
    String FIELDS_LIST = "img_small_url,subscribed_count";

    int getEntryId();
    String getName();
    String getShortName();
    String getLogoSmallUrl();
    int getSubscribedCount();
}
