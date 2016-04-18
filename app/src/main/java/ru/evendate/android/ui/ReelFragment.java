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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import ru.evendate.android.R;
import ru.evendate.android.adapters.AppendableAdapter;
import ru.evendate.android.adapters.EventsAdapter;
import ru.evendate.android.loaders.EventsLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.EventFeed;

/**
 * fragment containing a reel
 * used in calendar, main pager activities
 * contain recycle view with cards for event list
 */
public class ReelFragment extends Fragment implements LoaderListener<ArrayList<EventFeed>>, AppendableAdapter.AdapterController {
    private String LOG_TAG = ReelFragment.class.getSimpleName();

    private android.support.v7.widget.RecyclerView mRecyclerView;

    private EventsAdapter mAdapter;
    private EventsLoader mEventLoader;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    boolean refreshingEnabled = false;

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
        CALENDAR(4);

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
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accent),
                PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        if (savedInstanceState != null) {
            type = savedInstanceState.getInt(TYPE);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh_layout);
        if (!refreshingEnabled)
            mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mEventLoader.reset();
                mEventLoader.startLoading();
                if (mRefreshListenerList != null) {
                    for (OnRefreshListener listener : mRefreshListenerList) {
                        listener.onRefresh();
                    }
                }
            }
        });
        mAdapter = new EventsAdapter(getActivity(), this, type);
        mRecyclerView.setAdapter(mAdapter);
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
        initLoader();
        mSwipeRefreshLayout.setRefreshing(true);
        mEventLoader.startLoading();
        return rootView;
    }

    private void initLoader() {
        if (type == TypeFormat.FEED.type) {
            mEventLoader = new EventsLoader(getActivity(), type);
        } else if (type == TypeFormat.ORGANIZATION.type) {
            mEventLoader = new EventsLoader(getActivity(), type, organizationId);
        } else if (type == TypeFormat.CALENDAR.type) {
            mEventLoader = new EventsLoader(getActivity(), type, mDate);
        } else if (type == TypeFormat.FAVORITES.type) {
            mEventLoader = new EventsLoader(getActivity(), type);
        }
        mEventLoader.setLoaderListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE, type);
    }


    public ArrayList<Object> getEventList() {
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

    /**
     *
     */
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

    public void onLoaded(ArrayList<EventFeed> eventList) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mAdapter.reset();
            mAdapter.enableNext();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        if (eventList.size() < mEventLoader.getLength()) {
            mAdapter.disableNext();
        }
        mAdapter.setList(eventList);
        mProgressBar.setVisibility(View.GONE);
        if (mDataListener != null)
            mDataListener.onEventsDataLoaded();
    }

    public void onError() {
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventLoader.cancelLoad();
    }

    @Override
    public void requestNext() {
        mEventLoader.startLoading();
    }
}