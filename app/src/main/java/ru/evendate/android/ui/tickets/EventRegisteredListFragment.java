package ru.evendate.android.ui.tickets;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ybq.endless.Endless;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.ui.AbstractEndlessAdapter;
import ru.evendate.android.ui.eventdetail.EventDetailActivity;
import ru.evendate.android.ui.utils.EventFormatter;
import ru.evendate.android.ui.utils.FormatUtils;
import ru.evendate.android.ui.utils.TicketFormatter;
import ru.evendate.android.views.LoadStateView;

//TODO DRY
public class EventRegisteredListFragment extends Fragment implements EventRegisteredContract.View,
        EventRegisteredContract.OnEventInteractionListener {

    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    EventRegisteredRecyclerViewAdapter mAdapter;
    EventRegisteredContract.Presenter mPresenter;
    private Endless mEndless;
    private Unbinder unbinder;

    public void setPresenter(EventRegisteredContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_registered_list, container, false);

        unbinder = ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new EventRegisteredRecyclerViewAdapter(getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        //todo not work cause Endless notifyDataSetChanged
        mRecyclerView.setItemAnimator(new LandingAnimator());

        View loadingView = inflater.inflate(R.layout.item_progress, container, false);
        mEndless = Endless.applyTo(mRecyclerView, loadingView);
        mEndless.setLoadMoreListener((int page) -> mPresenter.loadEvents(false, page));

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mEndless.setLoadMoreAvailable(false);
            mEndless.setCurrentPage(0);
            mPresenter.loadEvents(true, 0);
        });
        mLoadStateView.setOnReloadListener(() -> mPresenter.loadEvents(true, 0));

        setEmptyCap();

        return view;
    }

    private void setEmptyCap() {
        mLoadStateView.setEmptyHeader(getString(R.string.event_registered_empty_cap));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPresenter.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void showEvents(List<EventRegistered> list, boolean isLast) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEndless.loadMoreComplete();
        mEndless.setLoadMoreAvailable(!isLast);
        mAdapter.add(list);
    }

    @Override
    public void reshowEvents(List<EventRegistered> list, boolean isLast) {
        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.setVisibility(View.VISIBLE);
        mEndless.loadMoreComplete();
        mEndless.setLoadMoreAvailable(!isLast);
        mAdapter.set(list);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            mLoadStateView.showProgress();
        } else {
            mLoadStateView.hideProgress();
        }
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public void showEmptyState() {
        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadStateView.showEmptyHint();
        mEndless.loadMoreComplete();
        mEndless.setLoadMoreAvailable(false);
    }

    @Override
    public void showError() {
        mEndless.loadMoreComplete();
        mEndless.setLoadMoreAvailable(false);
        mSwipeRefreshLayout.setRefreshing(false);
        mLoadStateView.showErrorHint();
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onEventClick(EventRegistered item) {
        Intent ticketsIntent = new Intent(getContext(), TicketListActivity.class);
        Bundle bundle = new Bundle();
        Parcelable parcelEvent = Parcels.wrap(item);
        bundle.putParcelable(TicketListActivity.EVENT_KEY, parcelEvent);
        ticketsIntent.putExtras(bundle);
        startActivity(ticketsIntent);
    }

    @Override
    public void onEventLongClick(EventRegistered item) {
        final int OPEN_EVENT_ID = 0;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(item.getTitle())
                .setItems(new CharSequence[]{
                                getString(R.string.event_registered_open_detail)
                        },
                        (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case OPEN_EVENT_ID:
                                    Intent intent = new Intent(getContext(), EventDetailActivity.class);
                                    intent.setData(EvendateContract.EventEntry.getContentUri(item.getEntryId()));
                                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
                                    break;
                            }
                        });
        builder.create().show();
    }

    class EventRegisteredRecyclerViewAdapter extends AbstractEndlessAdapter<EventRegistered,
            EventRegisteredRecyclerViewAdapter.EventRegisteredViewHolder> {

        private final EventRegisteredContract.OnEventInteractionListener mListener;
        private Context mContext;

        EventRegisteredRecyclerViewAdapter(@NonNull Context context,
                                           @NonNull EventRegisteredContract.OnEventInteractionListener listener) {
            mListener = listener;
            mContext = context;
        }

        @Override
        public EventRegisteredViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_ticket, parent, false);
            return new EventRegisteredRecyclerViewAdapter.EventRegisteredViewHolder(view);
        }

        @Override
        public void onViewRecycled(EventRegisteredViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mTicketCount.setVisibility(View.GONE);
        }

        @Override
        public void onBindViewHolder(final EventRegisteredViewHolder holder, int position) {
            EventRegistered event = getItem(position);
            holder.mTitle.setText(event.getTitle());
            holder.mDatetime.setText(EventFormatter.formatDate(EventFormatter.getNearestDateTime((Event) event)));
            holder.mPlace.setText(event.getLocation());
            holder.mEvent = event;
            if (event.getMyTicketsCount() > 1) {
                holder.mTicketCount.setText(
                        TicketFormatter.formatTicketCount(FormatUtils.getCurrentLocale(mContext),
                                event.getMyTicketsCount(), mContext.getString(R.string.ticket_count_label)));
                holder.mTicketCount.setVisibility(View.VISIBLE);
            }

            holder.holderView.setOnClickListener((View v) -> mListener.onEventClick(holder.mEvent));
            holder.holderView.setOnLongClickListener((View v) -> {
                mListener.onEventLongClick(holder.mEvent);
                return true;
            });
        }

        class EventRegisteredViewHolder extends RecyclerView.ViewHolder {
            View holderView;
            @BindView(R.id.hint) TextView mHint;
            @BindView(R.id.title) TextView mTitle;
            @BindView(R.id.datetime) TextView mDatetime;
            @BindView(R.id.place) TextView mPlace;
            @BindView(R.id.ticket_count) TextView mTicketCount;

            @Nullable EventRegistered mEvent;

            EventRegisteredViewHolder(View view) {
                super(view);
                holderView = itemView;
                ButterKnife.bind(this, view);
            }
        }
    }
}
