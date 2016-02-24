package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.OrganizationModel;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponse;

/**
 * Created by Dmitry on 24.02.2016.
 * performing subscribe
 */
public class SubOrganizationLoader extends AbstractLoader<Void> {
    private final String LOG_TAG = SubOrganizationLoader.class.getSimpleName();
    OrganizationModel mOrganization;
    boolean subscribe;
    public SubOrganizationLoader(Context context, OrganizationModel organizationModel,
                                 boolean subscribe) {
        super(context);
        this.subscribe = subscribe;
        mOrganization = organizationModel;
    }

    public void execute(){
        Log.d(LOG_TAG, "performing sub");
        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        Call<EvendateServiceResponse> call;
        if(subscribe){
            call = evendateService.organizationDeleteSubscription(mOrganization.getEntryId(), peekToken());
        } else {
            call = evendateService.organizationPostSubscription(mOrganization.getEntryId(), peekToken());
        }

        call.enqueue(new Callback<EvendateServiceResponse>() {
            @Override
            public void onResponse(Response<EvendateServiceResponse> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    Log.d(LOG_TAG, "performed sub");
                } else {
                    Log.e(LOG_TAG, "Error with response with organization sub");
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