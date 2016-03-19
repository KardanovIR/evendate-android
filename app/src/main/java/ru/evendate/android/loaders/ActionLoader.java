package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.Action;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class ActionLoader extends AbstractLoader<ArrayList<Action>> {
    private final String LOG_TAG = ActionLoader.class.getSimpleName();

    public ActionLoader(Context context) {
        super(context);
    }
    public void getData(int userId){
        Log.d(LOG_TAG, "getting actions");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<Action>> call =
                evendateService.getActions(peekToken(), userId, Action.FIELDS_LIST, Action.ORDER_BY);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<Action>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<Action>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData());
                } else {
                    if(response.code() == 401)
                        invalidateToken();
                    Log.e(LOG_TAG, "Error with response with actions");
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