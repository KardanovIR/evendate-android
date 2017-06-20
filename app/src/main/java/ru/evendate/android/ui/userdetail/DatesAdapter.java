package ru.evendate.android.ui.userdetail;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.ActionType;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.WrapLinearLayoutManager;
import ru.evendate.android.ui.utils.EventFormatter;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class DatesAdapter extends AbstractAdapter<AggregateDate<ActionType>, DatesAdapter.DateHolder> {
    private RecyclerView.RecycledViewPool dateItemsPool = new RecyclerView.RecycledViewPool();
    private RecyclerView.RecycledViewPool actionItemsPool = new RecyclerView.RecycledViewPool();

    public DatesAdapter(Context context) {
        super(context);
    }

    @Override
    public DateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DateHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false));
    }

    @Override
    public void onBindViewHolder(DateHolder holder, int position) {
        AggregateDate<ActionType> entry = getItem(position);
        String date = EventFormatter.formatDate(entry.getDate());
        holder.mDateTextView.setText(date);
        holder.mActionsAdapter = new ActionTypesAdapter(mContext, actionItemsPool);
        WrapLinearLayoutManager manager =
                new WrapLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        manager.setAutoMeasureEnabled(false);
        holder.actionsListView.setLayoutManager(manager);
        holder.actionsListView.setRecycledViewPool(dateItemsPool);
        holder.actionsListView.setAdapter(holder.mActionsAdapter);
        holder.mActionsAdapter.add(entry.getList());
        holder.actionsListView.setNestedScrollingEnabled(false);
    }

    class DateHolder extends RecyclerView.ViewHolder {
        View holderView;
        @Bind(R.id.recycler_view) RecyclerView actionsListView;
        @Bind(R.id.date_text_view) TextView mDateTextView;
        ActionTypesAdapter mActionsAdapter;

        DateHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
        }
    }
}