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
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserLoader extends AbstractLoader<UserDetail> {
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();

    public UserLoader(Context context) {
        super(context);
    }

    public void getData(int userId) {
        Log.d(LOG_TAG, "getting user");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<UserDetail>> call =
                evendateService.getUser(peekToken(), userId, UserDetail.FIELDS_LIST);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<UserDetail>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<UserDetail>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData().get(0));
                } else {
                    if (response.code() == 401)
                        invalidateToken();
                    // error response, no access to resource?
                    Log.e(LOG_TAG, "Error with response with user");
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