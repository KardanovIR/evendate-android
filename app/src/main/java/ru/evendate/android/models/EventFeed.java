package ru.evendate.android.models;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface EventFeed {
    String FIELDS_LIST = "organization_short_name," +
            "organization_short_name,is_favorite,dates,organization_logo_small_url," +
            "registration_required,registration_till,is_free,min_price,is_same_time" +
            ",created_at";
    String ORDER_BY_TIME = "created_at";

    int getEntryId();

    String getTitle();

    long getFirstDate();

    long getLastDate();

    long getNearestDate();

    String getImageHorizontalUrl();

    String getImageVerticalUrl();

    int getOrganizationId();

    ArrayList<DateFull> getDateList();

    boolean isFavorite();

    void setIsFavorite(boolean isFavorite);

    String getOrganizationShortName();

    String getOrganizationLogoSmallUrl();

    void favore();

    boolean isRegistrationRequired();

    long getRegistrationTill();

    boolean isFree();

    int getMinPrice();

    boolean isSameTime();
}
