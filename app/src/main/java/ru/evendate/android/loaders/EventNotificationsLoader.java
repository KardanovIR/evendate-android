package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;
import ru.evendate.android.ui.ReelFragment;

/**
 * Created by Ali Abdulmadzhidov on 16.05.2016.
 * downloading notifications for events from server
 */
public class EventNotificationsLoader extends AppendableLoader<EventNotification> implements Callback<EvendateServiceResponseArray<EventNotification>> {
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
        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        mCall = evendateService.getNotifications(peekToken(),eventId);
        mCall.enqueue(new Callback<EvendateServiceResponseArray<EventNotification>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<EventNotification>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(new ArrayList<EventNotification>(response.body().getData()));
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
    public void onResponse(Response<EvendateServiceResponseArray<EventNotification>> response, Retrofit retrofit) {

    }
}
