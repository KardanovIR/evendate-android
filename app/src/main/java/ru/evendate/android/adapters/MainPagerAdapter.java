package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.evendate.android.R;
import ru.evendate.android.ui.ReelFragment;

/**
 * Created by Dmitry on 23.01.2016.
 */

public class MainPagerAdapter extends FragmentStatePagerAdapter implements ReelFragment.OnRefreshListener {
    private Context mContext;
    private ReelFragment.OnRefreshListener listener;
    private final int TAB_COUNT = 3;
    private final int REEL_TAB = 0;
    private final int FAVE_TAB = 1;
    private final int RECOMMEND_TAB = 2;


    public void setOnRefreshListener(ReelFragment.OnRefreshListener refreshListener) {
        listener = refreshListener;
    }

    public MainPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case REEL_TAB: {
                ReelFragment fragment = ReelFragment.newInstance(ReelFragment.TypeFormat.FEED.type(), true);
                fragment.setOnRefreshListener(this);
                return fragment;
            }
            case FAVE_TAB: {
                ReelFragment fragment = ReelFragment.newInstance(ReelFragment.TypeFormat.FAVORITES.type(), true);
                fragment.setOnRefreshListener(this);
                return fragment;
            }
            case RECOMMEND_TAB: {
                ReelFragment fragment = ReelFragment.newInstance(ReelFragment.TypeFormat.RECOMMENDATION.type(), true);
                fragment.setOnRefreshListener(this);
                return fragment;
            }
            default:
                throw new IllegalArgumentException("invalid page number");
        }
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case REEL_TAB:
                return mContext.getString(R.string.feed_tab);
            case FAVE_TAB:
                return mContext.getString(R.string.favorite_tab);
            case RECOMMEND_TAB:
                return mContext.getString(R.string.recommendation_tab);
            default:
                return null;
        }
    }

    /**
     * return strings for statistics
     */
    public String getPageLabel(int position) {
        switch (position) {
            case REEL_TAB:
                return mContext.getString(R.string.stat_page_feed);
            case FAVE_TAB:
                return mContext.getString(R.string.stat_page_favorite);
            case RECOMMEND_TAB:
                return mContext.getString(R.string.stat_page_recommendations);
            default:
                return null;
        }
    }

    @Override
    public void onRefresh() {
        if (listener != null)
            listener.onRefresh();
    }
}