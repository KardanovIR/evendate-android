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
import ru.evendate.android.models.EventDate;
import ru.evendate.android.ui.utils.DateFormatter;
import ru.evendate.android.ui.utils.DateUtils;
import ru.evendate.android.ui.utils.EventFormatter;

/**
 * Created by Dmitry on 04.03.2016.
 * contain one date for event (month + day)
 */
public class DatetimeView extends LinearLayout {
    @Bind(R.id.date) TextView mDateView;
    @Bind(R.id.time) TextView mTimeView;
    private EventDate mDate;

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

    public void setDate(EventDate date) {
        mDate = date;
        mTimeView.setText(EventFormatter.formatEventTime(date.getStartDateTime(), date.getEndDateTime()));
        mDateView.setText(EventFormatter.formatDate(date.getStartDateTime()));
    }

    public void setDate(int date) {
        if (!isInEditMode())
            throw new IllegalArgumentException("Only for using in edit mode");
        mTimeView.setText(DateFormatter.formatEventSingleTime(DateUtils.date(date), DateUtils.date(date)));
        mDateView.setText(DateFormatter.formatEventSingleDate(DateUtils.date(date)));
    }
}