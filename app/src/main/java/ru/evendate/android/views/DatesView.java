package ru.evendate.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.Date;

/**
 * Created by ds_gordeev on 15.03.2016.
 */
public class DatesView extends LinearLayout {
    private ArrayList<Date> mDates;
    private LinearLayout mLayout;

    public DatesView(Context context) {
        this(context, null);
    }

    public DatesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_dates, this, true);
        mLayout = (LinearLayout) findViewById(R.id.container);
        if(isInEditMode()){
            mockup();
            return;
        }
    }
    public void setDates(ArrayList<Date> dates){
        mDates = dates;
        initDates();
        invalidate();
    }
    public ArrayList<Date> getDates(){
        return mDates;
    }

    private void initDates(){
        if(mDates == null)
            return;
        if(getChildCount() != 0)
            removeViewsInLayout(0, getChildCount());
        for (Date date : mDates){
            DatetimeView dateView = new DatetimeView(getContext());
            dateView.setDate(date);
            addView(dateView);
        }
    }

    private void mockup(){
        long[] str = {
                1446508800,
                1448928000,
                1449014400,
                1449100800
        };
        for(long date : str){
            DatetimeView dateView = new DatetimeView(getContext());
            dateView.setDate(date);
            addView(dateView);
        }
    }
}
