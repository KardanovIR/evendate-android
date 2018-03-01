package ru.evendate.android.ui.feed;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Observable;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Event;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.EndlessListFragment;
import ru.evendate.android.ui.EventsAdapter;
import ru.evendate.android.ui.NpaLinearLayoutManager;
import ru.evendate.android.ui.eventdetail.EventDetailActivity;

/**
 * fragment containing a reel
 * used in calendar, main pager activities
 * contain endless recycle view with cards for event list
 */
public class ReelFragment extends EndlessListFragment<ReelPresenter, Event, EventsAdapter.EventHolder>
        implements EventsAdapter.EventsInteractionListener {
    private static String LOG_TAG = ReelFragment.class.getSimpleName();
    static final String TYPE_KEY = "type";
    /**
     * organization id from detail organization
     */
    static final String ORG_KEY = "organization_id";
    /**
     * selected date in calendar
     */
    static final String DATE_KEY = "date";
    private boolean refreshTurnOn = false;
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

    protected AbstractAdapter<Event, EventsAdapter.EventHolder> getAdapter() {
        if (mAdapter != null) {
            return mAdapter;
        } else {
            return new EventsAdapter(getContext(), type, this);
        }
    }

    @Override
    protected void onRefresh() {
        if (!refreshTurnOn)
            getSwipeRefreshLayout().setEnabled(false);
        super.onRefresh();
        if (mRefreshListenerList != null) {
            for (OnRefreshListener listener : mRefreshListenerList) {
                listener.onRefresh();
            }
        }
    }

    @Override
    protected void initRecyclerView() {
        super.initRecyclerView();
        int columnCount = getResources().getInteger(R.integer.feed_column_count);
        if (columnCount == 1) {
            mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }
        /*
          listener that let using reload on top of the event list
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
                    getSwipeRefreshLayout().setEnabled(enable);
            }
        });

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

    @Override
    protected String getEmptyHeader() {
        switch (ReelType.getType(type)) {
            case FEED:
                return getString(R.string.list_feed_empty_header);
            case FAVORITES:
                return getString(R.string.list_favourites_empty_header);
            case RECOMMENDATION:
                return getResources().getString(R.string.list_recommendation_empty_header);
            case ORGANIZATION:
            case ORGANIZATION_PAST:
                return getString(R.string.list_organization_empty_header);
        }
        return null;
    }

    @Override
    protected String getEmptyDescription() {
        switch (ReelType.getType(type)) {
            case FEED:
                return getString(R.string.list_feed_empty_text);
            case FAVORITES:
                return getString(R.string.list_favourites_empty_text);
            case RECOMMENDATION:
                return null;
            case ORGANIZATION:
            case ORGANIZATION_PAST:
                return getString(R.string.list_organization_empty_text);
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!refreshTurnOn)
            getSwipeRefreshLayout().setEnabled(false);
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
    public void openEvent(Event event) {
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.setData(EvendateContract.EventEntry.getContentUri(event.getEntryId()));
        if (Build.VERSION.SDK_INT >= 21) {
            getContext().startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        } else
            getContext().startActivity(intent);
    }

    @Override
    public void likeEvent(Event event) {
        getPresenter().likeEvent(event);
    }

    @Override
    public void hideEvent(Event event) {
        getPresenter().hideEvent(event);
    }

    @Override
    public Observable<String> requestAuth() {
        return ((BaseActivity)getActivity()).requestAuth();
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
        getPresenter().reload();
    }


    public enum ReelType {
        FEED(0),
        FAVORITES(1),
        ORGANIZATION(2),
        ORGANIZATION_PAST(5),
        CALENDAR(3),
        RECOMMENDATION(4),
        SEARCH(6),
        SEARCH_BY_TAG(7);

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