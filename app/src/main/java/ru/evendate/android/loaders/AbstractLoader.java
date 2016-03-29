package ru.evendate.android.loaders;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import retrofit.Call;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.authorization.AuthActivity;

/**
 * Created by Dmitry on 04.02.2016.
 */
public abstract class AbstractLoader<D> {
    private final String LOG_TAG = AbstractLoader.class.getSimpleName();
    protected Context mContext;
    private LoaderListener<D> mListener;
    protected Call mCall;
    private boolean isCanceled;

    public AbstractLoader(Context context) {
        mContext = context;
    }

    public void setLoaderListener(LoaderListener<D> listener) {
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

    protected void onStartLoading() {
        isCanceled = false;
    }

    public void cancel() {
        if (mCall == null)
            return;
        mCall.cancel();
    }

    protected void onError() {
        if (!isCanceled)
            mListener.onError();
    }

    protected void onLoaded(D data) {
        isCanceled = true;
        mListener.onLoaded(data);
    }
}
