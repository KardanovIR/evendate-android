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
import ru.evendate.android.sync.models.EventModel;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class EventLoader extends AbsctractLoader<EventModel>{
    private final String LOG_TAG = EventLoader.class.getSimpleName();

    public EventLoader(Context context) {
        super(context);
    }

    public void getData(int eventId){
        Log.d(LOG_TAG, "getting event");
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseAttr<EventModel>> call =
                evendateService.eventData(eventId, peekToken());

        call.enqueue(new Callback<EvendateServiceResponseAttr<EventModel>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseAttr<EventModel>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    mListener.onLoaded(response.body().getData());
                } else {
                    Log.e(LOG_TAG, "Error with response with events");
                    mListener.onError();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Error", t.getMessage());
                mListener.onError();
            }
        });
    }
}
