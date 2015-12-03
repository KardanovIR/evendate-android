package ru.getlect.evendate.evendate.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Dmitry on 03.09.2015.
 */
public class EvendateProvider extends ContentProvider {
    private static final String LOG_TAG = ContentProvider.class.getSimpleName();

    private static final int ORGANIZATIONS = 100;
    private static final int ORGANIZATION_ID = 101;
    private static final int TAGS = 200;
    private static final int TAG_ID = 201;
    private static final int EVENTS = 301;
    private static final int EVENT_ID = 302;
    private static final int EVENT_TAGS = 303;
    private static final int EVENT_FRIENDS = 305;
    private static final int EVENT_DATES = 306;
    private static final int USERS = 400;
    private static final int USER_ID = 401;
    private static final int EVENT_IMAGE = 501;
    private static final int ORGANIZATION_IMAGE = 502;
    private static final int ORGANIZATION_LOGO = 503;
    private static final int USER_AVATAR = 504;
    private static final int DATES = 601;
    private static final int DATES_WITH_PARAMS = 602;

    private EvendateDBHelper mEvendateDBHelper;
    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate(){
        mEvendateDBHelper = new EvendateDBHelper(getContext());
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_ORGANIZATIONS + "/#", ORGANIZATION_ID);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_ORGANIZATIONS, ORGANIZATIONS);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_TAGS, TAGS);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_TAGS + "/#", TAG_ID);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_EVENTS, EVENTS);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_EVENTS + "/#", EVENT_ID);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_EVENTS + "/#/" + EvendateContract.PATH_TAGS, EVENT_TAGS);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_EVENTS + "/#/" + EvendateContract.PATH_USERS, EVENT_FRIENDS);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_EVENTS + "/#/" + EvendateContract.PATH_DATES, EVENT_DATES);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_USERS, USERS);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_USERS + "/#", USER_ID);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_EVENT_IMAGES + "/#", EVENT_IMAGE);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_ORGANIZATION_IMAGES + "/#", ORGANIZATION_IMAGE);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_ORGANIZATION_LOGOS + "/#", ORGANIZATION_LOGO);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                "images/user", USER_AVATAR);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_DATES, DATES);
        return true;
    }

    @Override
    public String getType(final Uri uri) {

        switch (mUriMatcher.match(uri)) {
            case ORGANIZATIONS:
                return EvendateContract.OrganizationEntry.CONTENT_TYPE;
            case ORGANIZATION_ID:
                return EvendateContract.OrganizationEntry.CONTENT_ITEM_TYPE;
            case TAGS:
                return EvendateContract.TagEntry.CONTENT_TYPE;
            case TAG_ID:
                return EvendateContract.TagEntry.CONTENT_ITEM_TYPE;
            case EVENTS:
                return EvendateContract.EventEntry.CONTENT_TYPE;
            case EVENT_ID:
                return EvendateContract.EventEntry.CONTENT_ITEM_TYPE;
            case USERS:
                return EvendateContract.UserEntry.CONTENT_TYPE;
            case USER_ID:
                return EvendateContract.UserEntry.CONTENT_ITEM_TYPE;
            case EVENT_TAGS:
                return EvendateContract.TagEntry.CONTENT_TYPE;
            case EVENT_FRIENDS:
                return EvendateContract.UserEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection,
                        final String[] selectionArgs, final String sortOrder){
        switch(mUriMatcher.match(uri)){
            case ORGANIZATIONS: {
                String categories = uri.getQueryParameter("categories");
                Cursor cursor;
                if(categories == null || !categories.equals("true")){
                    cursor = mEvendateDBHelper.getReadableDatabase().query(
                            EvendateContract.OrganizationEntry.TABLE_NAME,
                            QueryHelper.getOrganizationProjection(),
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                }else{
                    final SQLiteQueryBuilder sOrganizationCategoriesBuilder
                            = new SQLiteQueryBuilder();
                    sOrganizationCategoriesBuilder.setDistinct(true);
                    sOrganizationCategoriesBuilder.setTables(EvendateContract.OrganizationEntry.TABLE_NAME);
                    cursor = sOrganizationCategoriesBuilder.query(
                            mEvendateDBHelper.getReadableDatabase(),
                            new String[]{EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME},
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                    );
                }

                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.OrganizationEntry.CONTENT_URI);
                return cursor;
            }
            case TAGS: {
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.TagEntry.TABLE_NAME,
                        QueryHelper.getTagProjection(),
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.TagEntry.CONTENT_URI);
                return cursor;
            }
            case EVENTS: {
                final Cursor cursor = QueryHelper.buildEventWithDateQuery().query(
                        mEvendateDBHelper.getReadableDatabase(),
                        QueryHelper.getEventProjection(),
                        selection,
                        selectionArgs,
                        null,
                        null,
                        EvendateContract.EventDateEntry.COLUMN_DATE + " ASC"
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.EventEntry.CONTENT_URI);
                return cursor;
            }
            case USERS: {
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.UserEntry.TABLE_NAME,
                        QueryHelper.getUserProjection(),
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.UserEntry.CONTENT_URI);
                return cursor;
            }
            case DATES: {

                String with_favorite = uri.getQueryParameter("with_favorite");
                if(with_favorite != null && !with_favorite.equals("1")){
                    return mEvendateDBHelper.getReadableDatabase().query(
                            true,
                            EvendateContract.EventDateEntry.TABLE_NAME,
                            null,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder,
                            null
                    );
                }
                else{
                    return QueryHelper.buildDateWithEventQuery().query(
                            mEvendateDBHelper.getReadableDatabase(),
                            QueryHelper.getDateWithEventProjection(),
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                }
            }
            case EVENT_ID: {
                String[] args = {uri.getLastPathSegment()};
                final Cursor cursor = QueryHelper.buildEventQuery().query(
                        mEvendateDBHelper.getReadableDatabase(),
                        QueryHelper.getEventProjection(),
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry.COLUMN_EVENT_ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.EventEntry.CONTENT_URI);
                return cursor;
            }
            case USER_ID: {
                String[] args = {uri.getLastPathSegment()};
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.UserEntry.TABLE_NAME,
                        projection,
                        EvendateContract.UserEntry.COLUMN_USER_ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.UserEntry.CONTENT_URI);
                return cursor;
            }
            case TAG_ID: {
                String[] args = {uri.getLastPathSegment()};
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.TagEntry.TABLE_NAME,
                        projection,
                        EvendateContract.TagEntry.COLUMN_TAG_ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.TagEntry.CONTENT_URI);
                return cursor;
            }
            case ORGANIZATION_ID: {
                String[] args = {uri.getLastPathSegment()};
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.OrganizationEntry.TABLE_NAME,
                        projection,
                        EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.OrganizationEntry.CONTENT_URI);
                return cursor;
            }
            case EVENT_TAGS: {
                //events/1/tags
                String[] args = {uri.getPathSegments().get(1)};
                final Cursor cursor = QueryHelper.buildEventTagQuery().query(
                        mEvendateDBHelper.getReadableDatabase(),
                        QueryHelper.getEventTagProjection(),
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry.COLUMN_EVENT_ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.EventEntry.CONTENT_URI);
                return cursor;
            }
            case EVENT_FRIENDS: {
                //events/1/friends
                String[] args = {uri.getPathSegments().get(1)};
                final Cursor cursor = QueryHelper.buildEventFriendQuery().query(
                        mEvendateDBHelper.getReadableDatabase(),
                        QueryHelper.getUserEventProjection(),
                        EvendateContract.UserEventEntry.TABLE_NAME + "." + EvendateContract.UserEventEntry.COLUMN_EVENT_ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.UserEventEntry.getContentUri(Integer.parseInt(args[0])));
                return cursor;
            }
            case EVENT_DATES: {
                //events/1/dates
                String[] args = {uri.getPathSegments().get(1)};
                String selectionDate;
                if(selection != null){
                    selectionDate = selection + "AND " + EvendateContract.EventDateEntry.COLUMN_EVENT_ID + "=?";
                }
                else
                    selectionDate = EvendateContract.EventDateEntry.COLUMN_EVENT_ID + "=?";
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.EventDateEntry.TABLE_NAME,
                        projection,
                        selectionDate,
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.EventDateEntry.getContentUri(Integer.parseInt(args[0])));
                return cursor;
            }
            //case EVENT_WITH_PARAMS: {
            //    //events/1/dates
            //    //params since till future
            //    String since = uri.getQueryParameter("since");
            //    String till = uri.getQueryParameter("till");
            //    String future = uri.getQueryParameter("future");
            //    String[] args = {uri.getPathSegments().get(1)};
            //    final Cursor cursor = QueryHelper.buildEventWithDateQuery().query(
            //            mEvendateDBHelper.getReadableDatabase(),
            //            QueryHelper.getEventProjection(),
            //            EvendateContract.EventDateEntry.COLUMN_EVENT_ID + "=?" +
            //                    (since != null ? " AND " + EvendateContract.EventDateEntry.COLUMN_DATE + ">?" : null) +
            //                    (till != null ? " AND " + EvendateContract.EventDateEntry.COLUMN_DATE + "<?" : null) +
            //                    (future != null ? " AND " + EvendateContract.EventDateEntry.COLUMN_DATE + "> date('now')" : null),
            //            new String[]{
            //                    (since != null ? since : null),
            //                    (till != null ? till : null),
            //                    (future != null ? future : null)
            //            },
            //            null,
            //            null,
            //            sortOrder
            //    );
            //    cursor.setNotificationUri(getContext().getContentResolver(),
            //            EvendateContract.EventDateEntry.getContentUri(Integer.parseInt(args[0])));
            //    return cursor;
            //}
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        final SQLiteDatabase db = mEvendateDBHelper.getWritableDatabase();
        Uri returnUri;
        switch (mUriMatcher.match(uri)){
            case ORGANIZATIONS: {
                long id = db.insert(EvendateContract.OrganizationEntry.TABLE_NAME, null, values);
                if( id > 0 )
                    returnUri = ContentUris.
                            withAppendedId(EvendateContract.OrganizationEntry.CONTENT_URI, id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TAGS: {
                long id = db.insert(EvendateContract.TagEntry.TABLE_NAME, null, values);
                if( id > 0 )
                    returnUri = ContentUris.withAppendedId(EvendateContract.TagEntry.CONTENT_URI, id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case EVENTS: {
                long id = db.insert(EvendateContract.EventEntry.TABLE_NAME, null, values);
                if( id > 0 )
                    returnUri = ContentUris.withAppendedId(EvendateContract.EventEntry.CONTENT_URI, id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case USERS: {
                long id = db.insert(EvendateContract.UserEntry.TABLE_NAME, null, values);
                if( id > 0 )
                    returnUri = ContentUris.withAppendedId(EvendateContract.UserEntry.CONTENT_URI, id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case EVENT_DATES: {
                String eventId = uri.getPathSegments().get(1);
                values.put(EvendateContract.EventDateEntry.COLUMN_EVENT_ID, eventId);
                long id = db.insert(EvendateContract.EventDateEntry.TABLE_NAME, null, values);
                if( id > 0 )
                    returnUri = Uri.parse(EvendateContract.PATH_EVENTS + "/" + eventId + "/" + EvendateContract.PATH_DATES);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case EVENT_TAGS: {
                //events/1/tags?addTag=tagId1,tagId2
                String eventId = uri.getPathSegments().get(1);
                String tags = uri.getQueryParameter(EvendateContract.EventTagEntry.QUERY_ADD_PARAMETER_NAME);
                if(tags == null)
                    throw new IllegalArgumentException("no parameter " + EvendateContract.EventTagEntry.QUERY_ADD_PARAMETER_NAME);

                for (String tagId: tags.split(",")) {
                    ContentValues contentValues = new ContentValues(2);
                    contentValues.put(EvendateContract.EventTagEntry.COLUMN_EVENT_ID, eventId);
                    contentValues.put(EvendateContract.EventTagEntry.COLUMN_TAG_ID, tagId);
                    long id =  db.insert(EvendateContract.EventTagEntry.TABLE_NAME, null, contentValues);
                    if( id <= 0 )
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                returnUri = Uri.parse(EvendateContract.PATH_EVENTS + "/" + eventId + "/" + EvendateContract.PATH_TAGS);
                break;
            }
            case EVENT_FRIENDS: {
                //events/1/tags?addFriends=friendId1,friendId2
                String eventId = uri.getPathSegments().get(1);
                String friends = uri.getQueryParameter(EvendateContract.UserEventEntry.QUERY_ADD_PARAMETER_NAME);
                if(friends == null)
                    throw new IllegalArgumentException("no parameter " + EvendateContract.UserEventEntry.QUERY_ADD_PARAMETER_NAME);

                for (String friendId: friends.split(",")) {
                    ContentValues contentValues = new ContentValues(2);
                    contentValues.put(EvendateContract.UserEventEntry.COLUMN_EVENT_ID, eventId);
                    contentValues.put(EvendateContract.UserEventEntry.COLUMN_USER_ID, friendId);
                    long id =  db.insert(EvendateContract.UserEventEntry.TABLE_NAME, null, contentValues);
                    if( id <= 0 )
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                returnUri = Uri.parse(EvendateContract.PATH_EVENTS + "/" + eventId + "/" + EvendateContract.PATH_TAGS);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }
    @Override

    public int update(final Uri uri, final ContentValues values, final String selection,
                      final String[] selectionArgs){
        final SQLiteDatabase db = mEvendateDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int rowsUpdated;
        String[] args = {uri.getLastPathSegment()};
        switch (match) {
            case ORGANIZATION_ID:
                rowsUpdated = db.update(
                        EvendateContract.OrganizationEntry.TABLE_NAME, values,
                        EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID + "=?", args);
                break;
            case TAG_ID:
                rowsUpdated = db.update(
                        EvendateContract.TagEntry.TABLE_NAME, values,
                        EvendateContract.TagEntry.COLUMN_TAG_ID + "=?", args);
                break;
            case EVENT_ID:
                rowsUpdated = db.update(
                        EvendateContract.EventEntry.TABLE_NAME, values,
                        EvendateContract.EventEntry.COLUMN_EVENT_ID + "=?", args);
                break;
            case USER_ID:
                rowsUpdated = db.update(
                        EvendateContract.UserEntry.TABLE_NAME, values,
                        EvendateContract.UserEntry.COLUMN_USER_ID + "=?", args);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase db = mEvendateDBHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        int rowsDeleted;
        String[] args = {uri.getLastPathSegment()};
        //Log.i(LOG_TAG, "in delete selection " + selection);
        //for(String s : selectionArgs)
        //    Log.i(LOG_TAG, "in delete selectionArgs " + s);
        // this makes delete all rows return the number of rows deleted
        switch (match) {
            case ORGANIZATION_ID:
                rowsDeleted = db.delete(
                        EvendateContract.OrganizationEntry.TABLE_NAME,
                        EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID + "=?", args);
                break;
            case TAG_ID:
                rowsDeleted = db.delete(
                        EvendateContract.TagEntry.TABLE_NAME,
                        EvendateContract.TagEntry.COLUMN_TAG_ID + "=?", args);
                break;
            case EVENT_ID:
                rowsDeleted = db.delete(
                        EvendateContract.EventEntry.TABLE_NAME,
                        EvendateContract.EventEntry.COLUMN_EVENT_ID + "=?", args);
                break;
            case USER_ID:
                rowsDeleted = db.delete(
                        EvendateContract.UserEntry.TABLE_NAME,
                        EvendateContract.UserEntry.COLUMN_USER_ID + "=?", args);
                break;
            case EVENT_DATES:{
                //events/1/dates?date=bla_bla
                String event_id = uri.getPathSegments().get(1);
                String date = uri.getQueryParameter("date");
                if(date == null)
                    throw new IllegalArgumentException("no parameter date");
                String[] eventsDatesArgs = {event_id, date};
                rowsDeleted = db.delete(
                        EvendateContract.EventDateEntry.TABLE_NAME,
                        EvendateContract.EventDateEntry.COLUMN_EVENT_ID + "=?" + " AND " +
                                EvendateContract.EventDateEntry.COLUMN_DATE + "=?",
                        eventsDatesArgs
                );
                break;
            }
            case EVENT_TAGS:{
                //events/1/tags?removeTags=tagId1,tagId2
                String event_id = uri.getPathSegments().get(1);
                String tags = uri.getQueryParameter(EvendateContract.EventTagEntry.QUERY_REMOVE_PARAMETER_NAME);
                if(tags == null)
                    throw new IllegalArgumentException("no parameter " + EvendateContract.EventTagEntry.QUERY_REMOVE_PARAMETER_NAME);

                rowsDeleted = 0;
                for (String tagId: tags.split(",")) {
                    String[] eventsTagsArgs = {event_id, tagId};
                    rowsDeleted += db.delete(
                            EvendateContract.EventTagEntry.TABLE_NAME,
                            EvendateContract.EventTagEntry.COLUMN_EVENT_ID + "=?" + " AND " +
                            EvendateContract.EventTagEntry.COLUMN_TAG_ID + "=?", eventsTagsArgs
                    );
                }
                break;
            }
            case EVENT_FRIENDS:{
                //events/1/tags?removeFriends=tagId1,tagId2
                String event_id = uri.getPathSegments().get(1);
                String friends = uri.getQueryParameter(EvendateContract.UserEventEntry.QUERY_REMOVE_PARAMETER_NAME);
                if(friends == null)
                    throw new IllegalArgumentException("no parameter " + EvendateContract.EventTagEntry.QUERY_REMOVE_PARAMETER_NAME);

                rowsDeleted = 0;
                for (String friendId: friends.split(",")) {
                    String[] eventsTagsArgs = {event_id, friendId};
                    rowsDeleted += db.delete(
                            EvendateContract.UserEventEntry.TABLE_NAME,
                            EvendateContract.UserEventEntry.COLUMN_EVENT_ID + "=?" + " AND " +
                            EvendateContract.UserEventEntry.COLUMN_USER_ID + "=?", eventsTagsArgs
                    );
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }



    @Override
    public ParcelFileDescriptor openFile (final Uri uri, final String mode) throws FileNotFoundException {
        Log.d(LOG_TAG, "openFile: " + uri);

        final int match = mUriMatcher.match(uri);

        final String BASE_PATH = getContext().getExternalCacheDir().toString();

        //TODO EXTENSION!!!
        switch (match) {
            //TODO песец
            case EVENT_IMAGE: {
                String event_id = uri.getPathSegments().get(2);
                File file = new File(BASE_PATH, EvendateContract.PATH_EVENT_IMAGES + "/" + event_id + ".jpg");
                if(!file.exists()){
                    file = new File(BASE_PATH, EvendateContract.PATH_EVENT_IMAGES + "/" + event_id + ".png");
                    if(!file.exists())
                        return null;
                }
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            }
            case USER_AVATAR: {
                File file = new File(BASE_PATH, "images/user.jpg");
                if(!file.exists()){
                    file = new File(BASE_PATH, "images/user.png");
                    if(!file.exists())
                        return null;
                }
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            }
            //TODO песец
            case ORGANIZATION_IMAGE: {
                String organization_id = uri.getPathSegments().get(2);
                File file = new File(BASE_PATH, EvendateContract.PATH_ORGANIZATION_IMAGES + "/" + organization_id + ".jpg");
                if(!file.exists()){
                    file = new File(BASE_PATH, EvendateContract.PATH_ORGANIZATION_IMAGES + "/" + organization_id + ".png");
                    if(!file.exists())
                        return null;
                }
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            }
            case ORGANIZATION_LOGO: {
                String organization_id = uri.getPathSegments().get(3);
                File file = new File(BASE_PATH, EvendateContract.PATH_ORGANIZATION_LOGOS + "/" + organization_id + ".png");
                if(!file.exists())
                    return null;
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            }
        }
        return null;
    }
}
