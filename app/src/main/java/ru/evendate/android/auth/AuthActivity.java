package ru.evendate.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.network.ApiFactory;

public class AuthActivity extends AccountAuthenticatorAppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private final String LOG_TAG = AuthActivity.class.getSimpleName();
    private static final String GOOGLE_SCOPE = "oauth2:email profile https://www.googleapis.com/auth/plus.login";
    private static final int REQ_SIGN_IN_REQUIRED = 55664;
    private static final int REQUEST_SIGN_IN = 3;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static public String URL_KEY = "url";
    private final int REQUEST_WEB_AUTH = 2;
    GoogleApiClient apiClient;
    @Bind(R.id.sing_in_google_button) View googleButton;
    private String FB_URL;
    private boolean isGoogleServicesAvailable = false;
    private ProgressDialog mProgressDialog;
    private Dialog serviceDialog;

    public static String getGoogleUrl(Context context) {
        return "https://accounts.google.com/o/oauth2/auth?scope=email profile https://www.googleapis.com/auth/plus.login &redirect_uri=" + ApiFactory.getHostName(context) + "/googleOauthDone.php?mobile=true&response_type=token&client_id=403640417782-lfkpm73j5gqqnq4d3d97vkgfjcoebucv.apps.googleusercontent.com";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initLinks();
        EvendateAccountManager.setAuthRunning(this);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        initTransitions();
        apiClient = initGoogleApiClient();
        deletedOldAccount();

        if (checkPlayServicesExists()) {
            isGoogleServicesAvailable = true;
            apiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(
                            (@NonNull Status status) -> hideProgressDialog()
                    );
                }

                @Override
                public void onConnectionSuspended(int i) {
                    hideProgressDialog();
                }
            });
            showProgressDialog();
            apiClient.connect();
        }
    }

    private void initLinks() {
        //todo change to https (when move testing to prod server?)
        FB_URL = "https://www.facebook.com/dialog/oauth?client_id=1692270867652630&response_type=token&scope=public_profile,email,user_friends&display=popup&redirect_uri=" + ApiFactory.getHostName(this) + "/fbOauthDone.php?mobile=true";

    }

    private void initTransitions() {
        if (Build.VERSION.SDK_INT > 21) {
            getWindow().setExitTransition(new Slide(Gravity.START));
            getWindow().setEnterTransition(new Slide(Gravity.END));
        }
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

    private void deletedOldAccount() {
        final AccountManager am = AccountManager.get(this);
        // TODO change account
        // temporary we remove function to change accounts
        // delete old account
        Account oldAccount = EvendateAccountManager.getSyncAccount(this);
        if (oldAccount != null)
            am.removeAccount(oldAccount, null, null);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.auth_loading));
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
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        if (serviceDialog != null)
            serviceDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    @SuppressWarnings("unused")
    @OnClick({R.id.sing_in_vk_button, R.id.sing_in_fb_button, R.id.sing_in_google_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sing_in_vk_button:
                VKSdk.login(this, VKScope.FRIENDS, VKScope.EMAIL, VKScope.WALL, VKScope.OFFLINE, VKScope.PAGES, VKScope.PHOTOS, VKScope.GROUPS);
                break;
            case R.id.sing_in_fb_button:
                startWebAuth(FB_URL);
                break;
            case R.id.sing_in_google_button:
                if (isGoogleServicesAvailable) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                    startActivityForResult(signInIntent, REQUEST_SIGN_IN);
                } else {
                    startWebAuth(getGoogleUrl(this));
                }
        }
    }

    private void startWebAuth(String Url) {
        Intent intent = new Intent(this, WebAuthActivity.class);
        intent.putExtra(URL_KEY, Url);
        startActivityForResult(intent, REQUEST_WEB_AUTH);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServicesExists() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                serviceDialog = apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
                serviceDialog.show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                onVkTokenReceived(res);
            }

            @Override
            public void onError(VKError error) {
                onErrorOccurred();
            }
        }))

            if (requestCode == REQUEST_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
            }
        if (requestCode == REQUEST_WEB_AUTH) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(WebAuthActivity.EMAIL);
                String token = data.getStringExtra(WebAuthActivity.TOKEN);
                onTokenReceived(email, token);
            } else {
                onErrorOccurred();
            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result == null) {
            onErrorOccurred();
            return;
        }
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        GoogleSignInAccount acct = result.getSignInAccount();
        if (result.isSuccess() && acct != null) {
            new RetrieveGoogleTokenTask().execute(acct.getEmail());
        } else {
            if (result.getStatus().isCanceled())
                onErrorOccurred();
        }
    }

    private void onGoogleTokenReceived(String token) {
        String url = ApiFactory.getHostName(this) + "/oAuthDone.php?mobile=true&type=google&token_type=Bearer&expires_in=3600&" +
                "authuser=0&session_state=49f4dc4624058e6cd300e7ec1c42331c52f1a97b..fb18&prompt=none&access_token=";
        url += token;
        startWebAuth(url);
    }

    private void onVkTokenReceived(VKAccessToken res) {
        String url = ApiFactory.getHostName(this) + "/oAuthDone.php?mobile=true&type=vk&expires_in=" + res.expiresIn + "&user_id=" + res.userId +
                "&access_token=" + res.accessToken + "&email=" + res.email + "&secret=" + res.secret;
        startWebAuth(url);
    }

    private void onErrorOccurred() {
        Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show();
    }

    public void onTokenReceived(String email, String token) {

        final AccountManager manager = AccountManager.get(this);
        String accountType = getResources().getString(R.string.account_type);
        Account account = new Account(email, accountType);

        final Bundle result = new Bundle();
        if (manager.getAccountsByType(getString(R.string.account_type)).length > 0) {
            manager.removeAccount(manager.getAccountsByType(getString(R.string.account_type))[0], new AccountManagerCallback<Boolean>() {
                @Override
                public void run(AccountManagerFuture<Boolean> future) {
                    if (future.isDone()) {
                        if (manager.addAccountExplicitly(account, "", new Bundle())) {
                            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                            result.putString(AccountManager.KEY_AUTHTOKEN, token);
                            manager.setAuthToken(account, account.type, token);
                            EvendateAccountManager.setActiveAccountName(getBaseContext(), account.name);
                            setAccountAuthenticatorResult(result);
                            setResult(RESULT_OK);
                            finish();
                            Log.i(LOG_TAG, "Account added. Auth done");
                            return;
                        }
                    }
                    result.putString(AccountManager.KEY_ERROR_MESSAGE, getString(R.string.auth_account_already_exists));
                    setResult(RESULT_CANCELED);
                    finish();
                    Log.e(LOG_TAG, "Auth error");
                }
            }, new Handler());
        } else if (manager.addAccountExplicitly(account, "", new Bundle())) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, token);
            manager.setAuthToken(account, account.type, token);
            EvendateAccountManager.setActiveAccountName(this, account.name);
            setAccountAuthenticatorResult(result);
            setResult(RESULT_OK);
            finish();
            Log.i(LOG_TAG, "Account added. Auth done");
        } else {
            result.putString(AccountManager.KEY_ERROR_MESSAGE, getString(R.string.auth_account_already_exists));
            setResult(RESULT_CANCELED);
            finish();
            Log.e(LOG_TAG, "Auth error");
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        onErrorOccurred();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EvendateAccountManager.setAuthDone(this);
    }

    private class RetrieveGoogleTokenTask extends AsyncTask<String, Void, String> {

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
}
