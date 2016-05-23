package ru.evendate.android.ui;

import android.support.annotation.LayoutRes;

import com.mikepenz.materialdrawer.model.ProfileDrawerItem;

import ru.evendate.android.R;

/**
 * Created by ds_gordeev on 04.03.2016.
 */
public class SubscriptionDrawerItem extends ProfileDrawerItem {

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.drawer_item_subscription;
    }
}
