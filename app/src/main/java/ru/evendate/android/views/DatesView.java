package ru.evendate.android.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.R;
import ru.evendate.android.models.EventDate;

/**
 * Created by ds_gordeev on 15.03.2016.
 * date card for event detail
 * contain also button to expand and collapse when dates more when more than 5
 */
public class DatesView extends CardView {
    @BindView(R.id.container) LinearLayout mLayout;
    @BindView(R.id.expand_container) LinearLayout mLayoutExpand;
    @BindView(R.id.expand_button) ToggleButton ExpandButton;
    private ArrayList<EventDate> mDates;
    private int minDates = 5;

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

    public ArrayList<EventDate> getDates() {
        return mDates;
    }

    public void setDates(ArrayList<EventDate> dates) {
        mDates = dates;
        initDates();
        if (dates.size() < minDates) {
            ExpandButton.setVisibility(GONE);
        } else
            mLayoutExpand.setVisibility(GONE);
    }

    private void initDates() {
        if (mDates == null)
            return;
        if (mLayout.getChildCount() != 0)
            mLayout.removeViewsInLayout(0, mLayout.getChildCount());
        for (EventDate date : mDates) {
            DatetimeView dateView = new DatetimeView(getContext());
            dateView.setDate(date);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            lp.setMargins(0, margin, 0, margin);
            dateView.setLayoutParams(lp);
            if (mLayout.getChildCount() == 5)
                mLayoutExpand.addView(dateView);
            else
                mLayout.addView(dateView);
        }
        invalidate();
    }

    private void hideViews() {
        if (ExpandButton.isChecked()) {
            expand(mLayoutExpand);
        } else
            collapse(mLayoutExpand);
    }

    public void expand(final View v) {
        v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RelativeLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 2dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density) / 2);
        v.startAnimation(a);
    }

    public void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 2dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density) / 2);
        v.startAnimation(a);
    }

    private void mockup() {
        int[] str = {
                1446508800,
                1448928000,
                1449014400,
                1449100800
        };
        for (int date : str) {
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
        hideViews();
    }
}
