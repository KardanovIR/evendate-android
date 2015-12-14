package ru.evendate.android.data;

import android.database.sqlite.SQLiteQueryBuilder;

/**
 * Created by ds_gordeev on 23.11.2015.
 * contains projections that uses in content provider
 */
public class QueryHelper {
    public static String[] getOrganizationProjection(){
        return new String[] {
                EvendateContract.OrganizationEntry._ID,
                EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID,
                EvendateContract.OrganizationEntry.COLUMN_NAME,
                EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
                EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION,
                EvendateContract.OrganizationEntry.COLUMN_SITE_URL,
                EvendateContract.OrganizationEntry.COLUMN_LOGO_URL,
                EvendateContract.OrganizationEntry.COLUMN_BACKGROUND_URL,
                EvendateContract.OrganizationEntry.COLUMN_TYPE_ID,
                EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME,
                EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT,
                EvendateContract.OrganizationEntry.COLUMN_SUBSCRIPTION_ID,
                EvendateContract.OrganizationEntry.COLUMN_IS_SUBSCRIBED,
                EvendateContract.OrganizationEntry.COLUMN_UPDATED_AT,
                EvendateContract.OrganizationEntry.COLUMN_CREATED_AT,
        };
    }
    public static String[] getEventProjection(){
        return new String[]{
                EvendateContract.EventEntry.TABLE_NAME + "." +
                        EvendateContract.EventEntry._ID,
                EvendateContract.EventEntry.TABLE_NAME + "." +
                        EvendateContract.EventEntry.COLUMN_EVENT_ID,
                EvendateContract.EventEntry.COLUMN_TITLE,
                EvendateContract.EventEntry.TABLE_NAME + "." +
                        EvendateContract.EventEntry.COLUMN_DESCRIPTION,
                EvendateContract.EventEntry.TABLE_NAME + "." +
                        EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID,
                EvendateContract.EventEntry.COLUMN_LOCATION_TEXT,
                EvendateContract.EventEntry.COLUMN_LOCATION_URI,
                EvendateContract.EventEntry.COLUMN_LOCATION_JSON,
                EvendateContract.EventEntry.COLUMN_LATITUDE,
                EvendateContract.EventEntry.COLUMN_LONGITUDE,
                EvendateContract.EventEntry.COLUMN_IMAGE_VERTICAL_URL,
                EvendateContract.EventEntry.COLUMN_IMAGE_HORIZONTAL_URL,
                EvendateContract.EventEntry.COLUMN_IMAGE_SQUARE_URL,
                EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL,
                EvendateContract.EventEntry.COLUMN_CAN_EDIT,
                EvendateContract.EventEntry.COLUMN_IS_FAVORITE,
                EvendateContract.EventEntry.COLUMN_LIKED_USERS_COUNT,
                EvendateContract.EventEntry.COLUMN_NOTIFICATIONS,
                EvendateContract.EventEntry.COLUMN_IS_FULL_DAY,
                EvendateContract.EventEntry.COLUMN_BEGIN_TIME,
                EvendateContract.EventEntry.COLUMN_END_TIME,
                EvendateContract.EventEntry.COLUMN_FIRST_DATE,
                EvendateContract.EventEntry.COLUMN_START_DATE,
                EvendateContract.EventEntry.COLUMN_END_DATE,
                EvendateContract.EventEntry.TABLE_NAME + "." +
                        EvendateContract.EventEntry.COLUMN_UPDATED_AT,
                EvendateContract.EventEntry.TABLE_NAME + "." +
                        EvendateContract.EventEntry.COLUMN_CREATED_AT,
                EvendateContract.OrganizationEntry.TABLE_NAME + "." +
                        EvendateContract.OrganizationEntry.COLUMN_NAME,
                EvendateContract.OrganizationEntry.TABLE_NAME + "." +
                        EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
                EvendateContract.OrganizationEntry.TABLE_NAME + "." +
                        EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME,
                EvendateContract.OrganizationEntry.TABLE_NAME + "." +
                        EvendateContract.OrganizationEntry.COLUMN_LOGO_URL,
        };
    }

