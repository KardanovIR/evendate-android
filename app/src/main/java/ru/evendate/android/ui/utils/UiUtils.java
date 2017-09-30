package ru.evendate.android.ui.utils;

import android.animation.Animator;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by Dmitry on 25.09.2016.
 */

public class UiUtils {

    public static void revealView(View view) {
        if (view.getVisibility() == View.VISIBLE && view.isAttachedToWindow())
            return;
        view.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT < 21)
            return;
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator animation = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        animation.start();
    }
}
