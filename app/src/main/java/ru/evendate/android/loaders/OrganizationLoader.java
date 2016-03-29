package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by Dmitry on 02.02.2016.
 */
public class OrganizationLoader extends AbstractLoader<OrganizationDetail> {
    private final String LOG_TAG = OrganizationLoader.class.getSimpleName();

    public OrganizationLoader(Context context) {
        super(context);
    }

    public void getOrganization(int organizationId) {
        Log.d(LOG_TAG, "getting organization " + organizationId);
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<OrganizationFull>> call =
                evendateService.getOrganization(peekToken(), organizationId, OrganizationDetail.FIELDS_LIST);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<OrganizationFull>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<OrganizationFull>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData().get(0));
                } else {
                    if (response.code() == 401)
                        invalidateToken();
                    Log.e(LOG_TAG, "Error with response with events");
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