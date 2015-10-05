package ru.getlect.evendate.evendate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ReelActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel);

        // зададим Toolbar, как ActionBar, это обеспечит нам
        // обратную совместимость с предыдущими версиями Android (API Level < 21).
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //можно жмякать на три полоски и вылезет nav drawer!
        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new android.support.v7.app.ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(toggle);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final int PAGE_COUNT = 2;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ReelFragment.newInstance(position + 1);
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return PAGE_COUNT;
        }

        //@Override
        //public CharSequence getPageTitle(int position) {
        //    Locale l = Locale.getDefault();
        //    switch (position) {
        //        case 0:
        //            return getString(R.string.title_section1).toUpperCase(l);
        //        case 1:
        //            return getString(R.string.title_section2).toUpperCase(l);
        //        case 2:
        //            return getString(R.string.title_section3).toUpperCase(l);
        //    }
        //    return null;
        //}
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    //public static class NavDrawlerFragment extends Fragment {
    //    /**
    //     * The fragment argument representing the section number for this
    //     * fragment.
    //     */
    //    private static final String ARG_SECTION_NUMBER = "section_number";
//
    //    /**
    //     * Returns a new instance of this fragment for the given section
    //     * number.
    //     */
    //    public static NavDrawlerFragment newInstance(int sectionNumber) {
    //        NavDrawlerFragment fragment = new NavDrawlerFragment();
    //        Bundle args = new Bundle();
    //        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
    //        fragment.setArguments(args);
    //        return fragment;
    //    }
//
    //    public NavDrawlerFragment() {
    //    }
//
    //    @Override
    //    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    //                             Bundle savedInstanceState) {
    //        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    //        return rootView;
    //    }
//
    //    @Override
    //    public void onAttach(Activity activity) {
    //        super.onAttach(activity);
    //        ((MainActivity) activity).onSectionAttached(
    //                getArguments().getInt(ARG_SECTION_NUMBER));
    //    }
    //}

}
