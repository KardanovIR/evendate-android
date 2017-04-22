package ru.evendate.android.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ds_gordeev on 11.03.2016.
 */
public interface EventFeed {
    String FIELDS_LIST = "organization_short_name," +
            "organization_short_name,is_favorite,is_hidden," +
            "dates" + DataUtil.encloseFields(EventDate.FIELDS_LIST) + "," +
            "organization_logo_small_url," +
            "registration_required,registration_till,is_free,min_price,is_same_time," +
            "created_at,actuality";
    String ORDER_BY_ACTUALITY = "-actuality";
    String ORDER_BY_FAVORITE_AND_FIRST_TIME = "-is_favorite,first_event_date";
    String ORDER_BY_LAST_DATE = "-last_event_date";

    int getEntryId();

    String getTitle();

    Date getFirstDateTime();

    Date getLastDateTime();

    @Nullable
    Date getNearestDateTime();

    String getImageHorizontalUrl();

    int getOrganizationId();

    ArrayList<EventDate> getDateList();

    boolean isFavorite();

    void setIsFavorite(boolean isFavorite);

    boolean isHidden();

    void setHidden(boolean isHidden);

    String getOrganizationShortName();

    String getOrganizationLogoSmallUrl();

    void favore();

    boolean isRegistrationRequired();

    Date getRegistrationTill();

    boolean isFree();

    int getMinPrice();

    boolean isSameTime();
}
