package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
public abstract class AbstractAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected Context mContext;
    private ArrayList<T> mList;

    public AbstractAdapter(Context context) {
        this.mContext = context;
    }

    public void setList(ArrayList<T> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public ArrayList<T> getList() {
        return mList;
    }

    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }
}
