package ru.evendate.android.ui;

/**
 * Created by Dmitry on 23.09.2015.
 */

import android.accounts.AccountManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;
import ru.evendate.android.sync.models.EventDetail;

/**
 * fragment containing a reel
 * used in calendar, main pager, detail organization activities
 * contain recycle view with cards for event list
 */
public class ReelFragment extends Fragment {
    private String LOG_TAG = ReelFragment.class.getSimpleName();

    private android.support.v7.widget.RecyclerView mRecyclerView;

    private EventsAdapter mAdapter;
    EventLoader mEventLoader = new EventLoader();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    boolean refreshingEnabled = false;

    /** organization id from detail organization */
    private int organizationId;
    static final String TYPE = "type";
    private int type = 0;
    /** selected date in calendar */
    private Date mDate;

    public enum TypeFormat {
        feed                (0),
        favorites           (1),
        organization        (2),
        //organizationSubscribed  (3),
        calendar  (4);

        TypeFormat(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }

    private OnEventsDataLoadedListener mDataListener;
    private ArrayList<OnRefreshListener> mRefreshListenerList;


    public static ReelFragment newInstance(int type, int organizationId, boolean enableRefreshing){
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.organizationId = organizationId;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }
    public static ReelFragment newInstance(int type, boolean enableRefreshing){
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }
    public static ReelFragment newInstance(int type, Date date, boolean enableRefreshing){
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.mDate = date;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }

    public void setDataListener(OnEventsDataLoadedListener dataListener) {
        this.mDataListener = dataListener;
    }
    public void setOnRefreshListener(OnRefreshListener refreshListener){
        if(mRefreshListenerList == null)
            mRefreshListenerList = new ArrayList<>();
        mRefreshListenerList.add(refreshListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reel, container, false);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accent),
                PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        if (savedInstanceState != null){
            type = savedInstanceState.getInt(TYPE);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh_layout);
        if(!refreshingEnabled)
            mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mEventLoader.getEvents();
                if(mRefreshListenerList != null){
                    for(OnRefreshListener listener : mRefreshListenerList){
                        listener.onRefresh();
                    }
                }
            }
        });
        mAdapter = new EventsAdapter(getActivity(), type);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        /**
         * listener that let using refresh on top of the event list
         */
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean enable = false;
                if (mRecyclerView != null && mRecyclerView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    // check if the top of the first item is visible
                    boolean verticalScrollOffset = mRecyclerView.computeVerticalScrollOffset() == 0;
                    // enabling or disabling the refresh layout
                    enable = verticalScrollOffset;
                }
                if(refreshingEnabled)
                    mSwipeRefreshLayout.setEnabled(enable);
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventLoader.getEvents();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE, type);
    }

    /**
     * downloading events from server
     */
    private class EventLoader{
        public void getEvents(){
            mSwipeRefreshLayout.setRefreshing(true);
            Log.d(LOG_TAG, "getting events");
            EvendateService evendateService = EvendateApiFactory.getEvendateService();

            AccountManager accountManager = AccountManager.get(getActivity());
            String token;
            try {
                token = accountManager.peekAuthToken(EvendateAccountManager.getSyncAccount(getActivity()),
                        getString(R.string.account_type));
            } catch (Exception e){
                Log.e(LOG_TAG, "Error with peeking token");
                e.fillInStackTrace();
                onError();
                return;
            }
            Call<EvendateServiceResponseArray<EventDetail>> call;
            if(type == TypeFormat.favorites.nativeInt){
                call = evendateService.getFavorite(token, EventDetail.FIELDS_LIST);
            }else if(type == TypeFormat.organization.nativeInt){
                call = evendateService.getEvents(token, organizationId, true, EventDetail.FIELDS_LIST);
            }else{
                if(mDate != null){
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.getDefault());
                    call = evendateService.getEvents(token, dateFormat.format(mDate), true, EventDetail.FIELDS_LIST);
                }
                else{
                    call = evendateService.getFeed(token, true, EventDetail.FIELDS_LIST);
                }
            }
            call.enqueue(new Callback<EvendateServiceResponseArray<EventDetail>>() {
                @Override
                public void onResponse(Response<EvendateServiceResponseArray<EventDetail>> response,
                                       Retrofit retrofit) {
                    if (response.isSuccess()) {
                        mAdapter.setEventList(response.body().getData());
                        onDownloaded();
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

    public ArrayList<EventDetail> getEventList() {
        return mAdapter.getEventList();
    }

    /**
     * notify about finishing a download
     */
    interface OnEventsDataLoadedListener{
        void onEventsDataLoaded();
    }

    /**
     *
     */
    interface OnRefreshListener{
        void onRefresh();
    }

    /**
     * handle date changing in calendar
     * @param mDate
     */
    public void setDate(Date mDate) {
        if(type != TypeFormat.calendar.nativeInt)
            return;
        this.mDate = mDate;
        mEventLoader.getEvents();
    }

    private void onDownloaded(){
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
        if(mDataListener != null)
            mDataListener.onEventsDataLoaded();
    }
    private void onError(){
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
    }
}