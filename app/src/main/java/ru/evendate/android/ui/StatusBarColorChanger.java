package ru.evendate.android.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.util.TypedValue;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class StatusBarColorChanger implements AppBarLayout.OnOffsetChangedListener {
    private Activity mContext;

    public StatusBarColorChanger(Activity context) {
        mContext = context;
    }

    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (Build.VERSION.SDK_INT < 21)
            return;
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
            if (Math.abs(verticalOffset) + actionBarHeight > appBarLayout.getTotalScrollRange()) {
                mContext.getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                mContext.getWindow().setStatusBarColor(mContext.getResources().getColor(R.color.black_translucent));
            }
        }
    }
}
