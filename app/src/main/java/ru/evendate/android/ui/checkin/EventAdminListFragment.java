package ru.evendate.android.ui.checkin;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ybq.endless.Endless;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.utils.EventFormatter;
import ru.evendate.android.ui.utils.FormatUtils;
import ru.evendate.android.ui.utils.TicketFormatter;
import ru.evendate.android.views.LoadStateView;

//TODO DRY
public class EventAdminListFragment extends Fragment implements CheckInContract.EventAdminView {

    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    private EventAdminListFragment.EventAdminRecyclerViewAdapter mAdapter;
    private CheckInContract.EventAdminPresenter mPresenter;
    private CheckInContract.EventInteractionListener mListener;
    private Endless mEndless;
    private Unbinder unbinder;

    @Override
    public void setPresenter(CheckInContract.EventAdminPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (CheckInContract.EventInteractionListener)parent;
        } else {
            mListener = (CheckInContract.EventInteractionListener)context;
        }
    }

    @Override
    public Observable<String> requestAuth() {
        return ((BaseActivity)getActivity()).requestAuth();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_admin_list, container, false);

        unbinder = ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new EventAdminRecyclerViewAdapter(getContext(), mListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        //todo not work cause Endless notifyDataSetChanged
        mRecyclerView.setItemAnimator(new LandingAnimator());

        View loadingView = inflater.inflate(R.layout.item_progress, container, false);
        mEndless = Endless.applyTo(mRecyclerView, loadingView);
        mEndless.setLoadMoreListener((int page) -> mPresenter.load(false, page));

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mEndless.setLoadMoreAvailable(true);
            mEndless.setCurrentPage(0);
            mPresenter.reload();
        });
        mLoadStateView.setOnReloadListener(() -> mPresenter.reload());

        setEmptyCap();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.start();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mPresenter.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    private void setEmptyCap() {
        mLoadStateView.setEmptyDescription(getString(R.string.check_in_event_empty_description));
    }

    @Override
    public void showList(List<EventRegistered> list, boolean isLast) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEndless.loadMoreComplete();
        mEndless.setLoadMoreAvailable(!isLast);
        mAdapter.add(list);
    }

    @Override
    public void reshowList(List<EventRegistered> list, boolean isLast) {
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

    class EventAdminRecyclerViewAdapter extends AbstractAdapter<EventRegistered,
            EventAdminRecyclerViewAdapter.EventAdminViewHolder> {

        private final CheckInContract.EventInteractionListener mListener;
        private Context mContext;

        EventAdminRecyclerViewAdapter(@NonNull Context context,
                                      @NonNull CheckInContract.EventInteractionListener listener) {
            mListener = listener;
            mContext = context;
        }

        @Override
        public EventAdminViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_ticket, parent, false);
            return new EventAdminViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final EventAdminViewHolder holder, int position) {
            EventRegistered event = getItem(position);
            holder.mTitle.setText(event.getTitle());
            holder.mDatetime.setText(EventFormatter.formatDate(EventFormatter.getNearestDateTime((Event)event)));
            holder.mPlace.setText(event.getLocation());
            holder.mEvent = event;
            holder.mTicketCount.setText(
                    TicketFormatter.formatTicketCount(FormatUtils.getCurrentLocale(mContext),
                            ((Event)event).getTicketsCount(), getString(R.string.check_event_tickets)));
            holder.mTicketCount.setVisibility(View.VISIBLE);

            holder.holderView.setOnClickListener((View v) -> mListener.onEventSelected(holder.mEvent));
        }

        class EventAdminViewHolder extends RecyclerView.ViewHolder {
            View holderView;
            @BindView(R.id.hint) TextView mHint;
            @BindView(R.id.title) TextView mTitle;
            @BindView(R.id.datetime) TextView mDatetime;
            @BindView(R.id.place) TextView mPlace;
            @BindView(R.id.ticket_count) TextView mTicketCount;

            @Nullable EventRegistered mEvent;

            EventAdminViewHolder(View view) {
                super(view);
                holderView = itemView;
                ButterKnife.bind(this, view);
            }
        }
    }
}
