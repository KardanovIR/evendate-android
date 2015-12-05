package ru.evendate.android.sync.merge;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.sync.models.DataModel;
import ru.evendate.android.sync.models.EventModel;

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

        for(DataModel localEvent : localList){
            DataModel cloudEvent = cloudMap.get(localEvent.getEntryId());
            if (cloudEvent != null) {

                Log.i(LOG_TAG, "Scheduling update: " + ContentUri);
                Uri contentUriTags = EvendateContract.EventTagEntry.GetContentUri(localEvent.getEntryId());
                Uri contentUriFriends = EvendateContract.UserEventEntry.getContentUri(localEvent.getEntryId());
                Uri contentUriDates = EvendateContract.EventDateEntry.getContentUri(localEvent.getEntryId());
                ArrayList<DataModel> tagLocalList = new ArrayList<>();
                ArrayList<DataModel> tagListMatch = new ArrayList<>();
                tagLocalList.addAll(((EventModel) localEvent).getTagList());
                tagListMatch.addAll(((EventModel) cloudEvent).getTagList());
                mMergerTags.mergeData(contentUriTags, tagListMatch, tagLocalList);

                ArrayList<DataModel> friendLocalList = new ArrayList<>();
                ArrayList<DataModel> friendListMatch = new ArrayList<>();
                friendLocalList.addAll(((EventModel) localEvent).getFriendList());
                friendListMatch.addAll(((EventModel) cloudEvent).getFriendList());
                //mMergerFriends.mergeData(contentUriFriends, friendListMatch, friendLocalList);
                mergeDates(contentUriDates, ((EventModel) cloudEvent).getDataRangeList(),
                        ((EventModel) localEvent).getDataRangeList());
            } else {
                Log.wtf(LOG_TAG, "WTF");
            }
        }
        Log.v(LOG_TAG, "Batch update done");
    }
    private void mergeDates(Uri ContentUri, ArrayList<String> cloudList, ArrayList<String> localList){
        Log.d(LOG_TAG, "started sync date ranges");
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (String cloudDate: cloudList) {
            boolean found = false;
            for (String localDate: localList) {
                if (cloudDate.equals(localDate)) {
                    localList.remove(localDate);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Log.d(LOG_TAG, "insert date " + cloudDate);
                batch.add(ContentProviderOperation.newInsert(ContentUri)
                        .withValue(EvendateContract.EventDateEntry.COLUMN_DATE, cloudDate).build());
            }
            else{
                localList.remove(cloudDate);
            }
        }
        for(String localDate: localList){
            Log.d(LOG_TAG, "remove date " + localDate);
            batch.add(ContentProviderOperation.newDelete(ContentUri.buildUpon()
                    .appendQueryParameter("date", localDate).build()).build());
        }
        try {
            mContentResolver.applyBatch(EvendateContract.CONTENT_AUTHORITY, batch);
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return;
        }
        mContentResolver.notifyChange(
                ContentUri, // URI where data was modified
                null,                           // No local observer
                false);
    }
}
