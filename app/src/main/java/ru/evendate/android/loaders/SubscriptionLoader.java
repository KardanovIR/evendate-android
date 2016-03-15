package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.Organization;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by ds_gordeev on 01.02.2016.
 * download subs from server
 */
public class SubscriptionLoader extends AbstractLoader<ArrayList<Organization>> {
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();

    public SubscriptionLoader(Context context) {
        super(context);
    }
    public void getSubscriptions(){
        Log.d(LOG_TAG, "getting subs");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<Organization>> call =
                evendateService.getSubscriptions(peekToken());
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<Organization>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<Organization>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData());
                } else {
                    if(response.code() == 401)
                        invalidateToken();
                    // error response, no access to resource?
                    Log.e(LOG_TAG, "Error with response with subs");
                    onError();
                }
            }

            // something went completely south (like no internet connection)
            @Override
            public void onFailure(Throwable t) {
                Log.e("Error", t.getMessage());
                onError();
            }
        });
    }
}