    public static String[] getTagProjection(){
        return new String[]{
                EvendateContract.TagEntry._ID,
                EvendateContract.TagEntry.COLUMN_TAG_ID,
                EvendateContract.TagEntry.COLUMN_NAME,
        };
    }
    public static String[] getEventTagProjection(){
        return new String[]{
                EvendateContract.TagEntry.TABLE_NAME + "." + EvendateContract.TagEntry._ID,
                EvendateContract.TagEntry.TABLE_NAME + "." + EvendateContract.TagEntry.COLUMN_TAG_ID,
                EvendateContract.EventTagEntry.TABLE_NAME + "." + EvendateContract.EventTagEntry.COLUMN_EVENT_ID,
                EvendateContract.TagEntry.TABLE_NAME + "." + EvendateContract.TagEntry.COLUMN_NAME,
        };
    }
    public static String[] getUserProjection(){
        return new String[]{
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry._ID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_USER_ID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_LAST_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_FIRST_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_MIDDLE_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_AVATAR_URL,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_FRIEND_UID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_TYPE,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_LINK
        };
    }

    public static String[] getUserEventProjection(){
        return new String[]{
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry._ID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_USER_ID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_LAST_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_FIRST_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_MIDDLE_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_AVATAR_URL,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_FRIEND_UID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_TYPE,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_LINK,
                EvendateContract.UserEventEntry.TABLE_NAME + "." + EvendateContract.UserEventEntry.COLUMN_EVENT_ID
        };
    }
    public static String[] getDateWithEventProjection(){
        return new String[]{
                EvendateContract.EventDateEntry.TABLE_NAME + "." + EvendateContract.EventDateEntry.COLUMN_DATE,
                EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry.COLUMN_IS_FAVORITE,
        };
    }

    public static SQLiteQueryBuilder buildEventQuery(){
        final SQLiteQueryBuilder sOrganizationWithEventQueryBuilder
                = new SQLiteQueryBuilder();
        sOrganizationWithEventQueryBuilder.setTables(EvendateContract.EventEntry.TABLE_NAME
                + " INNER JOIN " + EvendateContract.OrganizationEntry.TABLE_NAME +
                " ON " + EvendateContract.OrganizationEntry.TABLE_NAME +
                "." + EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID +
                " = " + EvendateContract.EventEntry.TABLE_NAME +
                "." + EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID);
        return sOrganizationWithEventQueryBuilder;
    }
    public static SQLiteQueryBuilder buildEventWithDateQuery(){
        final SQLiteQueryBuilder sOrganizationWithEventQueryBuilder
                = new SQLiteQueryBuilder();
        sOrganizationWithEventQueryBuilder.setDistinct(true);
        sOrganizationWithEventQueryBuilder.setTables(EvendateContract.EventEntry.TABLE_NAME
                + " INNER JOIN " + EvendateContract.OrganizationEntry.TABLE_NAME +
                " ON " + EvendateContract.OrganizationEntry.TABLE_NAME +
                "." + EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID +
                " = " + EvendateContract.EventEntry.TABLE_NAME +
                "." + EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID
                + " LEFT JOIN " + EvendateContract.EventDateEntry.TABLE_NAME +
                " ON " + EvendateContract.EventDateEntry.TABLE_NAME +
                "." + EvendateContract.EventDateEntry.COLUMN_EVENT_ID +
                " = " + EvendateContract.EventEntry.TABLE_NAME +
                "." + EvendateContract.EventEntry.COLUMN_EVENT_ID);
        sOrganizationWithEventQueryBuilder.setDistinct(true);
        return sOrganizationWithEventQueryBuilder;
    }

