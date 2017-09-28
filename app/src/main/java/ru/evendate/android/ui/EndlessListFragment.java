package ru.evendate.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ybq.endless.Endless;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.R;
import ru.evendate.android.views.LoadStateView;

/**
 * Created by dmitry on 11.09.17.
 */
public abstract class EndlessListFragment<T extends EndlessContract.EndlessPresenter, D, VH extends RecyclerView.ViewHolder>
        extends Fragment implements EndlessContract.EndlessView<T, D> {
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    protected @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    private Endless mEndless;
    private Unbinder unbinder;
    protected AbstractAdapter<D, VH> mAdapter;
    private T mPresenter;

    @Override
    public void setPresenter(T presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_endless_list, container, false);

        unbinder = ButterKnife.bind(this, view);
        initRecyclerView();

        View loadingView = inflater.inflate(R.layout.item_progress, container, false);
        mEndless = Endless.applyTo(mRecyclerView, loadingView);
        mEndless.setLoadMoreListener((int page) -> mPresenter.load(false, page));

        mSwipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        mLoadStateView.setOnReloadListener(() -> mPresenter.reload());
        mLoadStateView.setEmptyHeader(getEmptyHeader());
        mLoadStateView.setEmptyDescription(getEmptyDescription());

        return view;
    }

    protected void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new LandingAnimator());
        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }


    protected void onRefresh() {
        mEndless.setLoadMoreAvailable(false);
        mEndless.setCurrentPage(0);
        mPresenter.reload();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPresenter != null)
            mPresenter.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mPresenter != null)
            mPresenter.stop();
    }

    protected abstract AbstractAdapter<D, VH> getAdapter();

    protected abstract String getEmptyHeader();

    protected abstract String getEmptyDescription();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active && !mSwipeRefreshLayout.isRefreshing()) {
            mLoadStateView.showProgress();
        } else {
            if (!isEmpty()) {
                mLoadStateView.hideProgress();
            }
            mSwipeRefreshLayout.setRefreshing(active);
        }
    }

    @Override
    public void showList(List<D> list, boolean isLast) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEndless.loadMoreComplete();
        mEndless.setLoadMoreAvailable(!isLast);
        mAdapter.add(list);
    }

    @Override
    public void reshowList(List<D> list, boolean isLast) {
        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.setVisibility(View.VISIBLE);
        mEndless.loadMoreComplete();
        mEndless.setLoadMoreAvailable(!isLast);
        mAdapter.set(list);
    }

    @Override
    public void showEmptyState() {
        mSwipeRefreshLayout.setRefreshing(false);
        //mRecyclerView.setVisibility(View.INVISIBLE);
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
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    protected T getPresenter() {
        return mPresenter;
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }
}
