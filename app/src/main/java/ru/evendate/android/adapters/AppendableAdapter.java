package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import ru.evendate.android.R;
import ru.evendate.android.ui.AdapterController;

/**
 * Created by ds_gordeev on 31.03.2016.
 */
public abstract class AppendableAdapter<T> extends AbstractAdapter<T, RecyclerView.ViewHolder> {
    private final String LOG_TAG = AppendableAdapter.class.getSimpleName();

    public static final int PROGRESS_VIEW_TYPE = R.layout.item_progress;

    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 2;
    private boolean loading;
    private AdapterController mController;

    public AppendableAdapter(Context context, RecyclerView recyclerView) {
        super(context);
        initLastItemsListener(recyclerView);
    }

    public void initLastItemsListener(RecyclerView recyclerView) {
        if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager))
            return;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    // End has been reached
                    // Do something
                    if (mController != null) {
                        if (mController.isDisable())
                            return;
                        loading = true;
                        Log.d(LOG_TAG, "requesting next");
                        mController.requestNext();
                        notifyItemInserted(getItemCount() - 1);
                    }
                }
            }
        });
    }

    protected boolean isLoading() {
        return loading;
    }

    @Override
    public int getItemCount() {
        final int PROGRESS_VIEW = 1;
        if (loading)
            return super.getItemCount() + PROGRESS_VIEW;
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (loading)
            return PROGRESS_VIEW_TYPE;
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progress, parent, false);

        return new ProgressViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        loading = false;
        notifyItemRemoved(getItemCount());
    }

    public void setController(AdapterController controller) {
        this.mController = controller;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar)v.findViewById(R.id.progress_bar);
        }
    }
}
