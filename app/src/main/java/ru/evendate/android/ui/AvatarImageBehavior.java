package ru.evendate.android.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 22.02.2016.
 */
@SuppressWarnings("unused")
public class AvatarImageBehavior extends CoordinatorLayout.Behavior<View> {
    private int mStartYPosition;
    private int mFinalYPosition;

    public AvatarImageBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (dependency instanceof AppBarLayout)
            modifyAvatarDependingDependencyState(child, (AppBarLayout)dependency);
        return true;
    }

    private void modifyAvatarDependingDependencyState(
            View avatar, AppBarLayout dependency) {
        int toolbarHeight = dependency.findViewById(R.id.toolbar).getHeight();
        avatar.setAlpha(1.0f - ((float)(dependency.getTotalScrollRange() - Math.abs(dependency.getTop()) - toolbarHeight)
                / (dependency.getTotalScrollRange() - toolbarHeight)));
        if (dependency.getTotalScrollRange() - Math.abs(dependency.getTop()) <= toolbarHeight)
            avatar.setAlpha(0.0f);
    }
}