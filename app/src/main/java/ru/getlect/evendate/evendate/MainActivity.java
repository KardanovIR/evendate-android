package ru.getlect.evendate.evendate;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;


public class MainActivity extends AppCompatActivity {


//    private NavigationDrawerFragment mNavigationDrawerFragment;
//    private TextView tv_bottom;
//    private CharSequence mTitle;
    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout drawerLayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Временная кнопка
        final Button btn_magicButton = (Button)findViewById(R.id.btn_magicButton);
        btn_magicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_magicButton:
                        Intent intentEvent = new Intent(MainActivity.this, AddEventActivity.class);
                        startActivity(intentEvent);
                        break;
                }
            }
        });


        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.accent_color));

        mDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);


//
//        toggle = new android.support.v7.app.ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
//        toggle.setDrawerIndicatorEnabled(true);
//        drawerLayout.setDrawerListener(toggle);



//        tv_bottom = (TextView)findViewById(R.id.tv_bottom);

//        mNavigationDrawerFragment = (NavigationDrawerFragment)
//                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
//        mTitle = getTitle();
//
////         Set up the drawer.
//        mNavigationDrawerFragment.setUp(
//                R.id.navigation_drawer,
//                (DrawerLayout) findViewById(R.id.drawer_layout));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {

        }


        MaterialCalendarView widget = (MaterialCalendarView) findViewById(R.id.calendarView);
//        widget.setOnDateChangedListener(this);
//
//        widget.addDecorator(new DisableAllDaysDecorator());
////        widget.addDecorator(new EnableOneToTenDecorator());

        // инициализация синхронизации, создание аккаунта
        EvendateSyncAdapter.initializeSyncAdapter(this);



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

//    @Override
//    public void onDateChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
//        String stringDay = String.valueOf(calendarDay);
//        Toast.makeText(this, stringDay, Toast.LENGTH_SHORT).show();

//    }



//    private static class DisableAllDaysDecorator implements DayViewDecorator {
//
//        @Override
//        public boolean shouldDecorate(CalendarDay day) {
//            return day.getDay() <=31;
//        }
//
//        @Override
//        public void decorate(DayViewFacade view) {
//            view.setDaysDisabled(true);
//        }

//        private static boolean[] PRIME_TABLE = {
//                false,  // 0?
//                true,
//                false,// 2
//                false,// 3
//                false,
//                false,// 5
//                false,
//                false,// 7
//                false,
//                false,
//                false,
//                false,// 11
//                false,
//                false,// 13
//                false,
//                false,
//                false,
//                false,// 17
//                false,
//                false,// 19
//                false,
//                false,
//                false,
//                true,// 23
//                true,
//                true,
//                true,
//                true,
//                true,
//                true,// 29
//                false,
//                false,// 31
//                false,
//                false,
//                false, //PADDING
//        };
//    }

//    private static class EnableOneToTenDecorator implements DayViewDecorator {
//
//        @Override
//        public boolean shouldDecorate(CalendarDay day) {
//            return day.getDay() <= 10;
//        }
//
//        @Override
//        public void decorate(DayViewFacade view) {
//            view.setDaysDisabled(false);
//        }
//    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        toggle.syncState();
//    }
//
//    @Override
//    public void onNavigationDrawerItemSelected(int position) {
//        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//                .commit();
//    }

//    public void onSectionAttached(int number) {
//        switch (number) {
//            case 1:
//
//                break;
//            case 2:
//
//                break;
//            case 3:
//
//                break;
//        }
//    }
//
//    public void restoreActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
//    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
//            // Only show items in the action bar relevant to this screen
//            // if the drawer is not showing. Otherwise, let the drawer
//            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.main, menu);
//            restoreActionBar();
//            return true;
//        }
//        return super.onCreateOptionsMenu(menu);
//    }


//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((MainActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }

}
