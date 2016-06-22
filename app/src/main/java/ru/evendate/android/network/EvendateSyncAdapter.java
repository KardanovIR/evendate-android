package ru.evendate.android.network;

/**
 * Created by Dmitry on 08.09.2015.
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import ru.evendate.android.R;
import ru.evendate.android.auth.Authenticator;

@Deprecated
public class EvendateSyncAdapter extends AbstractThreadedSyncAdapter {
    private static String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    public static final int ENTRY_LIMIT = 1000;
    public static final int PAGE = 0;

    public static String SYNC_FINISHED = "sync_finished";
    public static boolean isSyncRunning = false;
    ContentResolver mContentResolver;
    Context mContext;

    /**
     * constants for user data in shared preferences
     */

    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String AVATAR_URL = "avatar_url";

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

        Log.i(LOG_TAG, "SYNC_STARTED");
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        if (isSyncRunning)
            return;
        isSyncRunning = true;
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        Account account = getSyncAccount(context);
        if (account == null) {
            Log.e(LOG_TAG, "no account");
            return;
        }
        ContentResolver.requestSync(account,
                context.getString(R.string.content_authority), bundle);
        Log.d(LOG_TAG, "Scheduled sync");
    }

    /**
     * @param context The application context
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        SharedPreferences sPref = context.getSharedPreferences(Authenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
        String account_name = sPref.getString(Authenticator.ACTIVE_ACCOUNT_NAME, null);

        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
        if (accounts.length == 0 || account_name == null) {
            Log.e(LOG_TAG, "get account: No Accounts");
            return null;
        }
        for (Account account : accounts) {
            if (account.name.equals(account_name))
                return account;
        }
        return null;
    }

    //private static void onAccountCreated(Account newAccount, Context context) {
    //    /*
    //     * Since we've created an account
    //     */
    //
    //    /*
    //     * Without calling setSyncAutomatically, our periodic sync will not be enabled.
    //     */
    //    ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    //
    //    /*
    //     * Finally, let's do a sync to get things started
    //     */
    //    syncImmediately(context);
    //}
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean result = true;
        if (activeNetwork == null)
            result = false;
        else {
            boolean isConnected = activeNetwork.isConnected();
            if (!isConnected) {
                result = false;
            }
        }
        return result;
    }
}
