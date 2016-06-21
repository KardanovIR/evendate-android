package ru.evendate.android.loaders;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import retrofit.Call;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.authorization.AuthActivity;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;

/**
 * Created by Dmitry on 04.02.2016.
 */
public abstract class AbstractLoader<D> {
    private final String LOG_TAG = AbstractLoader.class.getSimpleName();
    protected Context mContext;
    private LoaderListener<ArrayList<D>> mListener;
    protected Call mCall;
    private boolean isStopped = true;

    public AbstractLoader(Context context) {
        mContext = context;
    }

    public void setLoaderListener(LoaderListener<ArrayList<D>> listener) {
        this.mListener = listener;
    }

    protected String peekToken() {
        AccountManager accountManager = AccountManager.get(mContext);
        String token = null;
        try {
            token = accountManager.peekAuthToken(EvendateAccountManager.getSyncAccount(mContext),
                    mContext.getString(R.string.account_type));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error with peeking token");
            e.fillInStackTrace();
            mListener.onError();
        }
        if (token == null) {
            mContext.startActivity(new Intent(mContext, AuthActivity.class));
        }
        return token;
    }

    protected void invalidateToken() {
        AccountManager accountManager = AccountManager.get(mContext);
        accountManager.invalidateAuthToken(mContext.getString(R.string.account_type), peekToken());
    }

    public final void startLoading() {
        isStopped = false;
        onStartLoading();
    }

    protected abstract void onStartLoading();

    public void cancelLoad() {
        isStopped = true;
        if (mCall == null)
            return;
        mCall.cancel();
    }

    protected void onError() {
        if (!isStopped)
            mListener.onError();
    }

    protected void onLoaded(ArrayList<D> data) {
        isStopped = true;
        mListener.onLoaded(data);
    }

    public void onFailure(Throwable t) {
        Log.e(LOG_TAG, t.getMessage());
        onError();
    }

    protected ApiService getEvendateService() {
        return ApiFactory.getEvendateService();
    }
}
