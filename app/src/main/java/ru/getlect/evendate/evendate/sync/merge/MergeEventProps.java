package ru.getlect.evendate.evendate.sync.merge;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;

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

    //ContentUri = null always
    //да, это говнокод
    @Override
    public void mergeData(Uri ContentUri, ArrayList<DataModel> cloudList, ArrayList<DataModel> localList){

        // Build hash table of incoming entries
        HashMap<Integer, DataModel> cloudMap = new HashMap<>();
        for (DataModel e : cloudList) {
            cloudMap.put(e.getEntryId(), e);
        }

        // Get list of all items
//        Log.i(LOG_TAG, "update for " + ContentUri.toString());
        Log.i(LOG_TAG, "Fetching local entries for merge");
        Log.i(LOG_TAG, "Found " + localList.size() + " local entries. Computing merge solution...");

        for(DataModel e : localList){
            DataModel match = cloudMap.get(e.getEntryId());
            if (match != null) {

                    Log.i(LOG_TAG, "Scheduling update: ");
                Uri contentUriTags = EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(e.getId())).appendPath(EvendateContract.PATH_TAGS).build();
                Uri contentUriFriends = EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(e.getId())).appendPath(EvendateContract.PATH_USERS).build();
                ArrayList<DataModel> tagList = new ArrayList<>();
                ArrayList<DataModel> tagListMatch = new ArrayList<>();
                tagList.addAll(((EventModel) e).getTagList());
                tagListMatch.addAll(((EventModel) match).getTagList());
                mMergerTags.mergeData(contentUriTags, tagListMatch, tagList);
                ArrayList<DataModel> friendList = new ArrayList<>();
                ArrayList<DataModel> friendListMatch = new ArrayList<>();
                friendList.addAll(((EventModel) e).getFriendList());
                friendListMatch.addAll(((EventModel) match).getFriendList());
                mMergerFriends.mergeData(contentUriFriends, friendList, friendListMatch);
                mContentResolver.notifyChange(contentUriTags, null, false);
                mContentResolver.notifyChange(contentUriFriends, null, false);
            } else {
                Log.e(LOG_TAG, "WTF");
            }
        }
        Log.i(LOG_TAG, "Batch update done");
    }
}
