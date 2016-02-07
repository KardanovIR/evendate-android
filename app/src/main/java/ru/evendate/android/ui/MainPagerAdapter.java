package ru.evendate.android.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 23.01.2016.
 */

class MainPagerAdapter extends FragmentStatePagerAdapter implements ReelFragment.OnRefreshListener{
    private Context mContext;
    private ReelFragment.OnRefreshListener listener;

    public void setOnRefreshListener(ReelFragment.OnRefreshListener refreshListener){
        listener = refreshListener;
    }

    public MainPagerAdapter(FragmentManager fragmentManager, Context context){
        super(fragmentManager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: {
                ReelFragment fragment = ReelFragment.newInstance(ReelFragment.TypeFormat.feed.nativeInt, true);
                fragment.setOnRefreshListener(this);
                return fragment;
            }
            case 1: {
                // we need only favorite events in this fragment
                ReelFragment fragment = ReelFragment.newInstance(ReelFragment.TypeFormat.favorites.nativeInt, true);
                fragment.setOnRefreshListener(this);
                return fragment;
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
                return mContext.getString(R.string.feed);
            case 1:
                return mContext.getString(R.string.favorite);
            default:
                return null;
        }
    }

    /**
     * return strings for statistics
     * @param position int
     * @return String
     */
    public String getPageLabel(int position){
        switch (position){
            case 0:
                return mContext.getString(R.string.page_feed);
            case 1:
                return mContext.getString(R.string.page_favorite);
            default:
                return null;
        }
    }

    @Override
    public void onRefresh() {
        if(listener != null)
            listener.onRefresh();
    }
}