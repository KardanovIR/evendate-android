package ru.evendate.android.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Dmitry on 02.09.2015.
 */
public class EvendateContract {
    public static final String CONTENT_AUTHORITY = "ru.evendate.android";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_EVENTS = "events";
    public static final String PATH_ORGANIZATIONS = "organizations";
    public static final String PATH_TAGS = "tags";
    public static final String PATH_USERS = "users";
    public static final String PATH_DATES = "dates";

    public static final class EventEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;

        public static Uri getContentUri(int eventId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(eventId)).build();
        }
    }

    public static final class EventDateEntry implements BaseColumns {
        public static Uri getContentUri(int eventId) {
            return EventEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(eventId)).appendPath(EvendateContract.PATH_DATES).build();
        }
    }

    public static final class EventTagEntry implements BaseColumns {
        public static Uri GetContentUri(int eventId) {
            return EventEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(eventId)).appendPath(EvendateContract.PATH_TAGS).build();
        }
    }

    public static final class OrganizationEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORGANIZATIONS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/organizations";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/organization";

        public static Uri getContentUri(int organizationId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(organizationId)).build();
        }
    }

    public static final class TagEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/tags";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/tag";
    }

    public static final class UserEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/users";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/user";

        public static Uri getContentUri(int userId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(userId)).build();
        }
    }

    public static final class UserEventEntry implements BaseColumns {
        public static Uri getContentUri(int eventId) {
            return EventEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(eventId)).appendPath(EvendateContract.PATH_USERS).build();
        }
    }
}
