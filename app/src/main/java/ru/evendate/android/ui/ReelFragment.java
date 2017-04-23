package ru.evendate.android.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.views.LoadStateView;

/**
 * fragment containing a reel
 * used in calendar, main pager activities
 * contain recycle view with cards for event list
 */
public class ReelFragment extends Fragment implements AdapterController.AdapterContext, LoadStateView.OnReloadListener {
    static final String TYPE_KEY = "type";
    /**
     * organization id from detail organization
     */
    static final String ORG_KEY = "organization_id";
    /**
     * selected date in calendar
     */
    static final String DATE_KEY = "date";
    @Bind(R.id.recycler_view) android.support.v7.widget.RecyclerView mRecyclerView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;
    boolean refreshTurnOn = false;
    private String LOG_TAG = ReelFragment.class.getSimpleName();
    private EventsAdapter mAdapter;
    private AdapterController mAdapterController;
    private int type = 0;
    private int organizationId;
    private Date mDate;
    private OnEventsDataLoadedListener mDataListener;
    private ArrayList<OnRefreshListener> mRefreshListenerList;

    public static ReelFragment newInstance(ReelType type, int organizationId, boolean enableRefreshing) {
        ReelFragment reel = new ReelFragment();
        reel.type = type.type();
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
            type = savedInstanceState.getInt(TYPE_KEY);
            organizationId = savedInstanceState.getInt(ORG_KEY);
            mDate = new Date(savedInstanceState.getLong(DATE_KEY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reel, container, false);
        ButterKnife.bind(this, rootView);

        initRefresh();
        initRecyclerView();
        mAdapter = new EventsAdapter(getActivity(), mRecyclerView, type);
        mAdapterController = new AdapterController(this, mAdapter);
        mRecyclerView.setAdapter(mAdapter);
        setEmptyCap();
        mLoadStateView.setOnReloadListener(this);
        return rootView;
    }

    private void initRefresh() {
        if (!refreshTurnOn)
            mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            reloadEvents();
            if (mRefreshListenerList != null) {
                for (OnRefreshListener listener : mRefreshListenerList) {
                    listener.onRefresh();
                }
            }
        });
    }

    private void initRecyclerView() {
        int columnCount = getResources().getInteger(R.integer.feed_column_count);
        if (columnCount == 1) {
            mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }
        /*
          listener that let using refresh on top of the event list
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

        /*
            centering cards in tablets
         */
        switch (ReelType.getType(type)) {
            case CALENDAR:
                break;
            default:
                if (columnCount == 1) {
                    mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                        @Override
                        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                            int totalWidth = parent.getWidth();
                            int maxCardWidth = getResources().getDimensionPixelOffset(R.dimen.card_feed_max_width);
                            int sidePadding = (totalWidth - maxCardWidth) / 2;
                            sidePadding = Math.max(0, sidePadding);
                            outRect.set(sidePadding, 0, sidePadding, 0);
                        }
                    });
                }
                break;
        }
    }

    private void setEmptyCap() {
        switch (ReelType.getType(type)) {
            case FEED:
                mLoadStateView.setEmptyHeader(getString(R.string.list_feed_empty_header));
                mLoadStateView.setEmptyDescription(getString(R.string.list_feed_empty_text));
                break;
            case FAVORITES:
                mLoadStateView.setEmptyHeader(getString(R.string.list_favourites_empty_header));
                mLoadStateView.setEmptyDescription(getString(R.string.list_favourites_empty_text));
                break;
            case RECOMMENDATION:
                mLoadStateView.setEmptyHeader(getResources().getString(R.string.list_recommendation_empty_header));
                break;
            case ORGANIZATION:
            case ORGANIZATION_PAST:
                mLoadStateView.setEmptyHeader(getString(R.string.list_organization_empty_header));
                mLoadStateView.setEmptyDescription(getString(R.string.list_organization_empty_text));
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE_KEY, type);
        outState.putInt(ORG_KEY, organizationId);
        if (mDate != null)
            outState.putLong(DATE_KEY, mDate.getTime());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoadStateView.showProgress();
        loadEvents();
    }

    @Override
    public void onReload() {
        reloadEvents();
    }

    public EventsAdapter getAdapter() {
        return mAdapter;
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
        mAdapter.reset();
        reloadEvents();
    }

    private void loadEvents() {
        getDataObservable().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> onLoaded(new ArrayList<>(result.getData())),
                        this::onError,
                        mLoadStateView::hideProgress
                );
    }

    public void reloadEvents() {
        mLoadStateView.hide();
        mAdapterController.reset();
        getDataObservable().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> onReloaded(new ArrayList<>(result.getData())),
                        this::onError,
                        mLoadStateView::hideProgress
                );
    }

    private Observable<ResponseArray<Event>> getDataObservable() {
        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<ResponseArray<Event>> observable;

        final int length = mAdapterController.getLength();
        final int offset = mAdapterController.getOffset();

        switch (ReelType.getType(type)) {
            case FEED:
                observable = getFeed(apiService, length, offset);
                break;
            case FAVORITES:
                observable = getFavorite(apiService, length, offset);
                break;
            case ORGANIZATION:
                observable = getOrgEvent(apiService, length, offset, organizationId);
                break;
            case ORGANIZATION_PAST:
                observable = getOrgPastEvent(apiService, length, offset, organizationId);
                break;
            case CALENDAR:
                observable = getCalendarEvent(apiService, length, offset, mDate);
                break;
            case RECOMMENDATION:
                observable = getRecommendation(apiService, length, offset);
                break;
            default:
                throw new RuntimeException("unknown type");
        }
        return observable;
    }

    private Observable<ResponseArray<Event>> getFeed(ApiService apiService,
                                                     int length, int offset) {
        return apiService.getFeed(EvendateAccountManager.peekToken(getActivity()),
                true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, length, offset);
    }

    private Observable<ResponseArray<Event>> getFavorite(ApiService apiService,
                                                         int length, int offset) {
        return apiService.getFavorite(EvendateAccountManager.peekToken(getActivity()),
                true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, length, offset);
    }

    private Observable<ResponseArray<Event>> getOrgEvent(
            ApiService apiService, int length, int offset, int organizationId) {
        return apiService.getEvents(EvendateAccountManager.peekToken(getActivity()),
                organizationId, true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, length, offset);
    }

    private Observable<ResponseArray<Event>> getOrgPastEvent(
            ApiService apiService, int length, int offset, int organizationId) {
        String date = ServiceUtils.formatDateForServer(Calendar.getInstance().getTime());
        return apiService.getEvents(EvendateAccountManager.peekToken(getActivity()),
                organizationId, date, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_LAST_DATE, length, offset);
    }

    private Observable<ResponseArray<Event>> getCalendarEvent(ApiService apiService,
                                                              int length, int offset, Date date) {
        return apiService.getFeed(EvendateAccountManager.peekToken(getActivity()),
                ServiceUtils.formatDateRequestNotUtc(date), true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, length, offset);
    }

    private Observable<ResponseArray<Event>> getRecommendation(ApiService apiService,
                                                               int length, int offset) {
        return apiService.getRecommendations(EvendateAccountManager.peekToken(getActivity()),
                true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, length, offset);
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mSwipeRefreshLayout.setRefreshing(false);
        mLoadStateView.showErrorHint();
    }

    public void onLoaded(ArrayList<EventFeed> events) {
        mAdapterController.loaded(events);
        mSwipeRefreshLayout.setRefreshing(false);
        if (mDataListener != null)
            mDataListener.onEventsDataLoaded();
        checkListAndShowHint();
    }

    public void onReloaded(ArrayList<EventFeed> events) {
        mAdapterController.reloaded(events);
        mSwipeRefreshLayout.setRefreshing(false);
        checkListAndShowHint();
    }

    protected void checkListAndShowHint() {
        if (mAdapter.isEmpty())
            mLoadStateView.showEmptryHint();
    }

    @Override
    public void requestNext() {
        loadEvents();
        mLoadStateView.hide();
    }

    public enum ReelType {
        FEED(0),
        FAVORITES(1),
        ORGANIZATION(2),
        ORGANIZATION_PAST(5),
        CALENDAR(3),
        RECOMMENDATION(4);

        final int type;

        ReelType(int type) {
            this.type = type;
        }

        static public ReelType getType(int pType) {
            for (ReelType type : ReelType.values()) {
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

    /**
     * notify about finishing a download
     */
    public interface OnEventsDataLoadedListener {
        void onEventsDataLoaded();
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}