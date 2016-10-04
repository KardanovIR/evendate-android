package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.evendate.android.R;
import ru.evendate.android.ui.ReelFragment;

/**
 * Created by Dmitry on 23.01.2016.
 */

public class MainPagerAdapter extends FragmentPagerAdapter implements ReelFragment.OnRefreshListener {
    private Context mContext;
    private ReelFragment.OnRefreshListener listener;
    private final int TAB_COUNT = 3;
    private final int REEL_TAB = 0;
    private final int FAVE_TAB = 1;
    private final int RECOMMEND_TAB = 2;

    ReelFragment reelFragment;
    ReelFragment faveFragment;
    ReelFragment recommendFragment;


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
                reelFragment = ReelFragment.newInstance(ReelFragment.ReelType.FEED.type(), true);
                reelFragment.setOnRefreshListener(this);
                return reelFragment;
            }
            case FAVE_TAB: {
                faveFragment = ReelFragment.newInstance(ReelFragment.ReelType.FAVORITES.type(), true);
                faveFragment.setOnRefreshListener(this);
                return faveFragment;
            }
            case RECOMMEND_TAB: {
                recommendFragment = ReelFragment.newInstance(ReelFragment.ReelType.RECOMMENDATION.type(), true);
                recommendFragment.setOnRefreshListener(this);
                return recommendFragment;
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
    public void refresh(){
        reelFragment.reloadEvents();
        faveFragment.reloadEvents();
        recommendFragment.reloadEvents();
    }
}