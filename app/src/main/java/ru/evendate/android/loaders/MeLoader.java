package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by Dmitry on 24.02.2016.
 */
public class MeLoader extends AbstractLoader<UserDetail> {
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();

    public MeLoader(Context context) {
        super(context);
    }
    public void getData(){
        Log.d(LOG_TAG, "getting me");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<UserDetail>> call =
                evendateService.getMe(peekToken(), UserDetail.FIELDS_LIST);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<UserDetail>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<UserDetail>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData().get(0));
                } else {
                    if(response.code() == 401)
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