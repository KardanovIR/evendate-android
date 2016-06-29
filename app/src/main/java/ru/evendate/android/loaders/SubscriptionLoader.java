package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.Organization;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;

/**
 * Created by ds_gordeev on 01.02.2016.
 * download subs from server
 */
public class SubscriptionLoader extends AbstractLoader<Organization> {
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();

    public SubscriptionLoader(Context context) {
        super(context);
    }

    protected void onStartLoading() {
        Log.d(LOG_TAG, "getting subs");
        ApiService apiService = ApiFactory.getEvendateService();

        Call<ResponseArray<Organization>> call =
                apiService.getSubscriptions(peekToken());
        mCall = call;

        call.enqueue(new Callback<ResponseArray<Organization>>() {
            @Override
            public void onResponse(Response<ResponseArray<Organization>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData());
                } else {
                    if (response.code() == 401)
                        invalidateToken();
                    // error response, no access to resource?
                    Log.e(LOG_TAG, "Error with response with subs");
                    onError();
                }
            }

            // something went completely south (like no internet connection)
            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                onError();
            }
        });
    }
}