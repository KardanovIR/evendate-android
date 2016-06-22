package ru.evendate.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;

import ru.evendate.android.R;
import ru.evendate.android.gcm.RegistrationGCMIntentService;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.EvendateSyncAdapter;

/**
 * Created by fj on 14.09.2015.
 */
public class AuthActivity extends AccountAuthenticatorAppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, WebAuthFragment.TokenReceiver {
    private final String LOG_TAG = AuthActivity.class.getSimpleName();

    private final String VK_URL = "https://oauth.vk.com/authorize?client_id=5029623&scope=friends,email,wall,offline,pages,photos,groups&redirect_uri=" + ApiFactory.HOST_NAME + "/vkOauthDone.php?mobile=true&response_type=token";
    private final String FB_URL = "https://www.facebook.com/dialog/oauth?client_id=1692270867652630&response_type=token&scope=public_profile,email,user_friends&display=popup&redirect_uri=" + ApiFactory.HOST_NAME + "/fbOauthDone.php?mobile=true";
    private final String GOOGLE_URL = "https://accounts.google.com/o/oauth2/auth?scope=email profile https://www.googleapis.com/auth/plus.login &redirect_uri=" + ApiFactory.HOST_NAME + "/googleOauthDone.php?mobile=true&response_type=token&client_id=403640417782-lfkpm73j5gqqnq4d3d97vkgfjcoebucv.apps.googleusercontent.com";

    private final String GOOGLE_SCOPE = "oauth2:email profile https://www.googleapis.com/auth/plus.login";

    static public String URL_KEY = "url";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQ_SIGN_IN_REQUIRED = 55664;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        if (oldAccount != null)
            am.removeAccount(oldAccount, null, null);
    }


    @Override
    public void onClick(View v) {
        Bundle args = new Bundle();
        switch (v.getId()) {
            case R.id.sing_in_vk_button:
                args.putString(URL_KEY, VK_URL);
                break;
            case R.id.sing_in_fb_button:
                args.putString(URL_KEY, FB_URL);
                break;
            case R.id.sing_in_google_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(getGoogleApiClient());
                startActivityForResult(signInIntent, RC_SIGN_IN);
                return;
        }
        Log.i(LOG_TAG, "replace fragment");
        WebAuthFragment fragment = new WebAuthFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, fragment)
                .commit();
    }

    private GoogleApiClient getGoogleApiClient(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
        return new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onTokenReceived(String email, String token) {

        final AccountManager manager = AccountManager.get(this);
        String accountType = getResources().getString(R.string.account_type);
        Account account = new Account(email, accountType);

        final Bundle result = new Bundle();
        if (manager.addAccountExplicitly(account, "", new Bundle())) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, token);
            manager.setAuthToken(account, account.type, token);

        } else {
            Log.i(LOG_TAG, "cannot add account");
            result.putString(AccountManager.KEY_ERROR_MESSAGE, getString(R.string.account_already_exists));
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            new RetrieveTokenTask().execute(acct.getEmail());
        } else {
            Toast.makeText(this, "Error occured", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, GOOGLE_SCOPE);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("retrieve", s);
        }
    }

}
