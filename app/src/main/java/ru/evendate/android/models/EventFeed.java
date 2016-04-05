package ru.evendate.android.models;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface EventFeed {
    String FIELDS_LIST = "organization_short_name," +
            "organization_short_name,is_favorite,dates,organization_logo_small_url," +
            "registration_required,registration_till,is_free,min_price";

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

    String getRegistrationTill();

    boolean isFree();

    int getMinPrice();
}
