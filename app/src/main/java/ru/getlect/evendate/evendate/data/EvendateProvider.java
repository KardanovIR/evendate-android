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
    private static final int USERS = 400;
    private static final int USER_ID = 401;
    private static final int EVENT_IMAGE = 501;
    private static final int ORGANIZATION_IMAGE = 502;
    private static final int ORGANIZATION_LOGO = 503;

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
                EvendateContract.PATH_USERS, USERS);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_USERS + "/#", USER_ID);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_EVENT_IMAGES + "/#", EVENT_IMAGE);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_ORGANIZATION_IMAGES + "/#", ORGANIZATION_IMAGE);
        mUriMatcher.addURI(EvendateContract.CONTENT_AUTHORITY,
                EvendateContract.PATH_ORGANIZATION_LOGOS + "/#", ORGANIZATION_LOGO);
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
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.OrganizationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.OrganizationEntry.CONTENT_URI);
                return cursor;
            }
            case TAGS: {
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.TagEntry.TABLE_NAME,
                        projection,
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
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.EventEntry.CONTENT_URI);
                return cursor;
            }
            case USERS: {
                final Cursor cursor = mEvendateDBHelper.getReadableDatabase().query(
                        EvendateContract.UserEntry.TABLE_NAME,
                        projection,
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
            case EVENT_ID: {
                //TODo нужно нормальное решение!
                String[] args = {uri.getLastPathSegment()};
                final SQLiteQueryBuilder sOrganizationWithEventQueryBuilder
                        = new SQLiteQueryBuilder();
                sOrganizationWithEventQueryBuilder.setTables(EvendateContract.EventEntry.TABLE_NAME
                        + " INNER JOIN " + EvendateContract.OrganizationEntry.TABLE_NAME +
                        " ON " + EvendateContract.OrganizationEntry.TABLE_NAME +
                        "." + EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID +
                        " = " + EvendateContract.EventEntry.TABLE_NAME +
                        "." + EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID);
                final Cursor cursor = sOrganizationWithEventQueryBuilder.query(
                        mEvendateDBHelper.getReadableDatabase(),
                        projection,
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry._ID + "=?",
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
                        EvendateContract.UserEntry._ID + "=?",
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
                        EvendateContract.TagEntry._ID + "=?",
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
                        EvendateContract.OrganizationEntry._ID + "=?",
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

                final SQLiteQueryBuilder sTagsByEventsQueryBuilder
                        = new SQLiteQueryBuilder();

                //This is an inner join which looks like
                //weather INNER JOIN location ON weather.location_id = location._id
                sTagsByEventsQueryBuilder.setTables(
                        EvendateContract.EventEntry.TABLE_NAME + " INNER JOIN " +
                                EvendateContract.EventTagEntry.TABLE_NAME +
                                " ON " + EvendateContract.EventEntry.TABLE_NAME +
                                "." + EvendateContract.EventEntry._ID +
                                " = " + EvendateContract.EventTagEntry.TABLE_NAME +
                                "." + EvendateContract.EventTagEntry.COLUMN_EVENT_ID +
                                " INNER JOIN " +
                                EvendateContract.TagEntry.TABLE_NAME +
                                " ON " + EvendateContract.TagEntry.TABLE_NAME +
                                "." + EvendateContract.TagEntry._ID +
                                " = " + EvendateContract.EventTagEntry.TABLE_NAME +
                                "." + EvendateContract.EventTagEntry.COLUMN_TAG_ID
                );

                //events/1/tags
                String[] args = {uri.getPathSegments().get(1)};
                final Cursor cursor = sTagsByEventsQueryBuilder.query(
                        mEvendateDBHelper.getReadableDatabase(),
                        projection,
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry._ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.EventTagEntry.CONTENT_URI);
                return cursor;
            }
            case EVENT_FRIENDS: {

                final SQLiteQueryBuilder sFriendsByEventsQueryBuilder
                        = new SQLiteQueryBuilder();

                //This is an inner join which looks like
                //weather INNER JOIN location ON weather.location_id = location._id
                sFriendsByEventsQueryBuilder.setTables(
                        EvendateContract.EventEntry.TABLE_NAME + " INNER JOIN " +
                                EvendateContract.UserEventEntry.TABLE_NAME +
                                " ON " + EvendateContract.EventEntry.TABLE_NAME +
                                "." + EvendateContract.EventEntry._ID +
                                " = " + EvendateContract.UserEventEntry.TABLE_NAME +
                                "." + EvendateContract.UserEventEntry.COLUMN_EVENT_ID +
                                " INNER JOIN " +
                                EvendateContract.UserEntry.TABLE_NAME +
                                " ON " + EvendateContract.UserEntry.TABLE_NAME +
                                "." + EvendateContract.UserEntry._ID +
                                " = " + EvendateContract.UserEventEntry.TABLE_NAME +
                                "." + EvendateContract.UserEventEntry.COLUMN_USER_ID
                );

                //events/1/friends
                String[] args = {uri.getPathSegments().get(1)};
                final Cursor cursor = sFriendsByEventsQueryBuilder.query(
                        mEvendateDBHelper.getReadableDatabase(),
                        projection,
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry._ID + "=?",
                        args,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(),
                        EvendateContract.EventTagEntry.CONTENT_URI);
                return cursor;
            }
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
            case EVENT_TAGS: {
                //events/1/tags?addTag=tagId1,tagId2
                String event_id = uri.getPathSegments().get(1);
                String tags = uri.getQueryParameter("addTags");
                for (String tagId: tags.split(",")) {
                    ContentValues contentValues = new ContentValues(2);
                    contentValues.put("event_id", event_id);
                    contentValues.put("tag_id", tagId);
                    long id =  db.insert(EvendateContract.EventTagEntry.TABLE_NAME, null, contentValues);
                    if( id <= 0 )
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                returnUri = Uri.parse(EvendateContract.PATH_EVENTS + "/" + event_id + "/" + EvendateContract.PATH_TAGS);
                break;
            }
            case EVENT_FRIENDS: {
                //events/1/tags?addTag=tagId1,tagId2
                String event_id = uri.getPathSegments().get(1);
                String friends = uri.getQueryParameter("addFriends");
                for (String friendId: friends.split(",")) {
                    ContentValues contentValues = new ContentValues(2);
                    contentValues.put("event_id", event_id);
                    contentValues.put("user_id", friendId);
                    long id =  db.insert(EvendateContract.UserEventEntry.TABLE_NAME, null, contentValues);
                    if( id <= 0 )
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                returnUri = Uri.parse(EvendateContract.PATH_EVENTS + "/" + event_id + "/" + EvendateContract.PATH_TAGS);
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
                        EvendateContract.OrganizationEntry._ID + "=?", args);
                break;
            case TAG_ID:
                rowsUpdated = db.update(
                        EvendateContract.TagEntry.TABLE_NAME, values,
                        EvendateContract.TagEntry._ID + "=?", args);
                break;
            case EVENT_ID:
                rowsUpdated = db.update(
                        EvendateContract.EventEntry.TABLE_NAME, values,
                        EvendateContract.EventEntry._ID + "=?", args);
                break;
            case USER_ID:
                rowsUpdated = db.update(
                        EvendateContract.UserEntry.TABLE_NAME, values,
                        EvendateContract.UserEntry._ID + "=?", args);
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
                        EvendateContract.OrganizationEntry._ID + "=?", args);
                break;
            case TAG_ID:
                rowsDeleted = db.delete(
                        EvendateContract.TagEntry.TABLE_NAME,
                        EvendateContract.TagEntry._ID + "=?", args);
                break;
            case EVENT_ID:
                rowsDeleted = db.delete(
                        EvendateContract.EventEntry.TABLE_NAME,
                        EvendateContract.EventEntry._ID + "=?", args);
                break;
            case USER_ID:
                rowsDeleted = db.delete(
                        EvendateContract.UserEntry.TABLE_NAME,
                        EvendateContract.UserEntry._ID + "=?", args);
                break;
            case EVENT_TAGS:{
                //events/1/tags?removeTags=tagId1,tagId2
                String event_id = uri.getPathSegments().get(1);
                String tags = uri.getQueryParameter("removeTags");
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
                String friends = uri.getQueryParameter("removeFriends");
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
            case EVENT_IMAGE: {
                String event_id = uri.getPathSegments().get(2);
                File file = new File(BASE_PATH, EvendateContract.PATH_EVENT_IMAGES + "/" + event_id + ".jpg");
                if(!file.exists())
                    return null;
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            }
            case ORGANIZATION_IMAGE: {
                String organization_id = uri.getPathSegments().get(2);
                File file = new File(BASE_PATH, EvendateContract.PATH_ORGANIZATION_IMAGES + "/" + organization_id + ".jpg");
                if(!file.exists())
                    return null;
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
