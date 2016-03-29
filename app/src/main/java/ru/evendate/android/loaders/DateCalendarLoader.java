package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.DateCalendar;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;

/**
 * Created by Dmitry on 08.02.2016.
 */
public class DateCalendarLoader extends AbstractLoader<ArrayList<DateCalendar>> {
    private final String LOG_TAG = DateCalendarLoader.class.getSimpleName();

    public DateCalendarLoader(Context context) {
        super(context);
    }

    public void getData() {
        Log.d(LOG_TAG, "getting calendar dates");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        Call<EvendateServiceResponseArray<DateCalendar>> call =
                evendateService.getCalendarDates(peekToken(), true, true, true,
                        null);
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<DateCalendar>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<DateCalendar>> response,
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

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                onError();
            }
        });
    }
}
