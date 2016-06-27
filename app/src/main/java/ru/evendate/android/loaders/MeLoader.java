package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;

/**
 * Created by Dmitry on 24.02.2016.
 */
public class MeLoader extends AbstractLoader<UserDetail> {
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();

    public MeLoader(Context context) {
        super(context);
    }

    protected void onStartLoading() {
        Log.d(LOG_TAG, "getting me");
        ApiService apiService = ApiFactory.getEvendateService();

        Call<ResponseArray<UserDetail>> call =
                apiService.getMe(peekToken(), UserDetail.FIELDS_LIST);
        mCall = call;

        call.enqueue(new Callback<ResponseArray<UserDetail>>() {
            @Override
            public void onResponse(Response<ResponseArray<UserDetail>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData());
                } else {
                    if (response.code() == 401)
                        invalidateToken();
                    // error response, no access to resource?
                    Log.e(LOG_TAG, "Error with response with me");
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