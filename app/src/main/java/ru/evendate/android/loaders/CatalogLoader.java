package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.OrganizationType;
import ru.evendate.android.network.ResponseArray;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class CatalogLoader extends AbstractLoader<OrganizationType> implements
        Callback<ResponseArray<OrganizationType>> {
    private final String LOG_TAG = CatalogLoader.class.getSimpleName();

    public CatalogLoader(Context context) {
        super(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStartLoading() {
        Log.d(LOG_TAG, "getting catalog");
        mCall = getEvendateService().getCatalog(peekToken(), OrganizationType.FIELDS_LIST);
        mCall.enqueue(this);
    }

    @Override
    public void onResponse(Response<ResponseArray<OrganizationType>> response,
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
}
