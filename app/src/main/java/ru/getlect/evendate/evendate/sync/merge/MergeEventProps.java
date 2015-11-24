package ru.getlect.evendate.evendate.sync.merge;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class MergeEventProps extends MergeStrategy {
    String LOG_TAG = MergeEventProps.class.getSimpleName();
    MergeStrategy mMergerFriends;
    MergeStrategy mMergerTags;

    public MergeEventProps(ContentResolver contentResolver) {
        super(contentResolver);
        mMergerFriends = new MergeProperties(mContentResolver,
                EvendateContract.UserEventEntry.QUERY_ADD_PARAMETER_NAME,
                EvendateContract.UserEventEntry.QUERY_REMOVE_PARAMETER_NAME);
        mMergerTags = new MergeProperties(mContentResolver,
                EvendateContract.EventTagEntry.QUERY_ADD_PARAMETER_NAME,
                EvendateContract.EventTagEntry.QUERY_REMOVE_PARAMETER_NAME);
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
        Log.v(LOG_TAG, "Fetching local entries for merge");
        Log.v(LOG_TAG, "Found " + localList.size() + " local entries. Computing merge solution...");

        for(DataModel e : localList){
            DataModel match = cloudMap.get(e.getEntryId());
            if (match != null) {

                Log.i(LOG_TAG, "Scheduling update: " + ContentUri);
                Uri contentUriTags = EvendateContract.EventTagEntry.GetContentUri(e.getEntryId());
                Uri contentUriFriends = EvendateContract.UserEventEntry.getContentUri(e.getEntryId());
                ArrayList<DataModel> tagLocalList = new ArrayList<>();
                ArrayList<DataModel> tagListMatch = new ArrayList<>();
                tagLocalList.addAll(((EventModel) e).getTagList());
                tagListMatch.addAll(((EventModel) match).getTagList());
                mMergerTags.mergeData(contentUriTags, tagListMatch, tagLocalList);

                ArrayList<DataModel> friendLocalList = new ArrayList<>();
                ArrayList<DataModel> friendListMatch = new ArrayList<>();
                friendLocalList.addAll(((EventModel) e).getFriendList());
                friendListMatch.addAll(((EventModel) match).getFriendList());
                //mMergerFriends.mergeData(contentUriFriends, friendListMatch, friendLocalList);
                mContentResolver.notifyChange(contentUriTags, null, false);
                mContentResolver.notifyChange(contentUriFriends, null, false);
            } else {
                Log.wtf(LOG_TAG, "WTF");
            }
        }
        Log.v(LOG_TAG, "Batch update done");
    }
}
