package ru.evendate.android.loaders;

import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;
import ru.evendate.android.sync.models.OrganizationModel;

/**
 * Created by ds_gordeev on 01.02.2016.
 *//**
 * download subs from server
 */
public class SubscriptionLoader{
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();
    private Context mContext;
    private SubscriptionLoaderListener mListener;

    public SubscriptionLoader(Context context) {
        mContext = context;
    }

    public void setSubscriptionLoaderListener(SubscriptionLoaderListener listener) {
        this.mListener = listener;
    }

    public void getSubscriptions(){
        Log.d(LOG_TAG, "getting subs");
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        AccountManager accountManager = AccountManager.get(mContext);
        String token;
        try {
            token = accountManager.peekAuthToken(EvendateAccountManager.getSyncAccount(mContext),
                    mContext.getString(R.string.account_type));
        } catch (Exception e){
            Log.d(LOG_TAG, "Error with peeking token");
            e.fillInStackTrace();
            mListener.onError();
            return;
        }
        Call<EvendateServiceResponseArray<OrganizationModel>> call = evendateService.subscriptionData(token);
        call.enqueue(new Callback<EvendateServiceResponseArray<OrganizationModel>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<OrganizationModel>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    mListener.onLoaded(response.body().getData());
                } else {
                    // error response, no access to resource?
                    Log.e(LOG_TAG, "Error with response with subs");
                    mListener.onError();
                }
            }

            // something went completely south (like no internet connection)
            @Override
            public void onFailure(Throwable t) {
                Log.e("Error", t.getMessage());
                mListener.onError();
            }
        });
    }

    public interface SubscriptionLoaderListener{
        void onLoaded(ArrayList<OrganizationModel> subList);
        void onError();
    }
}