package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.Event;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponse;

/**
 * Created by ds_gordeev on 14.03.2016.
 */
public class LikeEventLoader extends AbstractLoader<Void> {
    private final String LOG_TAG = LikeEventLoader.class.getSimpleName();
    Event mEvent;
    boolean favorite;

    public LikeEventLoader(Context context, Event event, boolean favorite) {
        super(context);
        this.favorite = favorite;
        mEvent = event;
    }

    public void load() {
        Log.d(LOG_TAG, "performing like");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        Call<EvendateServiceResponse> call;
        if (favorite) {
            call = evendateService.eventDeleteFavorite(mEvent.getEntryId(), peekToken());
        } else {
            call = evendateService.eventPostFavorite(mEvent.getEntryId(), peekToken());
        }
        mCall = call;
        call.enqueue(new Callback<EvendateServiceResponse>() {
            @Override
            public void onResponse(Response<EvendateServiceResponse> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    Log.d(LOG_TAG, "performed like");
                } else {
                    Log.e(LOG_TAG, "Error with response with like");
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