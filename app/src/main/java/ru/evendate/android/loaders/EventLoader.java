package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class EventLoader extends AbstractLoader<EventDetail>
        implements Callback<EvendateServiceResponseArray<EventDetail>> {
    private final String LOG_TAG = EventLoader.class.getSimpleName();

    int eventId;

    public EventLoader(Context context, int eventId) {
        super(context);
        this.eventId = eventId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onStartLoading() {
        Log.d(LOG_TAG, "getting event " + eventId);
        mCall = getEvendateService().eventData(eventId, peekToken(), EventDetail.FIELDS_LIST);
        mCall.enqueue(this);
    }

    @Override
    public void onResponse(Response<EvendateServiceResponseArray<EventDetail>> response,
                           Retrofit retrofit) {
        if (response.isSuccess()) {
            onLoaded(response.body().getData());
        } else {
            if (response.code() == 401)
                invalidateToken();
            Log.e(LOG_TAG, "Error with response with events");
            onError();
        }
    }
}
