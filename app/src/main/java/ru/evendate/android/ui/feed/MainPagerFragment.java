package ru.evendate.android.ui.feed;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.ReelFragment;

/**
 * Created by Dmitry on 23.01.2016.
 * contain logic of switching main fragments in main activity
 */
public class MainPagerFragment extends Fragment {
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    private MainPagerAdapter mMainPagerAdapter;
    private ReelFragment.OnRefreshListener mRefreshListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_pager, container, false);
        ButterKnife.bind(this, rootView);

        mMainPagerAdapter = new MainPagerAdapter(getChildFragmentManager(), getActivity());
        mViewPager.setOffscreenPageLimit(2);
        if (mRefreshListener != null)
            mMainPagerAdapter.setOnRefreshListener(mRefreshListener);
        mViewPager.setAdapter(mMainPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        setupStat();

        if (Build.VERSION.SDK_INT >= 21)
            getActivity().findViewById(R.id.app_bar_layout).setElevation(0.0f);
        return rootView;
    }

    /**
     * translate refresh message from child fragment to parent main activity
     */
    public void setOnRefreshListener(ReelFragment.OnRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
        if (mMainPagerAdapter != null)
            mMainPagerAdapter.setOnRefreshListener(refreshListener);
    }

    /**
     * setup screen names of fragments for statistic screen tracking
     */
    private void setupStat() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                new Statistics(getContext()).sendCurrentScreenName("Main Screen ~" +
                        mMainPagerAdapter.getPageLabel(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void refresh() {
        mMainPagerAdapter.refresh();
    }

    private class MainPagerAdapter extends FragmentPagerAdapter implements ReelFragment.OnRefreshListener {
        private final int TAB_COUNT = 2;
        private final int REEL_TAB = 0;
        private final int FAVE_TAB = 1;
        private final int RECOMMEND_TAB = 2;
        ReelFragment reelFragment;
        ReelFragment faveFragment;
        ReelFragment recommendFragment;
        private Context mContext;
        private ReelFragment.OnRefreshListener listener;


        MainPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            mContext = context;
        }

        void setOnRefreshListener(ReelFragment.OnRefreshListener refreshListener) {
            listener = refreshListener;
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
                    return mContext.getString(R.string.tab_main_feed);
                case FAVE_TAB:
                    return mContext.getString(R.string.tab_main_favorite);
                case RECOMMEND_TAB:
                    return mContext.getString(R.string.tab_main_recommendation);
                default:
                    return null;
            }
        }

        /**
         * return strings for statistics
         */
        String getPageLabel(int position) {
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

        void refresh() {
            reelFragment.reloadEvents();
            faveFragment.reloadEvents();
            recommendFragment.reloadEvents();
        }
    }
}
