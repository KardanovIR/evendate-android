package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class EventLoader extends AbstractLoader<EventDetail> {
    private final String LOG_TAG = EventLoader.class.getSimpleName();

    public EventLoader(Context context) {
        super(context);
    }

    public void getData(int eventId) {
        Log.d(LOG_TAG, "getting event " + eventId);
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<EventDetail>> call =
                evendateService.eventData(eventId, peekToken(), EventDetail.FIELDS_LIST);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<EventDetail>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<EventDetail>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(response.body().getData().get(0));
                } else {
                    if (response.code() == 401)
                        invalidateToken();
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
}
