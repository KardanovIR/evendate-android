package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseAttr;
import ru.evendate.android.sync.models.UserDetail;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserLoader extends AbstractLoader<UserDetail> {
    private final String LOG_TAG = SubscriptionLoader.class.getSimpleName();

    public UserLoader(Context context) {
        super(context);
    }
    public void getData(int userId){
        Log.d(LOG_TAG, "getting user");
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseAttr<UserDetail>> call =
                evendateService.getUser(peekToken(), userId, UserDetail.FIELDS_LIST);
        call.enqueue(new Callback<EvendateServiceResponseAttr<UserDetail>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseAttr<UserDetail>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    mListener.onLoaded(response.body().getData());
                } else {
                    if(response.code() == 401)
                        invalidateToken();
                    // error response, no access to resource?
                    Log.e(LOG_TAG, "Error with response with user");
                    mListener.onError();
                }
            }

            // something went completely south (like no internet connection)
            @Override
            public void onFailure(Throwable t) {
                Log.e("Error", t.getMessage());
                mListener.onError();
            }
        });
    }
}