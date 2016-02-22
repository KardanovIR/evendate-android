package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.evendate.android.R;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.UserActionsFragment;
import ru.evendate.android.ui.UserSubscriptionsFragment;

/**
 * Created by Dmitry on 21.02.2016.
 */
public class UserPagerAdapter extends FragmentStatePagerAdapter{
    private Context mContext;
    private UserDetail mUser;

    public UserPagerAdapter(FragmentManager fragmentManager, Context context, UserDetail user){
        super(fragmentManager);
        mContext = context;
        mUser = user;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: {
                return UserActionsFragment.newInstance(mUser.getEntryId());
            }
            case 1: {
                return UserSubscriptionsFragment.newInstance(mUser);
            }
            default:
                throw new IllegalArgumentException("invalid page number");
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return mContext.getString(R.string.tab_actions);
            case 1:
                return mContext.getString(R.string.tab_subscriptions);
            default:
                return null;
        }
    }
}