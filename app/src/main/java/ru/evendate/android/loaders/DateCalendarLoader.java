package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.DateCalendar;
import ru.evendate.android.network.ResponseArray;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class DateCalendarLoader extends AbstractLoader<DateCalendar> implements
        Callback<ResponseArray<DateCalendar>> {
    private final String LOG_TAG = DateCalendarLoader.class.getSimpleName();

    public DateCalendarLoader(Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onStartLoading() {
        Log.d(LOG_TAG, "getting calendar dates");
        mCall = getEvendateService().getCalendarDates(peekToken(), true, true, true, null);
        mCall.enqueue(this);
    }

    @Override
    public void onResponse(Response<ResponseArray<DateCalendar>> response,
                           Retrofit retrofit) {
        if (response.isSuccess()) {
            onLoaded(response.body().getData());
        } else {
            if (response.code() == 401)
                invalidateToken();
            Log.e(LOG_TAG, "Error with response with calendar dates");
            onError();
        }
    }
}
