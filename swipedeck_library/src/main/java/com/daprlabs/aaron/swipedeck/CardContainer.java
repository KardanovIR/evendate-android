package com.daprlabs.aaron.swipedeck;

import android.view.View;

import com.daprlabs.aaron.swipedeck.Utility.SwipeCallback;
import com.daprlabs.aaron.swipedeck.Utility.SwipeListener;

/**
 * Created by aaron on 21/08/2016.
 */
public class CardContainer {

    int positionWithinViewGroup = -1;
    int positionWithinAdapter = -1;
    private View view;
    private SwipeListener swipeListener;
    private SwipeCallback callback;
    private SwipeDeck parent;
    private long id;
    private int swipeDuration = SwipeDeck.ANIMATION_DURATION;

    public CardContainer(final View view, SwipeDeck parent, final SwipeCallback callback) {
        this.view = view;
        this.parent = parent;
        this.callback = callback;

        setupSwipeListener();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onClick(view);
            }
        });
    }

    public int getPositionWithinViewGroup() {
        return positionWithinViewGroup;
    }

    public void setPositionWithinViewGroup(int pos) {
        this.positionWithinViewGroup = pos;
    }

    public View getCard() {
        return this.view;
    }

    public void cleanupAndRemoveView() {
        //wait for card to render off screen, do cleanup and remove from viewgroup
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                deleteViewFromSwipeDeck();
            }
        }, swipeDuration);
    }

    private void deleteViewFromSwipeDeck() {
        parent.removeView(view);
        parent.removeFromBuffer(this);
    }

    public void setSwipeEnabled(boolean enabled) {
        //also checks in case user doesn't want to be able to swipe the card freely
        if (enabled && parent.SWIPE_ENABLED) {
            view.setOnTouchListener(swipeListener);
        } else {
            view.setOnTouchListener(null);
        }
    }

    public void setLeftImageResource(int leftImageResource) {
        View left = view.findViewById(leftImageResource);
        left.setAlpha(0);
        swipeListener.setLeftView(left);

    }

    public void setRightImageResource(int rightImageResource) {
        View right = view.findViewById(rightImageResource);
        right.setAlpha(0);
        swipeListener.setRightView(right);
    }

    public void setMaskResource(int maskResource) {
        View mask = view.findViewById(maskResource);
        swipeListener.setMaskView(mask);
    }

    public void setupSwipeListener() {
        this.swipeListener = new SwipeListener(
                view,
                callback,
                parent.getPaddingLeft(),
                parent.getPaddingTop(),
                parent.ROTATION_DEGREES,
                parent.OPACITY_END,
                parent
        );
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPositionWithinAdapter() {
        return positionWithinAdapter;
    }

    public void setPositionWithinAdapter(int position) {
        this.positionWithinAdapter = position;
    }

    public void swipeCardLeft(int duration) {
        // Remember how long card would be animating
        swipeDuration = duration;
        // Disable touch events
        setSwipeEnabled(false);
        swipeListener.swipeCardLeft(duration);
    }

    public void swipeCardRight(int duration) {
        swipeDuration = duration;
        setSwipeEnabled(false);
        swipeListener.swipeCardRight(duration);
    }
}
