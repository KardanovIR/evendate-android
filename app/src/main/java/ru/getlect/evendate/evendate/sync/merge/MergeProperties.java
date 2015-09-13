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
 * Created by Dmitry on 13.09.2015.
 */
public class MergeProperties extends MergeStrategy {
    String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();
    String keyToAdd;
    String KeyToDelete;

    public MergeProperties(ContentResolver contentResolver, String keyToAdd, String KeyToDelete){
        super(contentResolver);
        this.keyToAdd = keyToAdd;
        this.KeyToDelete = KeyToDelete;
    }

    @Override
    public void mergeData(final Uri ContentUri, ArrayList<DataEntry> cloudList, ArrayList<DataEntry> localList){
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming entries
        HashMap<Integer, DataEntry> cloudMap = new HashMap<>();
        for (DataEntry e : cloudList) {
            cloudMap.put(e.getEntryId(), e);
        }

        // Get list of all items
        Log.i(LOG_TAG, "update for " + ContentUri.toString());
        Log.i(LOG_TAG, "Found " + localList.size() + " local entries. Computing merge solution...");

        String propListToDelete = "";
        for(DataEntry e : localList){
            DataEntry match = cloudMap.get(e.getEntryId());
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                cloudMap.remove(e.getEntryId());
            } else {
                // Entry doesn't exist. Remove it from the database.
                if(propListToDelete.isEmpty())
                    propListToDelete += (Integer.toString(e.getEntryId()));
                else
                    propListToDelete += "," + (Integer.toString(e.getEntryId()));
            }
        }

        String propListToAdd = "";
        // Add new items
        for (DataEntry e : cloudMap.values()) {
            Log.i(LOG_TAG, "Scheduling insert: entry_id=" + e.getEntryId());
            if(propListToAdd.isEmpty())
                propListToAdd += (Integer.toString(e.getEntryId()));
            else
                propListToAdd += "," + (Integer.toString(e.getEntryId()));
        }
        Log.i(LOG_TAG, "Scheduling delete");
        batch.add(ContentProviderOperation.newDelete(Uri.parse(ContentUri.toString() + "?" + KeyToDelete + propListToDelete)).build());
        Log.i(LOG_TAG, "Scheduling insert");
        batch.add(ContentProviderOperation.newInsert(Uri.parse(ContentUri.toString() + "?" + keyToAdd + propListToAdd)).build());
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