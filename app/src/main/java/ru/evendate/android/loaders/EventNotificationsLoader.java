package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;

/**
 * downloading notifications for events from server
 */
public class EventNotificationsLoader extends AbstractLoader<EventNotification> implements Callback<ResponseArray<EventNotification>> {
    private final String LOG_TAG = EventNotificationsLoader.class.getSimpleName();
    private int eventId;
    private Date mDate;

    public EventNotificationsLoader(Context context, int eventId) {
        super(context);
        this.eventId = eventId;
    }



    @SuppressWarnings("unchecked")
    protected void onStartLoading() {
        Log.d(LOG_TAG, "getting events");
        ApiService evendateService = ApiFactory.getEvendateService();
        mCall = evendateService.getNotifications(peekToken(), eventId,
                EventNotification.FIELDS_LIST);
        mCall.enqueue(new Callback<ResponseArray<EventNotification>>() {
            @Override
            public void onResponse(Response<ResponseArray<EventNotification>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(new ArrayList<>(response.body().getData()));
                } else {
                    Log.e(LOG_TAG, "Error with response with events");
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


    @Override
    public void onResponse(Response<ResponseArray<EventNotification>> response, Retrofit retrofit) {

    }
}
