package ru.evendate.android.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler view adapter with endless listing support
 */
public abstract class AbstractAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private List<T> mList = new ArrayList<>();

    /**
     * append items to end of the list
     */
    public void add(final @NonNull List<T> list) {
        for (T item : list) {
            append(item);
        }
    }

    /**
     * replace current list by new list
     */
    public void set(final @NonNull List<T> list) {
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

    /**
     * clear list
     */
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

    /**
     * append item to end of the list
     */
    protected void append(T item) {
        mList.add(mList.size(), item);
        notifyItemInserted(getItemCount());
    }

    /**
     * remove concrete item from the list
     */
    protected void remove(T item) {
        int position = mList.indexOf(item);
        mList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * update concrete item in the list
     */
    protected void update(T item) {
        int position = mList.indexOf(item);
        notifyItemChanged(position);
    }
}
