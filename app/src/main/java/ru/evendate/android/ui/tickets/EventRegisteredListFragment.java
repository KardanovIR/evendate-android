package ru.evendate.android.ui.tickets;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ybq.endless.Endless;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.views.LoadStateView;
import rx.Subscription;

public class EventRegisteredListFragment extends Fragment implements LoadStateView.OnReloadListener {
    private static String LOG_TAG = EventRegisteredListFragment.class.getSimpleName();

    @Bind(R.id.load_state) LoadStateView mLoadStateView;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    EventRegisteredRecyclerViewAdapter mAdapter;
    boolean isFuture = true;
    int LENGTH = 10;
    DataRepository mDataRepository;
    Subscription mSubscription;
    private Endless mEndless;
    private OnEventInteractionListener mListener;

    @SuppressWarnings("unused")
    public static EventRegisteredListFragment newInstance(boolean future) {
        EventRegisteredListFragment fragment = new EventRegisteredListFragment();
        fragment.isFuture = future;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_registered_list, container, false);

        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new EventRegisteredRecyclerViewAdapter(getContext(), mListener);
        mAdapter.set(new ArrayList<>());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        //todo not work cause Endless notifyDataSetChanged
        mRecyclerView.setItemAnimator(new LandingAnimator());

        View loadingView = inflater.inflate(R.layout.item_progress, container, false);
        mEndless = Endless.applyTo(mRecyclerView, loadingView);
        mEndless.setLoadMoreListener((int page) -> loadEvents(false, page));

        setEmptyCap();
        mLoadStateView.setOnReloadListener(this);
        mDataRepository = new DataRepository(getContext());

        initRefresh();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadEvents(false, 0);
    }

    private void initRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mEndless.setLoadMoreAvailable(false);
            loadEvents(true, 0);
        });
    }

    private void setEmptyCap() {
        mLoadStateView.setEmptyHeader(getString(R.string.event_registered_empty_cap));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEventInteractionListener) {
            mListener = (OnEventInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventInteractionListener");
        }
    }

    @Override
    public void onReload() {
        loadEvents(true, 0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mSubscription != null)
            mSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    //todo solid
    public void loadEvents(boolean forceLoad, int page) {

        mSubscription = mDataRepository.getRegisteredEvents(isFuture, page, LENGTH).subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk())
                        if (forceLoad) {
                            onReloaded(new ArrayList<>(result.getData()));
                        } else {
                            onLoaded(new ArrayList<>(result.getData()));
                        }
                    else {
                        mEndless.loadMoreComplete();
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoadStateView.showErrorHint();
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    }
                },
                this::onError,
                mLoadStateView::hideProgress
        );
    }

    public void onLoaded(List<EventRegistered> list) {
        if (list.size() < LENGTH)
            mEndless.setLoadMoreAvailable(false);
        mAdapter.add(list);
        mEndless.loadMoreComplete();
        checkListAndShowHint();
    }

    protected void checkListAndShowHint() {
        if (mAdapter.isEmpty()) {
            mLoadStateView.showEmptryHint();
        }
    }

    public void onReloaded(List<EventRegistered> list) {
        mSwipeRefreshLayout.setRefreshing(false);
        mEndless.setLoadMoreAvailable(true);
        if (list.size() < LENGTH)
            mEndless.setLoadMoreAvailable(false);
        mAdapter.set(list);
        mRecyclerView.setVisibility(View.VISIBLE);
        checkListAndShowHint();
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mSwipeRefreshLayout.setRefreshing(false);
        mLoadStateView.showErrorHint();
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    interface OnEventInteractionListener {
        void onEventClick(EventRegistered item);
    }

}