    public static SQLiteQueryBuilder buildEventTagQuery(){
        final SQLiteQueryBuilder sTagsByEventsQueryBuilder
                = new SQLiteQueryBuilder();
        sTagsByEventsQueryBuilder.setTables(
                EvendateContract.EventEntry.TABLE_NAME + " INNER JOIN " +
                        EvendateContract.EventTagEntry.TABLE_NAME +
                        " ON " + EvendateContract.EventEntry.TABLE_NAME +
                        "." + EvendateContract.EventEntry.COLUMN_EVENT_ID +
                        " = " + EvendateContract.EventTagEntry.TABLE_NAME +
                        "." + EvendateContract.EventTagEntry.COLUMN_EVENT_ID +
                        " INNER JOIN " +
                        EvendateContract.TagEntry.TABLE_NAME +
                        " ON " + EvendateContract.TagEntry.TABLE_NAME +
                        "." + EvendateContract.TagEntry.COLUMN_TAG_ID +
                        " = " + EvendateContract.EventTagEntry.TABLE_NAME +
                        "." + EvendateContract.EventTagEntry.COLUMN_TAG_ID);
        return sTagsByEventsQueryBuilder;
    }
    public static SQLiteQueryBuilder buildEventFriendQuery(){

        final SQLiteQueryBuilder sFriendsByEventsQueryBuilder
                = new SQLiteQueryBuilder();
        sFriendsByEventsQueryBuilder.setTables(
                EvendateContract.EventEntry.TABLE_NAME + " INNER JOIN " +
                        EvendateContract.UserEventEntry.TABLE_NAME +
                        " ON " + EvendateContract.EventEntry.TABLE_NAME +
                        "." + EvendateContract.EventEntry.COLUMN_EVENT_ID +
                        " = " + EvendateContract.UserEventEntry.TABLE_NAME +
                        "." + EvendateContract.UserEventEntry.COLUMN_EVENT_ID +
                        " INNER JOIN " +
                        EvendateContract.UserEntry.TABLE_NAME +
                        " ON " + EvendateContract.UserEntry.TABLE_NAME +
                        "." + EvendateContract.UserEntry.COLUMN_USER_ID +
                        " = " + EvendateContract.UserEventEntry.TABLE_NAME +
                        "." + EvendateContract.UserEventEntry.COLUMN_USER_ID
        );
        return sFriendsByEventsQueryBuilder;
    }
    public static SQLiteQueryBuilder buildEventDatesQuery(){

        final SQLiteQueryBuilder sDatesByEventsQueryBuilder
                = new SQLiteQueryBuilder();
        sDatesByEventsQueryBuilder.setTables(
                EvendateContract.EventEntry.TABLE_NAME + " INNER JOIN " +
                        EvendateContract.EventDateEntry.TABLE_NAME +
                        " ON " + EvendateContract.EventDateEntry.TABLE_NAME +
                        "." + EvendateContract.EventDateEntry.COLUMN_EVENT_ID +
                        " = " + EvendateContract.EventEntry.TABLE_NAME +
                        "." + EvendateContract.EventEntry.COLUMN_EVENT_ID
        );
        return sDatesByEventsQueryBuilder;
    }

    public static SQLiteQueryBuilder buildDateWithEventQuery(){
        final SQLiteQueryBuilder sDateWithEventQueryBuilder
                = new SQLiteQueryBuilder();
        sDateWithEventQueryBuilder.setTables(EvendateContract.EventDateEntry.TABLE_NAME
                + " INNER JOIN " + EvendateContract.EventEntry.TABLE_NAME +
                " ON " + EvendateContract.EventDateEntry.TABLE_NAME +
                "." + EvendateContract.EventDateEntry.COLUMN_EVENT_ID +
                " = " + EvendateContract.EventEntry.TABLE_NAME +
                "." + EvendateContract.EventEntry.COLUMN_EVENT_ID);
        return sDateWithEventQueryBuilder;
    }
}
