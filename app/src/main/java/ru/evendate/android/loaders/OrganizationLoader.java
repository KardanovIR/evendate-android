package ru.evendate.android.loaders;

import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseAttr;
import ru.evendate.android.sync.models.OrganizationModelWithEvents;

/**
 * Created by Dmitry on 02.02.2016.
 */
public class OrganizationLoader{
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();
    private Context mContext;
    private LoaderListener<OrganizationModelWithEvents> mListener;

    public OrganizationLoader(Context context) {
        mContext = context;
    }

    public void setLoaderListener(LoaderListener<OrganizationModelWithEvents> listener) {
        this.mListener = listener;
    }

    public void getOrganization(int organizationId){
        Log.d(LOG_TAG, "getting organization");
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        AccountManager accountManager = AccountManager.get(mContext);
        String token;
        try {
            token = accountManager.peekAuthToken(EvendateAccountManager.getSyncAccount(mContext),
                    mContext.getString(R.string.account_type));
        } catch (Exception e){
            Log.e(LOG_TAG, "Error with peeking token");
            e.fillInStackTrace();
            mListener.onError();
            return;
        }
        Call<EvendateServiceResponseAttr<OrganizationModelWithEvents>> call =
                evendateService.organizationWithEventsData(organizationId, token);

        call.enqueue(new Callback<EvendateServiceResponseAttr<OrganizationModelWithEvents>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseAttr<OrganizationModelWithEvents>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    mListener.onLoaded(response.body().getData());
                } else {
                    Log.e(LOG_TAG, "Error with response with events");
                    mListener.onError();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Error", t.getMessage());
                mListener.onError();
            }
        });
    }
}