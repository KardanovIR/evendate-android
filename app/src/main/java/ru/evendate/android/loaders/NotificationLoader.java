package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;

/**
 * Created by Dmitry on 15.04.2016.
 */
public class NotificationLoader extends AbstractLoader<Void> {
    private final String LOG_TAG = NotificationLoader.class.getSimpleName();
    int eventId;
    String datetime;

    public NotificationLoader(Context context, int eventId, String datetime) {
        super(context);
        this.eventId = eventId;
        this.datetime = datetime;
    }

    protected void onStartLoading() {
        Log.d(LOG_TAG, "posting notification");
        ApiService evendateService = ApiFactory.getEvendateService();

        Call<Response> call =
                evendateService.setNotification(peekToken(), eventId, datetime);
        mCall = call;

        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(retrofit.Response<Response> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {

                } else {
                    if (response.code() == 401)
                        invalidateToken();
                    // error response, no access to resource?
                    Log.e(LOG_TAG, "Error with response posting notification");
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