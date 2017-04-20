package ru.evendate.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import ru.evendate.android.auth.AuthActivity;

/**
 * Created by Dmitry on 27.01.2016.
 * управляет аккаунтами: смена, обнова, выпил
 */
public class EvendateAccountManager {
    /**
     * field in preferences that contain active account name
     * needed for getSyncAccount
     */
    private static final String ACTIVE_ACCOUNT_NAME = "active_account_name";
    private static final String ACCOUNT_PREFERENCES = "account_preferences";
    private static final String KEY_AUTH_ACTIVE = "auth_active";
    private static String LOG_TAG = EvendateAccountManager.class.getSimpleName();

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        android.accounts.AccountManager accountManager =
                (android.accounts.AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        String account_name = getActiveAccountName(context);

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

    public static String getActiveAccountName(Context context) {
        return getAuthPreference(context).getString(ACTIVE_ACCOUNT_NAME, null);
    }

    //save account email into shared preferences to find current account later
    public static void setActiveAccountName(Context context, String accountName) {
        SharedPreferences.Editor ed = getAuthPreference(context).edit();
        ed.putString(ACTIVE_ACCOUNT_NAME, accountName);
        ed.apply();
    }

    public static void deleteAccount(Context context) {
        AccountManager accountManager = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);
        accountManager.removeAccount(getSyncAccount(context), null, null);

        UnregisterGCMTask task = new UnregisterGCMTask(context);
        task.execute();
        Log.i(LOG_TAG, "account removed");
    }

    public static String peekToken(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        String token = null;
        try {
            token = accountManager.peekAuthToken(EvendateAccountManager.getSyncAccount(context),
                    context.getString(R.string.account_type));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error with peeking token");
            e.fillInStackTrace();
        }
        if (token == null) {
            startAuth(context);
        }
        return token;
    }

    protected static void invalidateToken(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.invalidateAuthToken(context.getString(R.string.account_type), peekToken(context));
    }

    private static void startAuth(Context context) {
        if (!isAuthRunning(context)) {
            Intent intent = new Intent(context, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void setAuthRunning(Context context) {
        SharedPreferences.Editor edit = getAuthPreference(context).edit();
        edit.putBoolean(KEY_AUTH_ACTIVE, true);
        edit.apply();
    }

    public static void setAuthDone(Context context) {
        SharedPreferences.Editor edit = getAuthPreference(context).edit();
        edit.putBoolean(KEY_AUTH_ACTIVE, false);
        edit.apply();
    }

    private static SharedPreferences getAuthPreference(Context context) {
        return context.getSharedPreferences(ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
    }

    //true cause first start in main activity
    public static boolean isAuthRunning(Context context) {
        return getAuthPreference(context).getBoolean(KEY_AUTH_ACTIVE, true);
    }

    private static class UnregisterGCMTask extends AsyncTask<Void, Void, Void> {
        Context context;

        public UnregisterGCMTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InstanceID instanceID = InstanceID.getInstance(context);
                instanceID.deleteInstanceID();
                Log.d(LOG_TAG, "GCM UNREGISTERED");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "can't delete gcm registration");
            }
            return null;
        }
    }
}
