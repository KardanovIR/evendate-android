package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.EventFeed;

/**
 * Created by ds_gordeev on 15.04.2016.
 */
public abstract class AdapterWithDates extends AppendableAdapter<Object> {
    ItemsWithDatesAdapter mAdapter;

    public AdapterWithDates(Context context, AdapterController adapterController) {
        super(context, adapterController);
    }

    public ItemsWithDatesAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(ItemsWithDatesAdapter mAdapter) {
        this.mAdapter = mAdapter;
        setList(mAdapter.getList());
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void setList(ArrayList<Object> list) {
        mAdapter.addEvents(list);
        for (Object item : list)
            add(item, getList().size());
    }

    private void add(T item, int position) {
        getList().add(position, item);
        //TODO потенциальная проблема
        notifyItemInserted(getItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.date_item_light;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DateHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        DateHolder holder = (DateHolder)viewHolder;
        holder.mDatetimeTextView.setText(String.valueOf(((Date)getList().get(position)).getTime()));
    }

    public class DateHolder extends RecyclerView.ViewHolder {
        public View holderView;
        @Bind(R.id.date_item_datetime)
        public TextView mDatetimeTextView;

        public DateHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
        }
    }

}
