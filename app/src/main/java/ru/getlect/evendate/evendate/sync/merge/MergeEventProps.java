package ru.getlect.evendate.evendate.sync.merge;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class MergeEventProps extends MergeStrategy {
    String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();
    String tagKeyToAdd = "addTags";
    String tagKeyToRemove = "removeTags";
    String friendKeyToAdd = "addFriends";
    String friendKeyToRemove = "removeFriends";
    MergeStrategy mMergerFriends;
    MergeStrategy mMergerTags;

    public MergeEventProps(ContentResolver contentResolver) {
        super(contentResolver);
        mMergerFriends = new MergeProperties(mContentResolver, friendKeyToAdd, friendKeyToRemove);
        mMergerTags = new MergeProperties(mContentResolver, tagKeyToAdd, tagKeyToRemove);
    }

    @Override
    public void mergeData(Uri ContentUri, ArrayList<DataEntry> cloudList, ArrayList<DataEntry> localList){

        // Build hash table of incoming entries
        HashMap<Integer, DataEntry> cloudMap = new HashMap<>();
        for (DataEntry e : cloudList) {
            cloudMap.put(e.getEntryId(), e);
        }

        // Get list of all items
        Log.i(LOG_TAG, "update for " + ContentUri.toString());
        Log.i(LOG_TAG, "Fetching local entries for merge");
        Log.i(LOG_TAG, "Found " + localList.size() + " local entries. Computing merge solution...");

        for(DataEntry e : localList){
            DataEntry match = cloudMap.get(e.getEntryId());
            if (match != null) {

                    Log.i(LOG_TAG, "Scheduling update: ");
                Uri contentUriTags = EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                        .appendPath("/" + e.getId() + "/" + EvendateContract.PATH_TAGS).build();
                Uri contentUriFriends = EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                        .appendPath("/" + e.getId() + "/" + EvendateContract.PATH_USERS).build();
                mMergerTags.mergeData(contentUriTags,
                        ((EventEntry)match).getTagList(), ((EventEntry) e).getTagList());
                mMergerFriends.mergeData(contentUriFriends,
                        ((EventEntry)match).getFriendList(), ((EventEntry) e).getFriendList());
                mContentResolver.notifyChange(contentUriTags, null, false);
                mContentResolver.notifyChange(contentUriFriends, null, false);
            } else {
                Log.e(LOG_TAG, "WTF");
            }
        }
        Log.i(LOG_TAG, "Batch update done");
    }
}
