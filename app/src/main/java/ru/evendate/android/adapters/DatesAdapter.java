package ru.evendate.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ru.evendate.android.R;
import ru.evendate.android.sync.models.Action;
import ru.evendate.android.sync.models.EventFormatter;
import ru.evendate.android.ui.ActionsAdapter;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class DatesAdapter extends AbstractAdapter<AgregateDate<Action>, DateHolder> {

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
        AgregateDate<Action> entry = getList().get(position);
        String date = EventFormatter.formatDay(entry.getDate()) + " " +
                EventFormatter.formatMonth(entry.getDate());
        holder.mDateTextView.setText(date);
        holder.mActionsAdapter = new ActionsAdapter(mContext);
        holder.recyclerView.setAdapter(holder.mActionsAdapter);
        holder.mActionsAdapter.setList(entry.getList());
    }
}