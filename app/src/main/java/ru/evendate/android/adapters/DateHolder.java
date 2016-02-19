package ru.evendate.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.evendate.android.R;
import ru.evendate.android.ui.ActionsAdapter;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class DateHolder extends RecyclerView.ViewHolder {
    public View holderView;
    public RecyclerView recyclerView;
    public TextView mDateTextView;
    public ActionsAdapter mActionsAdapter;

    public DateHolder(View itemView){
        super(itemView);
        holderView = itemView;
        recyclerView = (RecyclerView)itemView.findViewById(R.id.recycler_view);
        mDateTextView = (TextView)itemView.findViewById(R.id.date_text_view);
    }
}
