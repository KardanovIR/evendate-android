package ru.getlect.evendate.evendate.sync.merge;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;

/**
 * Created by Dmitry on 12.09.2015.
 */
public class MergeWithoutDelete extends MergeStrategy{
    String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();

    public MergeWithoutDelete(ContentResolver contentResolver) {
        super(contentResolver);
    }

    @Override
    public void mergeData(Uri ContentUri, ArrayList<DataEntry> cloudList, ArrayList<DataEntry> localList) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

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
                // Entry exists. Remove from entry map to prevent insert later.
                cloudMap.remove(e.getEntryId());
                // Check to see if the entry needs to be updated
                Uri existingUri = ContentUri.buildUpon()
                        .appendPath(Integer.toString(e.getId())).build();
                if (!e.equals(match)){
                    // Update existing record
                    Log.i(LOG_TAG, "Scheduling update: " + existingUri);
                    batch.add(match.getUpdate(ContentUri));
                } else {
                    Log.i(LOG_TAG, "No action: " + existingUri);
                }
            } else {
                Uri deleteUri = ContentUri.buildUpon()
                        .appendPath(Integer.toString(e.getId())).build();
                Log.i(LOG_TAG, "No delete: " + deleteUri);
            }
        }

        // Add new items
        for (DataEntry e : cloudMap.values()) {
            Log.i(LOG_TAG, "Scheduling insert: entry_id=" + e.getEntryId());
            batch.add(e.getInsert(ContentUri));
        }
        Log.i(LOG_TAG, "Merge solution ready. Applying batch update");
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
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
        Log.i(LOG_TAG, "Batch update done");
    }
}
