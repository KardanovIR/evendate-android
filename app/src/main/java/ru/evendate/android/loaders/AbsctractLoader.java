package ru.evendate.android.loaders;

import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;

/**
 * Created by Dmitry on 04.02.2016.
 */
public abstract class AbsctractLoader<D> {
    private final String LOG_TAG = AbsctractLoader.class.getSimpleName();
    protected Context mContext;
    protected LoaderListener<D> mListener;

    public AbsctractLoader(Context context) {
        mContext = context;
    }
    public void setLoaderListener(LoaderListener<D> listener) {
        this.mListener = listener;
    }
    protected String peekToken(){
        AccountManager accountManager = AccountManager.get(mContext);
        String token = null;
        try {
            token = accountManager.peekAuthToken(EvendateAccountManager.getSyncAccount(mContext),
                    mContext.getString(R.string.account_type));
        } catch (Exception e){
            Log.e(LOG_TAG, "Error with peeking token");
            e.fillInStackTrace();
            mListener.onError();
        }
        return token;
    }
}
