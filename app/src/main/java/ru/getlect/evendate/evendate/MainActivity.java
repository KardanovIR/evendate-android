package ru.getlect.evendate.evendate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import java.io.IOException;

import ru.getlect.evendate.evendate.authorization.AccountChooser;
import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView mNavigationView;

    private SubMenu mOrganizationMenu;
    private Cursor mSubscriptionCursor;

    /** Loader id that get subs data */
    public static final int NAV_DRAWER_SUBSCRIPTIONS_ID = 0;

    private ViewPager mViewPager;
    private MainPagerAdapter mMainPagerAdapter;

    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.accent_color));

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        //sync initialization and account creation if there is no account in app
        EvendateSyncAdapter.initializeSyncAdapter(this);
        getSupportLoaderManager().initLoader(NAV_DRAWER_SUBSCRIPTIONS_ID, null,
                (LoaderManager.LoaderCallbacks) this);

        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mMainPagerAdapter);

        /**
         * listener to change selected menu item in navigation drawer when current page changed by pager
         */
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0 : {
                        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.calendar);
                        menuItem.setChecked(true);
                        break;
                    }
                    case 1 : {
                        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.reel);
                        menuItem.setChecked(true);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout = (TabLayout)findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * solution for issue with view pager from support library
         * http://stackoverflow.com/questions/32323570/viewpager-title-doesnt-appear-until-i-swipe-it
         */
        mViewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(1);
            }
        }, 100);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        switch (menuItem.getItemId()) {
            //TODO fragments controlling
            case R.id.calendar:
                mViewPager.setCurrentItem(0);
                drawerLayout.closeDrawers();
                return true;
            case R.id.reel:
                mViewPager.setCurrentItem(1);
                //временно открывает окно
                //Intent intent = new Intent(getApplicationContext(), ReelActivity.class);
                //startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            case R.id.settings:
                //viewPager.setCurrentItem(2);
                drawerLayout.closeDrawers();
                return true;
            case R.id.feedback:

                drawerLayout.closeDrawers();
                return true;
            case R.id.help:
                drawerLayout.closeDrawers();
                return true;
            case R.id.organizations:

                drawerLayout.closeDrawers();
                return true;
            case R.id.add_event:
                Intent intentEvent = new Intent(MainActivity.this, AddEventActivity.class);
                startActivity(intentEvent);
                return true;
            case R.id.sync:
                Log.w("BUTTON_SYNC", "clicked");
                EvendateSyncAdapter.syncImmediately(this);
                return true;
            case R.id.authorization:
                Intent intentAuth = new Intent(this, AccountChooser.class);
                startActivity(intentAuth);
                drawerLayout.closeDrawers();
                return true;
            default:
                Intent detailIntent = new Intent(this, OrganizationActivity.class);
                detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                        .buildUpon().appendPath(Long.toString(menuItem.getItemId())).build());
                startActivity(detailIntent);
                return true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case NAV_DRAWER_SUBSCRIPTIONS_ID:
                return new CursorLoader(
                        this,
                        EvendateContract.OrganizationEntry.CONTENT_URI,
                        new String[] {
                                EvendateContract.OrganizationEntry._ID,
                                EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
                                EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID
                        },
                        EvendateContract.OrganizationEntry.COLUMN_IS_SUBSCRIBED + " = 1",
                        null,
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case NAV_DRAWER_SUBSCRIPTIONS_ID:
                mSubscriptionCursor = data;
                mSubscriptionCursor.registerContentObserver(new SubscriptionObserver(this));
                updateSubscriptionMenu();
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case NAV_DRAWER_SUBSCRIPTIONS_ID:
                mSubscriptionCursor.close();
                mSubscriptionCursor = null;
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    /**
     * observe updates of organizations and call menu updater
     */
    class SubscriptionObserver extends ContentObserver{
        Activity mActivity;
        public SubscriptionObserver(Activity activity){
            super(new Handler());
            mActivity = activity;
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getSupportLoaderManager().restartLoader(MainActivity.NAV_DRAWER_SUBSCRIPTIONS_ID,
                    null, (LoaderManager.LoaderCallbacks)mActivity);
        }
    }

    /**
     * update menu with subs and clear it if it's already existed
     */
    private void updateSubscriptionMenu(){
        Menu navigationDrawerMenu = mNavigationView.getMenu();
        if(mOrganizationMenu != null){
            mOrganizationMenu.clear();
        }
        mOrganizationMenu = navigationDrawerMenu.addSubMenu(R.id.nav_organizations, 0, 0, R.string.subscriptions);
        if(mSubscriptionCursor != null){
            while(mSubscriptionCursor.moveToNext()){
                MenuItem menuItem = mOrganizationMenu.add(0, mSubscriptionCursor.getInt(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry._ID)), 0,
                        mSubscriptionCursor.getString(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME)));
                try {
                    final ParcelFileDescriptor fileDescriptor = getContentResolver()
                            .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                    .appendPath("images").appendPath("organizations").appendPath("logos")
                                    .appendPath(mSubscriptionCursor.getString(mSubscriptionCursor
                                            .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID))).build(), "r");
                    if(fileDescriptor == null)
                        //заглушка на случай отсутствия картинки
                        menuItem.setIcon(R.drawable.place);
                    else {
                        menuItem.setIcon(new BitmapDrawable(getResources(), BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor())));
                        fileDescriptor.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    class MainPagerAdapter extends FragmentStatePagerAdapter{
        private Context mContext;

        public MainPagerAdapter(FragmentManager fragmentManager, Context context){
            super(fragmentManager);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: {
                    return new CalendarFragment();
                }
                case 1: {
                    return new ReelFragment();
                }
                case 2: {
                    // we need only favorite events in this fragment
                    Fragment fragment = new ReelFragment();
                    Bundle args = new Bundle();
                    args.putBoolean(ReelFragment.FEED, true);
                    fragment.setArguments(args);
                    return fragment;
                }
                default:
                    throw new IllegalArgumentException("invalid page number");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.calendar);
                case 1:
                    return getString(R.string.reel);
                case 2:
                    return getString(R.string.feed);
                default:
                    return null;
            }
        }
    }
}
