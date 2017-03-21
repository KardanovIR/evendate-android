package ru.evendate.android.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.adapters.MainPagerAdapter;
import ru.evendate.android.statistics.Statistics;

/**
 * Created by Dmitry on 23.01.2016.
 * contain logic of switching main fragments in main activity
 */
public class MainPagerFragment extends Fragment {
    @Bind(R.id.pager) ViewPager mViewPager;
    @Bind(R.id.tabs) TabLayout mTabLayout;
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                new Statistics(getContext()).sendCurrentScreenName("Main Screen ~" +
                        mMainPagerAdapter.getPageLabel(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    public void refresh() {
        mMainPagerAdapter.refresh();
    }
}
