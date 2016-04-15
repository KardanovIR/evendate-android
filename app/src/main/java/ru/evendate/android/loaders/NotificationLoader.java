package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponse;

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
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponse> call =
                evendateService.setNotification(peekToken(), eventId, datetime);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponse>() {
            @Override
            public void onResponse(Response<EvendateServiceResponse> response,
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