package ru.evendate.android.ui;

/**
 * Created by Dmitry on 23.09.2015.
 */

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.adapters.EventsAdapter;
import ru.evendate.android.adapters.NpaLinearLayoutManager;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * fragment containing a reel
 * used in calendar, main pager activities
 * contain recycle view with cards for event list
 */
public class ReelFragment extends Fragment implements AdapterController.AdapterContext{
    private String LOG_TAG = ReelFragment.class.getSimpleName();

    @Bind(R.id.recyclerView) android.support.v7.widget.RecyclerView mRecyclerView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    boolean refreshTurnOn = false;
    private EventsAdapter mAdapter;
    private AdapterController mAdapterController;

    @Bind(R.id.tv_feed_header) TextView mFeedEmptyHeader;
    @Bind(R.id.tv_feed_emptyText) TextView mFeedEmptyTextView;
    @Bind(R.id.ll_feed_empty) LinearLayout mFeedEmptyLayout;

    static final String TYPE = "type";
    private int type = 0;
    /**
     * organization id from detail organization
     */
    private int organizationId;
    /**
     * selected date in calendar
     */
    private Date mDate;

    public enum ReelType {
        FEED(0),
        FAVORITES(1),
        ORGANIZATION(2),
        CALENDAR(3),
        RECOMMENDATION(4);

        final int type;

        ReelType(int type) {
            this.type = type;
        }

        static public ReelType getType(int pType) {
            for (ReelType type: ReelType.values()) {
                if (type.type() == pType) {
                    return type;
                }
            }
            throw new RuntimeException("unknown type");
        }
        public int type() {
            return type;
        }
    }

    private OnEventsDataLoadedListener mDataListener;
    private ArrayList<OnRefreshListener> mRefreshListenerList;


    public static ReelFragment newInstance(int type, int organizationId, boolean enableRefreshing) {
        ReelFragment reel = new ReelFragment();
        reel.type = type;
        reel.organizationId = organizationId;
        reel.refreshTurnOn = enableRefreshing;
        return reel;
    }

    public static ReelFragment newInstance(int type, boolean enableRefreshing) {
        ReelFragment reel = new ReelFragment();
        reel.type = type;
        reel.refreshTurnOn = enableRefreshing;
        return reel;
    }

    public static ReelFragment newInstance(int type, Date date, boolean enableRefreshing) {
        ReelFragment reel = new ReelFragment();
        reel.type = type;
        reel.mDate = date;
        reel.refreshTurnOn = enableRefreshing;
        return reel;
    }

    public void setDataListener(OnEventsDataLoadedListener dataListener) {
        this.mDataListener = dataListener;
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        if (mRefreshListenerList == null)
            mRefreshListenerList = new ArrayList<>();
        mRefreshListenerList.add(refreshListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            type = savedInstanceState.getInt(TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reel, container, false);
        ButterKnife.bind(this, rootView);

        mProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accent),
                PorterDuff.Mode.SRC_IN);

