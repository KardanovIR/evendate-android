package ru.evendate.android.ui;

/**
 * Created by Dmitry on 23.09.2015.
 */

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

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.adapters.EventsAdapter;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;
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
    boolean refreshingEnabled = false;
    private EventsAdapter mAdapter;
    private AdapterController mAdapterController;

    /**
     * organization id from detail organization
     */
    private int organizationId;
    static final String TYPE = "type";
    private int type = 0;
    /**
     * selected date in calendar
     */
    private Date mDate;

    public enum TypeFormat {
        FEED(0),
        FAVORITES(1),
        ORGANIZATION(2),
        //organizationSubscribed  (3),
        CALENDAR(4),
        RECOMENDATION(4);

        final int type;

        TypeFormat(int type) {
            this.type = type;
        }

        public int type() {
            return type;
        }
    }

    private OnEventsDataLoadedListener mDataListener;
    private ArrayList<OnRefreshListener> mRefreshListenerList;


    public static ReelFragment newInstance(int type, int organizationId, boolean enableRefreshing) {
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.organizationId = organizationId;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }

    public static ReelFragment newInstance(int type, boolean enableRefreshing) {
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }

    public static ReelFragment newInstance(int type, Date date, boolean enableRefreshing) {
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.mDate = date;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reel, container, false);
        ButterKnife.bind(this, rootView);

        mProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accent),
                PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            type = savedInstanceState.getInt(TYPE);
        }

        initRefresh();
        initRecyclerView();
        mAdapter = new EventsAdapter(getActivity(), mRecyclerView, type);
        mAdapterController = new AdapterController(this, mAdapter);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setRefreshing(true);
        loadEvents();
        return rootView;
    }

    private void initRefresh() {
        if (!refreshingEnabled)
            mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapterController.reset();
                loadEvents();
                if (mRefreshListenerList != null) {
                    for (OnRefreshListener listener : mRefreshListenerList) {
                        listener.onRefresh();
                    }
                }
            }
        });
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        /**
         * listener that let using refresh on top of the event list
         */
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean enable = false;
                if (recyclerView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    // check if the top of the first item is visible
                    boolean verticalScrollOffset = recyclerView.computeVerticalScrollOffset() == 0;
                    // enabling or disabling the refresh layout
                    enable = verticalScrollOffset;
                }
                if (refreshingEnabled)
                    mSwipeRefreshLayout.setEnabled(enable);
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE, type);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public ArrayList<EventFeed> getEventList() {
        return mAdapter.getList();
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
    public void setDate(Date mDate) {
        if (type != TypeFormat.CALENDAR.type())
            return;
        this.mDate = mDate;
    }

    private void loadEvents(){
        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        Observable<EvendateServiceResponseArray<EventDetail>> observable;

        final int length = mAdapterController.getLength();
        final int offset = mAdapterController.getOffset();
        if (type == ReelFragment.TypeFormat.FAVORITES.type()) {
            observable = evendateService.getFavorite(EvendateAccountManager.peekToken(getActivity()),
                    true, EventFeed.FIELDS_LIST, length, offset);

        } else if (type == ReelFragment.TypeFormat.ORGANIZATION.type()) {
            observable = evendateService.getEvents(EvendateAccountManager.peekToken(getActivity()),
                    organizationId, true, EventFeed.FIELDS_LIST, "created_at", length, offset);

        } else if (type == TypeFormat.CALENDAR.type()) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            observable = evendateService.getFeed(EvendateAccountManager.peekToken(getActivity()),
                    dateFormat.format(mDate), true, EventFeed.FIELDS_LIST, length, offset);

        } else if (type == TypeFormat.RECOMENDATION.type()) {
            observable = evendateService.getRecommendations(EvendateAccountManager.peekToken(getActivity()),
                    true, EventFeed.FIELDS_LIST, length, offset);
        }
        else{
            observable = evendateService.getFeed(EvendateAccountManager.peekToken(getActivity()),
                    true, EventDetail.FIELDS_LIST, length, offset);
        }

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    onLoaded(new ArrayList<>(result.getData()));
                }, error -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mProgressBar.setVisibility(View.GONE);
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "Complete!"));
    }

    public void onLoaded(ArrayList<EventFeed> events) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mAdapter.reset();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapterController.loaded(events);
        mProgressBar.setVisibility(View.GONE);
        if (mDataListener != null)
            mDataListener.onEventsDataLoaded();
    }

    @Override
    public void requestNext() {
        loadEvents();
    }
}