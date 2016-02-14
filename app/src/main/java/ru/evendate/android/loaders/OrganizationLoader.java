package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;
import ru.evendate.android.sync.models.OrganizationDetail;

/**
 * Created by Dmitry on 02.02.2016.
 */
public class OrganizationLoader extends AbstractLoader<OrganizationDetail> {
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();
    private LoaderListener<OrganizationDetail> mListener;

    public OrganizationLoader(Context context) {
        super(context);
    }

    public void setLoaderListener(LoaderListener<OrganizationDetail> listener) {
        this.mListener = listener;
    }

    public void getOrganization(int organizationId){
        Log.d(LOG_TAG, "getting organization");
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<OrganizationDetail>> call =
                evendateService.getOrganization(peekToken(), organizationId, OrganizationDetail.FIELDS_LIST);

        call.enqueue(new Callback<EvendateServiceResponseArray<OrganizationDetail>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<OrganizationDetail>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    mListener.onLoaded(response.body().getData().get(0));
                } else {
                    if(response.code() == 401)
                        invalidateToken();
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