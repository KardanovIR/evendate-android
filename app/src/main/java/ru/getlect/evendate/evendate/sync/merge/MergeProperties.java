package ru.getlect.evendate.evendate.sync.merge;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.models.DataModel;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class MergeProperties extends MergeStrategy {
    String LOG_TAG = MergeProperties.class.getSimpleName();
    String keyToAdd;
    String KeyToDelete;

    public MergeProperties(ContentResolver contentResolver, String keyToAdd, String KeyToDelete){
        super(contentResolver);
        this.keyToAdd = keyToAdd;
        this.KeyToDelete = KeyToDelete;
    }

    @Override
    public void mergeData(final Uri ContentUri, ArrayList<DataModel> cloudList, ArrayList<DataModel> localList){
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming entries
        HashMap<Integer, DataModel> cloudMap = new HashMap<>();
        for (DataModel e : cloudList) {
            cloudMap.put(e.getEntryId(), e);
        }

        // Get list of all items
        Log.v(LOG_TAG, "update for " + ContentUri.toString());
        Log.v(LOG_TAG, "Found " + localList.size() + " local entries. Computing merge solution...");

        String propListToDelete = "";
        //we will try to find property that don't exist in cloud
        for(DataModel localProp : localList){
            DataModel match = cloudMap.get(localProp.getEntryId());
            if (match != null) {
                // Entry exists. We remove entry from cloud map to prevent insert
                cloudMap.remove(localProp.getEntryId());
            } else {
                // Entry doesn't exist. Let's prepare removing property
                Log.v(LOG_TAG, "Scheduling delete props: entry_id=" + localProp.getEntryId());
                if(propListToDelete.isEmpty())
                    propListToDelete += (Integer.toString(localProp.getEntryId()));
                else
                    propListToDelete += "," + (Integer.toString(localProp.getEntryId()));
            }
        }

        String propListToAdd = "";
        // Add new properties
        for (DataModel cloudProp : cloudMap.values()) {
            Log.v(LOG_TAG, "Scheduling insert props: entry_id=" + cloudProp.getEntryId());
            if(propListToAdd.isEmpty())
                propListToAdd += (Integer.toString(cloudProp.getEntryId()));
            else
                propListToAdd += "," + (Integer.toString(cloudProp.getEntryId()));
        }
        //just for logging
        boolean isChanged = false;
        if(!propListToDelete.isEmpty()){
            Log.i(LOG_TAG, "Scheduling delete: props = " + propListToDelete);
            batch.add(ContentProviderOperation.newDelete(Uri.parse(ContentUri.toString() + "?" + KeyToDelete + "=" + propListToDelete)).build());
            isChanged = true;
        }
        if(!propListToAdd.isEmpty()){
            Log.i(LOG_TAG, "Scheduling insert: props = " + propListToAdd);
            batch.add(ContentProviderOperation.newInsert(Uri.parse(ContentUri.toString() + "?" + keyToAdd + "=" + propListToAdd)).build());
            isChanged = true;
        }
        if(!isChanged)
            Log.i(LOG_TAG, "no action with props " + ContentUri);
        Log.v(LOG_TAG, "Merge solution ready. Applying batch update");
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
        Log.v(LOG_TAG, "Batch update done");
    }
}