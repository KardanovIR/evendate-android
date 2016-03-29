package ru.evendate.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.DateFull;
import ru.evendate.android.models.EventFormatter;

/**
 * Created by Dmitry on 04.03.2016.
 * contain one date for event (month + day)
 */
public class DatetimeView extends LinearLayout {
    @Bind(R.id.date) TextView mDateView;
    @Bind(R.id.time) TextView mTimeView;
    private DateFull mDate;

    public DatetimeView(Context context) {
        this(context, null);
    }

    public DatetimeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.view_datetime, this, true);
        ButterKnife.bind(this, rootView);
        if (attrs == null) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

            lp.setMargins(0, margin, 0, margin);
            setLayoutParams(lp);
        }
    }

    public void setDate(DateFull date) {
        mDate = date;
        String endTime = mDate.getEndTime();
        mTimeView.setText(EventFormatter.formatTime(mDate.getStartTime()) + (endTime != null ? " - " + EventFormatter.formatTime(mDate.getEndTime()) : ""));
        mDateView.setText(EventFormatter.formatDate(mDate));
    }

    public void setDate(long date) {
        if (!isInEditMode())
            throw new IllegalArgumentException("Only for using in edit mode");
        mTimeView.setText(EventFormatter.formatTime(date) + " - " + EventFormatter.formatTime(date));
        mDateView.setText(EventFormatter.formatDate(date));
    }
}