package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.OrganizationType;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class CatalogLoader extends AbstractLoader<ArrayList<OrganizationType>> {
    private final String LOG_TAG = CatalogLoader.class.getSimpleName();

    public CatalogLoader(Context context) {
        super(context);
    }

    public void getData() {
        Log.d(LOG_TAG, "getting catalog");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<OrganizationType>> call =
                evendateService.getCatalog(peekToken(), OrganizationType.FIELDS_LIST);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<OrganizationType>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<OrganizationType>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData());
                } else {
                    if (response.code() == 401)
                        invalidateToken();
                    Log.e(LOG_TAG, "Error with response with catalog");
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
