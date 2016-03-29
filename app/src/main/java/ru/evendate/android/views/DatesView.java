package ru.evendate.android.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.R;
import ru.evendate.android.models.DateFull;

/**
 * Created by ds_gordeev on 15.03.2016.
 * date card for event detail
 * contain also button to expand and collapse when dates more when more than 5
 */
public class DatesView extends CardView {
    private ArrayList<DateFull> mDates;
    @Bind(R.id.container) LinearLayout mLayout;
    private int minDates = 5;
    @Bind(R.id.expand_button) ToggleButton ExpandButton;

    public DatesView(Context context) {
        this(context, null);
    }

    public DatesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View rootView = inflate(getContext(), R.layout.view_dates, this);
        ButterKnife.bind(this, rootView);
        ExpandButton.setChecked(false);
        if (isInEditMode()) {
            mockup();
            return;
        }
    }

    public void setDates(ArrayList<DateFull> dates) {
        mDates = dates;
        initDates();
        if (dates.size() < minDates) {
            ExpandButton.setVisibility(GONE);
        }
    }

    public ArrayList<DateFull> getDates() {
        return mDates;
    }

    private void initDates() {
        if (mDates == null)
            return;
        if (mLayout.getChildCount() != 0)
            mLayout.removeViewsInLayout(0, mLayout.getChildCount());
        for (DateFull date : mDates) {
            if (!ExpandButton.isChecked()) {
                if (mLayout.getChildCount() == minDates)
                    break;
            }
            DatetimeView dateView = new DatetimeView(getContext());
            dateView.setDate(date);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            lp.setMargins(0, margin, 0, margin);
            dateView.setLayoutParams(lp);
            mLayout.addView(dateView);
        }
        invalidate();
    }

    private void mockup() {
        long[] str = {
                1446508800,
                1448928000,
                1449014400,
                1449100800
        };
        for (long date : str) {
            DatetimeView dateView = new DatetimeView(getContext());
            dateView.setDate(date);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dateView.setLayoutParams(lp);
            mLayout.addView(dateView);
        }
    }

    @OnClick(R.id.expand_button)
    public void expand(ToggleButton button) {
        button.setChecked(button.isChecked());
        initDates();
    }
}
