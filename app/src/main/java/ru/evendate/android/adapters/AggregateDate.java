package ru.evendate.android.adapters;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class AggregateDate<T> implements Comparable<AggregateDate>{
    private long date;
    private ArrayList<T> mList;

    public AggregateDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public ArrayList<T> getList() {
        return mList;
    }

    public void setList(ArrayList<T> mList) {
        this.mList = mList;
    }

    @Override
    public int compareTo(AggregateDate another) {
        return date > another.date ? 1 : date == another.date ? 0 : -1;
    }
}
