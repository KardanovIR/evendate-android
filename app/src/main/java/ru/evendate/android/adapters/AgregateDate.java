package ru.evendate.android.adapters;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class AgregateDate<T> {
    long date;
    private ArrayList<T> mList;

    public AgregateDate(long date) {
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
}
