package ru.evendate.android.adapters;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class AggregateDate<T> implements Comparable<AggregateDate> {
    private Date date;
    private ArrayList<T> mList;

    public AggregateDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<T> getList() {
        return mList;
    }

    public void setList(ArrayList<T> mList) {
        this.mList = mList;
    }

    @Override
    public int compareTo(@NonNull AggregateDate another) {
        return date.getTime() > another.date.getTime() ? 1 : date.getTime() == another.date.getTime() ? 0 : -1;
    }
}
