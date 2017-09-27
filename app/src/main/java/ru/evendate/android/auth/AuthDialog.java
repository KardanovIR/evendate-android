package ru.evendate.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ResponseObject;

import static android.app.Activity.RESULT_OK;

/**
 * Created by dmitry on 07.09.17.
 */

public class AuthDialog extends DialogFragment implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String LOG_TAG = AuthDialog.class.getSimpleName();
    private static final String GOOGLE_SCOPE = "oauth2:email profile https://www.googleapis.com/auth/plus.login";
    private static final int REQ_SIGN_IN_REQUIRED = 55664;
    static public String URL_KEY = "url";
    private static final int REQUEST_GOOGLE_SIGN_IN = 101;
    private static final int REQUEST_WEB_AUTH = 102;
    private GoogleApiClient apiClient;
    private boolean isGoogleServicesAvailable = false;
    private ProgressDialog mProgressDialog;
    private Unbinder unbinder;
    private Disposable mDisposable;
    private AuthUrls mAuthUrls;
    private CallbackManager callbackManager;
    /**
     * we can receive many requests from one activity and all of they will reload data
     */
    private List<AuthListener> mAuthListeners = new ArrayList<>();

    public static String getGoogleUrl(Context context) {
        //if(mAuthUrls != null)
        //    return mAuthUrls.getGoogle();
        return "https://accounts.google.com/o/oauth2/auth?scope=email profile https://www.googleapis.com/auth/plus.login &redirect_uri="
                + ApiFactory.getHttpsHostName(context) + "/redirectOauth.php?mobile=true%26type=google&response_type=token&client_id=403640417782-lfkpm73j5gqqnq4d3d97vkgfjcoebucv.apps.googleusercontent.com";
    }

    public void setAuthListener(AuthListener listener) {
        mAuthListeners.add(listener);
    }

    public void unregisterAllListeners() {
        mAuthListeners.clear();
    }

    public Observable<String> getAuthObservable() {
        return Observable.create((ObservableEmitter<String> e) ->
                mAuthListeners.add(new AuthListener() {
                    @Override
                    public void OnAuthDone(String token) {
                        e.onNext(token);
                        e.onComplete();
                    }

                    @Override
                    public void OnAuthSkipped() {
                        e.onComplete();
                    }
                })
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGoogleApiClient();
        initFbLoginManager();
    }

    private void loadAuthUrls() {
        Observable<ResponseObject<AuthUrls>> observable =
                ApiFactory.getService(getContext()).getAuthUrls();

        mDisposable = observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result.isOk()) {
                                mAuthUrls = result.getData();
                            }
                        },
                        (Throwable error) -> Log.e(LOG_TAG, "auth urls get error")
                );
    }

    private void initGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN), new Scope(Scopes.EMAIL), new Scope(Scopes.PROFILE))
                .build();
        apiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity(),
                        new Random(Calendar.getInstance().getTimeInMillis()).nextInt(Integer.MAX_VALUE),
                        this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void initFbLoginManager() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        onFbTokenReceived(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.i(LOG_TAG, "FB retrieve token canceled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.i(LOG_TAG, "FB retrieve token error");
                        Log.e(LOG_TAG, exception.getLocalizedMessage());
                        onErrorOccurred();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (checkPlayServicesExists()) {
            isGoogleServicesAvailable = true;
            connectGoogleApiClient();
        }
        //        if (mAuthUrls == null) {
        //            loadAuthUrls();
        //        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK.
     * Don't show an update dialog cause google client initialization has already done it!
     */
    private boolean checkPlayServicesExists() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(LOG_TAG, "This device is not supported or have old version of apis.");
            return false;
        }
        return true;
    }

    private void connectGoogleApiClient() {
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

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.e(LOG_TAG, "onConnectionFailed:" + connectionResult);
        onErrorOccurred();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
        apiClient.disconnect();
        apiClient.stopAutoManage(getActivity());
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        if (mDisposable != null)
            mDisposable.dispose();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(LOG_TAG, "onDestroyView");
        unbinder.unbind();
    }


    @SuppressWarnings("unused")
    @OnClick({R.id.sing_in_vk_button, R.id.sing_in_fb_button, R.id.sing_in_google_button, R.id.auth_skip_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sing_in_vk_button:
                VKSdk.login(getActivity(), VKScope.FRIENDS, VKScope.EMAIL, VKScope.WALL,
                        VKScope.OFFLINE, VKScope.PAGES, VKScope.PHOTOS, VKScope.GROUPS);
                break;
            case R.id.sing_in_fb_button:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));
                break;
            case R.id.sing_in_google_button:
                if (isGoogleServicesAvailable) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                    startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN);
                } else {
                    startWebAuth(getGoogleUrl(getContext()));
                }
                break;
            case R.id.auth_skip_button:
                for (AuthListener listener : mAuthListeners) {
                    listener.OnAuthSkipped();
                }
                dismiss();
                break;
        }
    }

    private void startWebAuth(String Url) {
        Intent intent = new Intent(getContext(), WebAuthActivity.class);
        intent.putExtra(URL_KEY, Url);
        startActivityForResult(intent, REQUEST_WEB_AUTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                onVkTokenReceived(res);
                Log.i(LOG_TAG, "got VK response");
            }

            @Override
            public void onError(VKError error) {
                if (error.errorCode == VKError.VK_CANCELED)
                    return;
                Log.e(LOG_TAG, "VK get token error");
                onErrorOccurred();
            }
        });

        if (requestCode == REQUEST_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
        if (requestCode == REQUEST_WEB_AUTH) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(WebAuthActivity.EMAIL);
                String token = data.getStringExtra(WebAuthActivity.TOKEN);
                onTokenReceived(email, token);
            } else {
                if (data != null) {
                    boolean backPressed = data.getBooleanExtra(WebAuthActivity.BACK_PRESSED, false);
                    if (backPressed)
                        return;
                }
                Log.e(LOG_TAG, "WebAuth didn't return result ok");
                onErrorOccurred();
            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result == null) {
            Log.e(LOG_TAG, "Google sign in error");
            onErrorOccurred();
            return;
        }
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        GoogleSignInAccount acct = result.getSignInAccount();
        if (result.isSuccess() && acct != null) {
            new AuthDialog.RetrieveGoogleTokenTask().execute(acct.getAccount());
        } else {
            if (result.getStatus().isCanceled()) {
                Log.i(LOG_TAG, "Google retrieve token canceled");
            }
            if (result.getStatus().isInterrupted()) {
                Log.e(LOG_TAG, "Google retrieve token error");
                onErrorOccurred();
            }
        }
    }

    private void onGoogleTokenReceived(String token) {
        String url = ApiFactory.getHostName(getContext()) + "/oAuthDone.php?mobile=true&type=google&token_type=Bearer&expires_in=3600&" +
                "authuser=0&session_state=49f4dc4624058e6cd300e7ec1c42331c52f1a97b..fb18&prompt=none&access_token=";
        url += token;
        startWebAuth(url);
    }

    private void onVkTokenReceived(VKAccessToken res) {
        String url = ApiFactory.getHostName(getContext()) + "/oAuthDone.php?mobile=true&type=vk&expires_in=" + res.expiresIn + "&user_id=" + res.userId +
                "&access_token=" + res.accessToken + "&email=" + res.email + "&secret=" + res.secret;
        startWebAuth(url);
    }

    private void onFbTokenReceived(AccessToken accessToken) {
        String url = ApiFactory.getHostName(getContext()) +
                "/oAuthDone.php?mobile=true&type=facebook&access_token=" + accessToken.getToken() +
                "&expires_in=" + accessToken.getExpires().getTime() / 1000;
        startWebAuth(url);
    }

    private void onErrorOccurred() {
        Toast.makeText(getContext(), R.string.auth_error, Toast.LENGTH_SHORT).show();
    }

    private void onTokenReceived(String email, String token) {

        final AccountManager manager = AccountManager.get(getContext());
        String accountType = getResources().getString(R.string.account_type);
        Account account;
        try {
            account = new Account(email, accountType);
        } catch (Exception e) {
            onErrorOccurred();
            Log.e(LOG_TAG, "Empty account info received");
            return;
        }

        EvendateAccountManager.deleteAllAppAccounts(getContext());

        if (manager.addAccountExplicitly(account, "", new Bundle())) {
            manager.setAuthToken(account, account.type, token);
            EvendateAccountManager.setActiveAccountName(getContext(), account.name);
            Log.i(LOG_TAG, "Account added. Auth done");
        } else {
            Log.e(LOG_TAG, "Auth error");
        }
        for (AuthListener listener : mAuthListeners) {
            Log.e(LOG_TAG, "AUTH LISTENER != null");
            listener.OnAuthDone(token);
        }
        dismiss();
    }

    private class RetrieveGoogleTokenTask extends AsyncTask<Account, Void, String> {

        @Override
        protected String doInBackground(Account... params) {
            Account account = params[0];
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getContext(), account, GOOGLE_SCOPE);
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
            if (token != null)
                onGoogleTokenReceived(token);
        }
    }

    public interface AuthListener {
        void OnAuthDone(String token);

        void OnAuthSkipped();
    }
}
