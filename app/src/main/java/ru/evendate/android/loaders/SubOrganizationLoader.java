package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;

/**
 * Created by Dmitry on 24.02.2016.
 * performing subscribe
 */
@Deprecated
public class SubOrganizationLoader extends AbstractLoader<Void> {
    private final String LOG_TAG = SubOrganizationLoader.class.getSimpleName();
    OrganizationDetail mOrganization;
    boolean subscribe;

    public SubOrganizationLoader(Context context, OrganizationDetail organization,
                                 boolean subscribe) {
        super(context);
        this.subscribe = subscribe;
        mOrganization = organization;
    }

    @Override
    protected void onStartLoading() {
        Log.d(LOG_TAG, "performing sub");
        ApiService apiService = ApiFactory.getEvendateService();
        Call<Response> call;
        if (subscribe) {
            call = apiService.organizationDeleteSubscription(mOrganization.getEntryId(), peekToken());
        } else {
            call = apiService.organizationPostSubscription(mOrganization.getEntryId(), peekToken());
        }
        mCall = call;

        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(retrofit.Response response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    Log.d(LOG_TAG, "performed sub");
                } else {
                    Log.e(LOG_TAG, "Error with response with organization sub");
                    onError();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                onError();
            }
        });
    }
}