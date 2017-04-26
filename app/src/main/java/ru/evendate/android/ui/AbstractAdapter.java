package ru.evendate.android.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
@Deprecated
public abstract class AbstractAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected Context mContext;
    private List<T> mList = new ArrayList<>();

    public AbstractAdapter(Context context) {
        this.mContext = context;
    }

    public void add(List<T> list) {
        for (T item : list) {
            append(item);
        }
    }

    public void replace(final List<T> list) {
        int size = mList.size();
        for (int index = 0; index < list.size(); index++) {
            if (index < size) {
                mList.remove(index);
                mList.add(index, list.get(index));
                update(list.get(index));
            } else {
                append(list.get(index));
            }
        }
        if (list.size() < size) {
            List<T> removing = new ArrayList<>(mList.subList(list.size(), size));
            for (T item : removing) {
                remove(item);
            }
        }
    }

    public void reset() {
        int size = mList.size();
        mList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public boolean isEmpty() {
        return mList.size() == 0;
    }

    protected T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    protected void append(T item) {
        mList.add(mList.size(), item);
        notifyItemInserted(getItemCount());
    }

    protected void remove(T item) {
        int position = mList.indexOf(item);
        mList.remove(position);
        notifyItemRemoved(position);
    }

    protected void update(T item) {
        int position = mList.indexOf(item);
        notifyItemChanged(position);
    }
}