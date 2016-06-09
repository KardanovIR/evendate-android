package ru.evendate.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import ru.evendate.android.authorization.AuthActivity;
import ru.evendate.android.authorization.EvendateAuthenticator;

/**
 * Created by Dmitry on 27.01.2016.
 * управляет аккаунтами: смена, обнова, выпил
 */
public class EvendateAccountManager {
    private static String LOG_TAG = EvendateAccountManager.class.getSimpleName();

    /**
     * @param context The application context
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        android.accounts.AccountManager accountManager =
                (android.accounts.AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        SharedPreferences sPref = context.getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
        String account_name = sPref.getString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, null);

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

    public static void deleteAccount(Context context) {
        AccountManager accountManager = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);
        accountManager.removeAccount(getSyncAccount(context), null, null);
        context.startActivity(new Intent(context, AuthActivity.class));
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
            context.startActivity(new Intent(context, AuthActivity.class));
        }
        return token;
    }
}
