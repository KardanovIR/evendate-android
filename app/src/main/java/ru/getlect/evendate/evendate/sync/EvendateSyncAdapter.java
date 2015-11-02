package ru.getlect.evendate.evendate.sync;

/**
 * Created by Dmitry on 08.09.2015.
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ru.getlect.evendate.evendate.R;
import ru.getlect.evendate.evendate.authorization.AuthActivity;
import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;
import ru.getlect.evendate.evendate.sync.merge.MergeEventProps;
import ru.getlect.evendate.evendate.sync.merge.MergeSimple;
import ru.getlect.evendate.evendate.sync.merge.MergeStrategy;
import ru.getlect.evendate.evendate.sync.merge.MergeWithoutDelete;

public class EvendateSyncAdapter extends AbstractThreadedSyncAdapter {
    String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();
    // Global variables

    ContentResolver mContentResolver;
    Context mContext;

    /**
     * Set up the sync adapter
     */
    public EvendateSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public EvendateSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
        mContentResolver = context.getContentResolver();

    }
    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     * тут адский говнокод, от которого у меня бомбило при написании и бомбит до сих пор
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
        //token here

        AccountManager accountManager = AccountManager.get(mContext);
        String urlOrganization = "http://evendate.ru/api/organizations?with_subscriptions=true";
        String urlTags = "http://evendate.ru/api/tags";
        String urlEvents = "http://evendate.ru/api/events/my"; // + теги + друзьяшки + тока будущее
        ArrayList<DataEntry> cloudList;
        ArrayList<DataEntry> localList;
        LocalDataFetcher localDataFetcher = new LocalDataFetcher(mContentResolver);
        MergeStrategy merger = new MergeSimple(mContentResolver);
        MergeStrategy mergerSoft = new MergeWithoutDelete(mContentResolver);

        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        ImageManager imageManager = new ImageManager(localDataFetcher);
        try {
            String token = accountManager.blockingGetAuthToken(account, mContext.getString(R.string.account_type), false);

            String jsonOrganizations = getJsonFromServer(urlOrganization, token);
            String jsonTags = getJsonFromServer(urlTags, token);
            String jsonEvents = getJsonFromServer(urlEvents, token);

            //cloudList = ServerDataFetcher.getOrganizationData(evendateService);
            cloudList = CloudDataParser.getOrganizationDataFromJson(jsonOrganizations);
            localList = localDataFetcher.getOrganizationDataFromDB();
            merger.mergeData(EvendateContract.OrganizationEntry.CONTENT_URI, cloudList, localList);
            imageManager.updateOrganizationsImages(cloudList);
            imageManager.updateOrganizationsLogos(cloudList);

            cloudList = CloudDataParser.getTagsDataFromJson(jsonTags);
            localList = localDataFetcher.getTagsDataFromDB();
            merger.mergeData(EvendateContract.TagEntry.CONTENT_URI, cloudList, localList);

            //TODO надо зафигачить список другов один, ибо список локальный не обновляется
            cloudList = CloudDataParser.getEventsDataFromJson(jsonEvents);
            ArrayList<DataEntry> localFriendList = localDataFetcher.getUserDataFromDB();
            for(DataEntry e : cloudList){
                ArrayList<DataEntry> cloudFriendList = ((EventEntry) e).getFriendList();
                mergerSoft.mergeData(EvendateContract.UserEntry.CONTENT_URI, cloudFriendList, localFriendList);
            }

            ArrayList<DataEntry> eventList = localDataFetcher.getEventDataFromDB();
            merger.mergeData(EvendateContract.EventEntry.CONTENT_URI, cloudList, eventList);
            for(DataEntry e: eventList){
                ((EventEntry)e).setTagList(localDataFetcher.getEventTagDataFromDB(e.getId()));
                ((EventEntry)e).setFriendList(localDataFetcher.getEventFriendDataFromDB(e.getId()));
            }
            MergeStrategy mergerEventProps = new MergeEventProps(mContentResolver);
            mergerEventProps.mergeData(null, cloudList, eventList);

            imageManager.updateEventImages(cloudList);

        }catch (JSONException|IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }catch (OperationCanceledException|AuthenticatorException e){
            Log.e(LOG_TAG, "problem with getting token");
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }
    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
//
        //// Create the account type and default account
        ////Account newAccount = new Account(
        ////        context.getString(R.string.app_name), context.getString(R.string.account_type));
//
        //// If the password doesn't exist, the account doesn't exist
        //if ( null == accountManager.getPassword(newAccount) ) {
//
        ///*
        // * Add the account and account type, no password or user data
        // * If successful, return the Account object, otherwise report an error.
        // */
        //    if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
        //        return null;
        //    }
        //    /*
        //     * If you don't set android:syncable="true" in
        //     * in your <provider> element in the manifest,
        //     * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
        //     * here.
        //     */
//
        //    onAccountCreated(newAccount, context);
        //}

        Account [] accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
        if (accounts.length == 0) {
            Log.e("SYNC", "No Accounts");
            Intent dialogIntent = new Intent(context, AuthActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dialogIntent);
            return null;
        }
        Account account = accounts[0];
        //onAccountCreated(account, context);
        return account;
    }
    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        //SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        //ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private String getJsonFromServer(String urlServer, String token){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonString;

        try {
            URL url = new URL(urlServer);

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", token);
            urlConnection.connect();

            //Read the input stream into String eventsJsonStr
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            jsonString = buffer.toString();
            Log.w(LOG_TAG, jsonString);
            return jsonString;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return null;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }
}
