package ru.evendate.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        return rootView;
    }

    public void setOnRefreshListener(ReelFragment.OnRefreshListener refreshListener){
        mMainPagerAdapter.setOnRefreshListener(refreshListener);
    }
}
