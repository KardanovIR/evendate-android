package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.Event;
import ru.evendate.android.sync.EvendateServiceResponse;

/**
 * Created by ds_gordeev on 14.03.2016.
 */
public class LikeEventLoader extends AbstractLoader<Void> implements Callback<EvendateServiceResponse> {
    private final String LOG_TAG = LikeEventLoader.class.getSimpleName();
    Event mEvent;
    boolean favorite;

    public LikeEventLoader(Context context, Event event, boolean favorite) {
        super(context);
        this.favorite = favorite;
        mEvent = event;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onStartLoading() {
        Log.d(LOG_TAG, "performing like");
        if (favorite) {
            mCall = getEvendateService().eventDeleteFavorite(mEvent.getEntryId(), peekToken());
        } else {
            mCall = getEvendateService().eventPostFavorite(mEvent.getEntryId(), peekToken());
        }
        mCall.enqueue(this);
    }

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
}