        initRefresh();
        initRecyclerView();
        mAdapter = new EventsAdapter(getActivity(), mRecyclerView, type);
        mAdapterController = new AdapterController(this, mAdapter);
        mRecyclerView.setAdapter(mAdapter);
        setCap();
        mProgressBar.setVisibility(View.VISIBLE);
        return rootView;
    }

    private void initRefresh() {
        if (!refreshTurnOn)
            mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                mAdapterController.reset();
                loadEvents();
                if (mRefreshListenerList != null) {
                    for (OnRefreshListener listener : mRefreshListenerList) {
                        listener.onRefresh();
                    }
                }
        });
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
        /**
         * listener that let using refresh on top of the event list
         */
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean enable = false;
                if (recyclerView.getChildCount() > 0) {
                    enable = recyclerView.computeVerticalScrollOffset() == 0;
                }
                if (refreshTurnOn)
                    mSwipeRefreshLayout.setEnabled(enable);
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new LandingAnimator());
    }

    private void setCap(){
        ReelType reelType = ReelType.getType(type);
        if (reelType == ReelType.FEED) {
            mFeedEmptyHeader.setText(getResources().getString(R.string.feed_empty_header));
            mFeedEmptyTextView.setText(getResources().getString(R.string.feed_empty_text));
        } else if (reelType == ReelType.FAVORITES) {
            mFeedEmptyHeader.setText(getResources().getString(R.string.favourites_empty_header));
            mFeedEmptyTextView.setText(getResources().getString(R.string.favourites_empty_text));
        } else if (reelType == ReelType.RECOMMENDATION) {
            mFeedEmptyHeader.setText(getResources().getString(R.string.recommendation_empty_header));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE, type);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadEvents();
    }

    public EventsAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * notify about finishing a download
     */
    public interface OnEventsDataLoadedListener {
        void onEventsDataLoaded();
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * handle date changing in calendar
     *
     * @param mDate selected date in calendar
     */
    public void setDateAndReload(Date mDate) {
        if (type != ReelType.CALENDAR.type())
            return;
        this.mDate = mDate;
        mAdapterController.reset();
        mAdapter.reset();
        loadEvents();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void loadEvents(){
        hideCap();
        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<EventDetail>> observable;

        final int length = mAdapterController.getLength();
        final int offset = mAdapterController.getOffset();

        switch (ReelType.getType(type)){
            case FEED: observable = getFeed(apiService, length, offset); break;
            case FAVORITES: observable = getFavorite(apiService, length, offset); break;
            case ORGANIZATION: observable = getOrgEvent(apiService, length, offset, organizationId); break;
            case CALENDAR: observable = getCalendarEvent(apiService, length, offset, mDate); break;
            case RECOMMENDATION: observable = getRecommendation(apiService, length, offset); break;
            default: throw new RuntimeException("unknown type");
        }

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    onLoaded(new ArrayList<>(result.getData()));
                }, error -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mProgressBar.setVisibility(View.GONE);
                    Log.e(LOG_TAG, "" + error.getMessage());
                }, () -> Log.i(LOG_TAG, "Complete!"));
    }

    private Observable<ResponseArray<EventDetail>> getFeed(ApiService apiService,
                                                                     int length, int offset){
        return apiService.getFeed(EvendateAccountManager.peekToken(getActivity()),
                true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_TIME, length, offset);
    }

    private Observable<ResponseArray<EventDetail>> getFavorite(ApiService apiService,
                                                               int length, int offset){
        return apiService.getFavorite(EvendateAccountManager.peekToken(getActivity()),
                true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_TIME, length, offset);
    }

    private Observable<ResponseArray<EventDetail>> getOrgEvent(
            ApiService apiService, int length, int offset, int organizationId){
        return apiService.getEvents(EvendateAccountManager.peekToken(getActivity()),
                organizationId, true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_TIME, length, offset);
    }

    private Observable<ResponseArray<EventDetail>> getCalendarEvent(ApiService apiService,
                                                                      int length, int offset, Date date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return apiService.getFeed(EvendateAccountManager.peekToken(getActivity()),
                dateFormat.format(date), true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_TIME, length, offset);
    }

    private Observable<ResponseArray<EventDetail>> getRecommendation(ApiService apiService,
                                                                     int length, int offset){
        return apiService.getRecommendations(EvendateAccountManager.peekToken(getActivity()),
                true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_TIME, length, offset);
    }

    public void onLoaded(ArrayList<EventFeed> events) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mAdapterController.reloaded(events);
        }
        else
            mAdapterController.loaded(events);
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
        if (mDataListener != null)
            mDataListener.onEventsDataLoaded();
        if (mAdapter.isEmpty()) {
            displayCap();
        }
    }

    @Override
    public void requestNext() {
        loadEvents();
    }

    private void displayCap(){
        mFeedEmptyLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void hideCap(){
        mFeedEmptyLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}