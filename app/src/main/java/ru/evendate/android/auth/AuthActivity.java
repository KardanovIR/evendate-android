package ru.evendate.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.gcm.RegistrationGCMIntentService;
import ru.evendate.android.network.ApiFactory;

/**
 * Created by fj on 14.09.2015.
 */
public class AuthActivity extends AccountAuthenticatorAppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private final String LOG_TAG = AuthActivity.class.getSimpleName();

    private final String VK_URL = "https://oauth.vk.com/authorize?client_id=5029623&scope=friends,email,wall,offline,pages,photos,groups&redirect_uri=" + ApiFactory.HOST_NAME + "/vkOauthDone.php?mobile=true&response_type=token";
    private final String FB_URL = "https://www.facebook.com/dialog/oauth?client_id=1692270867652630&response_type=token&scope=public_profile,email,user_friends&display=popup&redirect_uri=" + ApiFactory.HOST_NAME + "/fbOauthDone.php?mobile=true";
    private final String GOOGLE_URL = "https://accounts.google.com/o/oauth2/auth?scope=email profile https://www.googleapis.com/auth/plus.login &redirect_uri=" + ApiFactory.HOST_NAME + "/googleOauthDone.php?mobile=true&response_type=token&client_id=403640417782-lfkpm73j5gqqnq4d3d97vkgfjcoebucv.apps.googleusercontent.com";

    private final String GOOGLE_SCOPE = "oauth2:email profile https://www.googleapis.com/auth/plus.login";

    static public String URL_KEY = "url";
    private static final int REQ_SIGN_IN_REQUIRED = 55664;
    private final int REQUEST_INTRO = 1;
    private final int REQUEST_WEB_AUTH = 2;
    private static final int REQUEST_SIGN_IN = 3;

    @Bind(R.id.sing_in_vk_button) FrameLayout SingInVkButton;
    @Bind(R.id.sing_in_fb_button) FrameLayout SingInFbButton;
    @Bind(R.id.sing_in_google_button) FrameLayout SingInGoogleButton;

    public final String APP_PREF = "evendate_pref";
    public final String FIRST_RUN = "first_run";

    GoogleApiClient apiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        SingInVkButton.setOnClickListener(this);
        SingInFbButton.setOnClickListener(this);
        SingInGoogleButton.setOnClickListener(this);

        apiClient = initGoogleApiClient();
        checkFirstRun();

        final AccountManager am = AccountManager.get(this);
        // TODO change account
        // temporary we remove function to change accounts
        // delete old account
        Account oldAccount = EvendateAccountManager.getSyncAccount(this);
        if (oldAccount != null)
            am.removeAccount(oldAccount, null, null);

    }

    private GoogleApiClient initGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN), new Scope(Scopes.EMAIL), new Scope(Scopes.PROFILE))
                .build();
        return new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    void checkFirstRun() {
        SharedPreferences preferences = getSharedPreferences(APP_PREF, MODE_PRIVATE);
        if (preferences.getBoolean(FIRST_RUN, true)) {
            preferences.edit().putBoolean(FIRST_RUN, false).apply();
            startActivityForResult(new Intent(this, IntroActivity.class), REQUEST_INTRO);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        apiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                hideProgressDialog();
                            }
                        });
            }

            @Override
            public void onConnectionSuspended(int i) {
                hideProgressDialog();
            }
        });

        showProgressDialog();
        apiClient.connect();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        apiClient.disconnect();
        mProgressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sing_in_vk_button:
                startAuth(VK_URL);
                break;
            case R.id.sing_in_fb_button:
                startAuth(FB_URL);
                break;
            case R.id.sing_in_google_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                startActivityForResult(signInIntent, REQUEST_SIGN_IN);
        }
    }

    private void startAuth(String Url) {
        Intent intent = new Intent(this, WebAuthActivity.class);
        intent.putExtra(URL_KEY, Url);
        startActivityForResult(intent, REQUEST_WEB_AUTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
        if (requestCode == REQUEST_INTRO) {
            if(resultCode == RESULT_CANCELED) {
                finish();
            }
        }
        if (requestCode == REQUEST_WEB_AUTH) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(WebAuthActivity.EMAIL);
                String token = data.getStringExtra(WebAuthActivity.TOKEN);
                onTokenReceived(email, token);
            }
            else{
                //TODO
            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        GoogleSignInAccount acct = result.getSignInAccount();
        if (result.isSuccess() && acct != null) {
            new RetrieveTokenTask().execute(acct.getEmail());
        } else {
            if(result.getStatus().isCanceled())
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }
    }
    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), email, GOOGLE_SCOPE);
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
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            onGoogleTokenReceived(token);
        }
    }

    private void onGoogleTokenReceived(String token) {
        String url = ApiFactory.HOST_NAME + "/oAuthDone.php?mobile=true&type=google&token_type=Bearer&expires_in=3600&" +
                "authuser=0&session_state=49f4dc4624058e6cd300e7ec1c42331c52f1a97b..fb18&prompt=none&access_token=";
        url += token;
        startAuth(url);
    }

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
            EvendateAccountManager.setActiveAccountName(this, account.name);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        //TODO
    }
}
