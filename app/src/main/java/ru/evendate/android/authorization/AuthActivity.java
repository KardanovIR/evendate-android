package ru.evendate.android.authorization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ru.evendate.android.R;
import ru.evendate.android.gcm.RegistrationGCMIntentService;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateSyncAdapter;

/**
 * Created by fj on 14.09.2015.
 */
public class AuthActivity extends AccountAuthenticatorAppCompatActivity implements View.OnClickListener {
    private final String LOG_TAG = AuthActivity.class.getSimpleName();

    private final String VK_URL = "https://oauth.vk.com/authorize?client_id=5029623&scope=friends,email,offline,nohttps&redirect_uri=" + EvendateApiFactory.HOST_NAME + "/vkOauthDone.php?mobile=true&response_type=code";
    private final String FB_URL = "https://www.facebook.com/dialog/oauth?client_id=1692270867652630&response_type=code&scope=public_profile,email,user_friends&display=popup&redirect_uri=" + EvendateApiFactory.HOST_NAME + "/fbOauthDone.php?mobile=true";
    private final String GOOGLE_URL = "https://accounts.google.com/o/oauth2/auth?scope=email profile https://www.googleapis.com/auth/plus.login &redirect_uri=" + EvendateApiFactory.HOST_NAME + "/googleOauthDone.php?mobile=true&response_type=token&client_id=403640417782-lfkpm73j5gqqnq4d3d97vkgfjcoebucv.apps.googleusercontent.com";

    static public String URL_KEY = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        AccountChooserFragment fragment = new AccountChooserFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.auth_container, fragment)
                .commit();

        final AccountManager am = AccountManager.get(this);
        // TODO change account
        // temporary we remove function to change accounts
        // delete old account
        Account oldAccount = EvendateSyncAdapter.getSyncAccount(getBaseContext());
        if(oldAccount != null)
            am.removeAccount(oldAccount, null, null);
    }


    @Override
    public void onClick(View v) {
        Bundle args = new Bundle();
        switch (v.getId()){
            case R.id.sing_in_vk_button:
                args.putString(URL_KEY, VK_URL);
                break;
            case R.id.sing_in_fb_button:
                args.putString(URL_KEY, FB_URL);
                break;
            case R.id.sing_in_google_button:
                args.putString(URL_KEY, GOOGLE_URL);
                break;
        }
        Log.i(LOG_TAG, "replace fragment");
        WebAuthFragment fragment = new WebAuthFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, fragment)
                .commit();
    }

    public void onTokenReceived(Account account, String password, String token) {

        final AccountManager am = AccountManager.get(this);

        final Bundle result = new Bundle();
        if (am.addAccountExplicitly(account, password, new Bundle())) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, token);
            am.setAuthToken(account, account.type, token);

            //save account email into shared preferences to find current account later
            SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, account.name);
            // will be updated by sync
            ed.putString(EvendateSyncAdapter.FIRST_NAME, null);
            ed.putString(EvendateSyncAdapter.FIRST_NAME, null);
            ed.putString(EvendateSyncAdapter.FIRST_NAME, null);
            ed.apply();

        } else {
            Log.i(LOG_TAG, "cannot add account");
            result.putString(AccountManager.KEY_ERROR_MESSAGE, getString(R.string.account_already_exists));
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        //ContentResolver.addPeriodicSync(
        //        EvendateSyncAdapter.getSyncAccount(this),
        //        getString(R.string.content_authority),
        //        Bundle.EMPTY,
        //        EvendateSyncAdapter.SYNC_INTERVAL);
        //ContentResolver.setSyncAutomatically(EvendateSyncAdapter.getSyncAccount(this), getString(R.string.content_authority), true);
        //EvendateSyncAdapter.syncImmediately(this);
        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationGCMIntentService.class);
        startService(intent);
        setAccountAuthenticatorResult(result);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
