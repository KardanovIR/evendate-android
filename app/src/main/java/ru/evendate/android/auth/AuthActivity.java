package ru.evendate.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Fade;
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

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    public final String APP_PREF = "evendate_pref";
    public final String FIRST_RUN = "first_run";
    private boolean introViewed = false;

    GoogleApiClient apiClient;
    private ProgressDialog mProgressDialog;

    @Bind(R.id.sing_in_google_button) View googleButton;
    private Dialog serviceDialog;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EvendateAccountManager.setAuthRunning(this);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        initTransitions();
        apiClient = initGoogleApiClient();
        deletedOldAccount();
    }

    private void initTransitions(){
        if(Build.VERSION.SDK_INT > 21){
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

    private void deletedOldAccount(){
        final AccountManager am = AccountManager.get(this);
        // TODO change account
        // temporary we remove function to change accounts
        // delete old account
        Account oldAccount = EvendateAccountManager.getSyncAccount(this);
        if (oldAccount != null)
            am.removeAccount(oldAccount, null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(checkPlayServicesExists()) {
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
        if(!introViewed)
            startActivityForResult(new Intent(this, IntroActivity.class), REQUEST_INTRO);
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
        if(mProgressDialog != null)
            mProgressDialog.dismiss();
        if(serviceDialog != null)
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
                startWebAuth(VK_URL);
                break;
            case R.id.sing_in_fb_button:
                startWebAuth(FB_URL);
                break;
            case R.id.sing_in_google_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                startActivityForResult(signInIntent, REQUEST_SIGN_IN);
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
                serviceDialog.setOnCancelListener(
                        (DialogInterface dialogInterface) -> setGoogleInactive()
                );
                serviceDialog.setOnDismissListener(
                        (DialogInterface dialogInterface) -> setGoogleInactive()
                );
                serviceDialog.show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
    private void setGoogleInactive(){
        googleButton.setClickable(false);
        googleButton.setEnabled(false);
        googleButton.setBackground(getResources().getDrawable(R.drawable.auth_google_inactive));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
        if (requestCode == REQUEST_INTRO) {
            if(resultCode == RESULT_CANCELED) {
                finish();
            }
            introViewed = true;
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
            new RetrieveGoogleTokenTask().execute(acct.getEmail());
        } else {
            if(result.getStatus().isCanceled())
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }
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

    private void onGoogleTokenReceived(String token) {
        String url = ApiFactory.HOST_NAME + "/oAuthDone.php?mobile=true&type=google&token_type=Bearer&expires_in=3600&" +
                "authuser=0&session_state=49f4dc4624058e6cd300e7ec1c42331c52f1a97b..fb18&prompt=none&access_token=";
        url += token;
        startWebAuth(url);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EvendateAccountManager.setAuthDone(this);
    }
}
