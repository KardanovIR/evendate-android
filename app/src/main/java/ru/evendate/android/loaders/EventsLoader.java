package ru.evendate.android.loaders;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;
import ru.evendate.android.ui.ReelFragment;

/**
 * Created by ds_gordeev on 11.03.2016.
 * downloading events from server
 */
public class EventsLoader extends AbstractLoader<ArrayList<EventFeed>>{
    private final String LOG_TAG = EventsLoader.class.getSimpleName();
    private int type;
    private int organizationId;
    private Date mDate;

    public EventsLoader(Context context, int type) {
        super(context);
        this.type = type;
    }
    public EventsLoader(Context context, int type, int organizationId) {
        super(context);
        this.type = type;
        this.organizationId = organizationId;
    }
    public EventsLoader(Context context, int type, Date date) {
        super(context);
        this.type = type;
        mDate = date;
    }

    public void getData(){
        Log.d(LOG_TAG, "getting events");
        onStartLoading();
        EvendateService evendateService = EvendateApiFactory.getEvendateService();

        Call<EvendateServiceResponseArray<EventDetail>> call;
        if(type == ReelFragment.TypeFormat.FAVORITES.type()){
            call = evendateService.getFavorite(peekToken(), true, EventFeed.FIELDS_LIST);
        }else if(type == ReelFragment.TypeFormat.ORGANIZATION.type()){
            call = evendateService.getEvents(peekToken(), organizationId, true, EventFeed.FIELDS_LIST);
        }else{
            if(mDate != null){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                call = evendateService.getFeed(peekToken(), dateFormat.format(mDate), true, EventFeed.FIELDS_LIST);
            }
            else{
                call = evendateService.getFeed(peekToken(), true, EventFeed.FIELDS_LIST);
            }
        }
        mCall = call;

        call.enqueue(new Callback<EvendateServiceResponseArray<EventDetail>>() {
            @Override
            public void onResponse(Response<EvendateServiceResponseArray<EventDetail>> response,
                                   Retrofit retrofit) {
                if (response.isSuccess()) {
                    onLoaded(new ArrayList<EventFeed>(response.body().getData()));
                } else {
                    Log.e(LOG_TAG, "Error with response with events");
                    onError();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Error", t.getMessage());
                onError();
            }
        });
    }
}
