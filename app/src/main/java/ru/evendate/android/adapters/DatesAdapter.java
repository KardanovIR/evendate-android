package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.evendate.android.R;
import ru.evendate.android.models.ActionType;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.ui.OrganizationCatalogAdapter;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class DatesAdapter extends AbstractAdapter<AggregateDate<ActionType>, DatesAdapter.DateHolder> {

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
        if(getList() == null)
            return;
        AggregateDate<ActionType> entry = getList().get(position);
        String date = EventFormatter.formatDay(entry.getDate()) + " " +
                EventFormatter.formatMonth(entry.getDate());
        holder.mDateTextView.setText(date);
        holder.mActionsAdapter = new ActionTypesAdapter(mContext);
        holder.recyclerView.setLayoutManager(
                new OrganizationCatalogAdapter.CatalogLinearLayoutManager(mContext,
                        LinearLayoutManager.VERTICAL, false));
        holder.recyclerView.setAdapter(holder.mActionsAdapter);
        holder.mActionsAdapter.setList(entry.getList());
        holder.recyclerView.setNestedScrollingEnabled(false);
    }
    public class DateHolder extends RecyclerView.ViewHolder {
        public View holderView;
        public RecyclerView recyclerView;
        public TextView mDateTextView;
        public ActionTypesAdapter mActionsAdapter;

        public DateHolder(View itemView){
            super(itemView);
            holderView = itemView;
            recyclerView = (RecyclerView)itemView.findViewById(R.id.recycler_view);
            mDateTextView = (TextView)itemView.findViewById(R.id.date_text_view);
        }
    }
}