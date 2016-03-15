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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;

/**
 * Created by Dmitry on 23.01.2016.
 */
public class MainPagerFragment extends Fragment {
    private ViewPager mViewPager;
    private MainPagerAdapter mMainPagerAdapter;

    private TabLayout mTabLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_pager, container, false);

        mViewPager = (ViewPager)rootView.findViewById(R.id.pager);
        mMainPagerAdapter = new MainPagerAdapter(getChildFragmentManager(), getActivity());
        mViewPager.setAdapter(mMainPagerAdapter);

        mTabLayout = (TabLayout)rootView.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        setupStat();

        if (Build.VERSION.SDK_INT >= 21)
            getActivity().findViewById(R.id.app_bar_layout).setElevation(0.0f);
        return rootView;
    }

    public void setOnRefreshListener(ReelFragment.OnRefreshListener refreshListener){
        mMainPagerAdapter.setOnRefreshListener(refreshListener);
    }
    private void setupStat(){
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                Tracker tracker = EvendateApplication.getTracker();
                tracker.setScreenName("Main Screen ~" +
                        mMainPagerAdapter.getPageLabel(position));
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 21)
            getActivity().findViewById(R.id.app_bar_layout).setElevation(4.0f);
    }
}
