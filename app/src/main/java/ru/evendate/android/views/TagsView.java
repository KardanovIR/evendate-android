package ru.evendate.android.views;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.Tag;

/**
 * Created by Dmitry on 27.02.2016.
 */
@Deprecated
public class TagsView extends ViewGroup {
    private ArrayList<Tag> mTags;
    private OnTagClickListener listener;

    public TagsView(Context context) {
        super(context);
    }

    public TagsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTags(ArrayList<Tag> tags) {
        mTags = tags;
        initTags();
        invalidate();
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener){
        listener = onTagClickListener;
    }

    public ArrayList<Tag> getTags() {
        return mTags;
    }

    private void initTags() {
        if (mTags == null)
            return;
        if (getChildCount() != 0)
            removeViewsInLayout(0, getChildCount());
        for (Tag tag : mTags) {
            TagView tagView = new TagView(getContext());
            tagView.setText(tag.getName());
            tagView.setOnClickListener((View v) -> listener.onTagClicked(tag.getName()));
            addView(tagView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int myWidth = MeasureSpec.getSize(widthMeasureSpec);
        //setMeasuredDimension(myWidth, myHeight);

        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int count = getChildCount();
        int childState = 0;

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        int rowWidth = 0;
        int childCountInRow = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                // Measure the child.
                measureChildWithMargins(child, 0, widthMeasureSpec, 0, heightMeasureSpec);
                MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();
                int mLeft = lp.leftMargin;
                int mRight = lp.rightMargin;
                int mTop = lp.topMargin;
                int mBottom = lp.bottomMargin;
                if (rowWidth + mLeft + child.getMeasuredWidth() + mRight > myWidth) {
                    if (childCountInRow != 0) {
                        childCountInRow = 0;
                        rowWidth = 0;
                    }
                    maxHeight += mTop + child.getMeasuredHeight() + mBottom;
                } else {
                    if (childCountInRow == 0)
                        maxHeight += mTop + child.getMeasuredHeight() + mBottom;
                }
                rowWidth += mLeft + child.getMeasuredWidth() + mRight;
                childCountInRow += 1;
                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }


        // Check against our minimum height
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

        // Report our final dimensions.
        setMeasuredDimension(myWidth, resolveSizeAndState(maxHeight, heightMeasureSpec,
                childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        // These are the far left and right edges in which we are performing layout.
        int leftPos = getPaddingLeft();
        int rightPos = right - left - getPaddingRight();

        // These are the top and bottom edges in which we are performing layout.
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        int rowWidth = 0;
        int childCountInRow = 0;
        int heightOffset = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();
                int mLeft = lp.leftMargin;
                int mRight = lp.rightMargin;
                int mTop = lp.topMargin;
                int mBottom = lp.bottomMargin;

                final int width = mLeft + child.getMeasuredWidth() + mRight;
                final int height = mTop + child.getMeasuredHeight() + mBottom;

                if (rowWidth + width > rightPos) {
                    if (childCountInRow != 0) {
                        childCountInRow = 0;
                        heightOffset += height;
                        rowWidth = 0;
                    }
                }
                child.layout(leftPos + rowWidth + mLeft, parentTop + heightOffset + mTop,
                        leftPos + rowWidth + width - mRight, parentTop + heightOffset + height - mBottom);
                rowWidth += width;
                childCountInRow += 1;
            }
        }
    }

    public class TagView extends FrameLayout {
        private TextView mTextView;

        public TagView(Context context) {
            this(context, null);
        }

        public TagView(Context context, AttributeSet attrs) {
            super(context, attrs);

            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.view_tag, this, true);
            if(Build.VERSION.SDK_INT >= 21){
                view.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ripple_tag));
            } else {
                view.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tag_oval));
            }
            if (attrs == null) {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

                lp.setMargins(margin, margin, margin, margin);
                setLayoutParams(lp);
            }
            mTextView = (TextView)getChildAt(0);
        }

        public void setText(String text) {
            mTextView.setText(text);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            //View background = getChildAt(0);
            //background.setLayoutParams(new RelativeLayout.LayoutParams(mTextView.getMeasuredWidth(), mTextView.getMeasuredHeight()));
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
        }
    }

    public interface OnTagClickListener{
        void onTagClicked(String tag);
    }
}
