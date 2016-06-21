package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.Action;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class ActionLoader extends AbstractLoader<Action> implements Callback<ResponseArray<Action>> {
    private final String LOG_TAG = ActionLoader.class.getSimpleName();

    int userId;

    public ActionLoader(Context context, int userId) {
        super(context);
        this.userId = userId;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStartLoading() {
        Log.d(LOG_TAG, "getting actions");
        ApiService apiService = ApiFactory.getEvendateService();

        mCall = apiService.getActions(peekToken(), userId, Action.FIELDS_LIST, Action.ORDER_BY);
        mCall.enqueue(this);
    }

    @Override
    public void onResponse(Response<ResponseArray<Action>> response,
                           Retrofit retrofit) {
        if (response.isSuccess()) {
            onLoaded(response.body().getData());
        } else {
            if (response.code() == 401)
                invalidateToken();
            Log.e(LOG_TAG, "Error with response with actions");
            onError();
        }
    }
}