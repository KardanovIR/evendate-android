package ru.evendate.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Dmitry on 27.01.2016.
 * управляет аккаунтами: смена, обнова, выпил
 */
public class EvendateAccountManager {
    /**
     * field in preferences that contain active account name
     * needed for getAccount
     */
    private static String LOG_TAG = EvendateAccountManager.class.getSimpleName();
    private static final String ACTIVE_ACCOUNT_NAME = "active_account_name";
    private static final String ACCOUNT_PREFERENCES = "account_preferences";
    private static final String KEY_FIRST_AUTH_DONE = "first_auth_done";
    private static final String KEY_FIRST_ONBOARDING_DONE = "first_ondoarding_done";

    public static Account getAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = AccountManager.get(context);

        String account_name = getActiveAccountName(context);
        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
        if (accounts.length == 0 || account_name == null) {
            Log.i(LOG_TAG, "no account exists");
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

    public static void deleteAllAppAccounts(Context context) {
        try {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(context.getString(R.string.account_type));
            for (Account account : accounts) {
                am.removeAccount(account, null, null);
                Log.i(LOG_TAG, "account removed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        invalidateToken(context);
        setActiveAccountName(context, null);
    }

    @Nullable
    public static String peekToken(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        try {
            return accountManager.peekAuthToken(EvendateAccountManager.getAccount(context),
                    context.getString(R.string.account_type));
        } catch (Exception e) {
            Log.i(LOG_TAG, "No token exists");
            return null;
        }
    }

    static void invalidateToken(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.invalidateAuthToken(context.getString(R.string.account_type), peekToken(context));
    }

    public static void setFirstAuthDone(Context context) {
        SharedPreferences.Editor edit = getAuthPreference(context).edit();
        edit.putBoolean(KEY_FIRST_AUTH_DONE, true);
        edit.apply();
    }

    public static boolean getFirstAuthDone(Context context) {
        return getAuthPreference(context).getBoolean(KEY_FIRST_AUTH_DONE, false);
    }

    public static void setOnboardingDone(Context context) {
        SharedPreferences.Editor edit = getAuthPreference(context).edit();
        edit.putBoolean(KEY_FIRST_ONBOARDING_DONE, true);
        edit.apply();
    }

    public static void setOnboardingUndone(Context context) {
        SharedPreferences.Editor edit = getAuthPreference(context).edit();
        edit.putBoolean(KEY_FIRST_ONBOARDING_DONE, false);
        edit.apply();
    }

    public static boolean getOnboardingDone(Context context) {
        return getAuthPreference(context).getBoolean(KEY_FIRST_ONBOARDING_DONE, false);
    }

    private static SharedPreferences getAuthPreference(Context context) {
        return context.getSharedPreferences(ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
    }

}
