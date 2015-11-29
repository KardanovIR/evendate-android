package ru.getlect.evendate.evendate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.util.ArrayList;

import ru.getlect.evendate.evendate.authorization.AuthActivity;
import ru.getlect.evendate.evendate.authorization.EvendateAuthenticator;
import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.utils.Utils;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    final String LOG_TAG = MainActivity.class.getSimpleName();

    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView mNavigationView;

    CalendarFragment mCalendarFragment;

    private SubMenu mOrganizationMenu;
    private SubMenu mAccountMenu;
    private Cursor mSubscriptionCursor;

    /** Loader id that get subs data */
    public static final int NAV_DRAWER_SUBSCRIPTIONS_ID = 0;

    private ViewPager mViewPager;
    private MainPagerAdapter mMainPagerAdapter;

    private TabLayout mTabLayout;
    private ToggleButton mAccountToggle;

    private IconObserver mIconObserver;

    private boolean isRunning = false;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name){

            //change menu in nav drawer to provide possibility to change selected item
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(mAccountToggle.isChecked()){
                    mNavigationView.getMenu().clear();
                    mNavigationView.inflateMenu(R.menu.drawer_actions);
                    updateSubscriptionMenu();
                    mAccountToggle.setChecked(false);
                }
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);

        //just change that fucking home icon
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_menu_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        setAccountInfo();

        //sync initialization and account creation if there is no account in app
        //EvendateSyncAdapter.initializeSyncAdapter(this);
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
                //switch (position) {
                    //case 0 : {
                    //    MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.calendar);
                    //    menuItem.setChecked(true);
                    //    break;
                    //}
                    //case 0 : {
                    //    MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.reel);
                    //    menuItem.setChecked(true);
                    //    break;
                    //}
                //}
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout = (TabLayout)findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mAccountToggle = (ToggleButton) findViewById(R.id.account_view_icon_button);
        mAccountToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mNavigationView.getMenu().clear();
                    updateAccountMenu();
                    mNavigationView.inflateMenu(R.menu.drawer_accounts);
                } else {
                    mNavigationView.getMenu().clear();
                    mNavigationView.inflateMenu(R.menu.drawer_actions);
                    updateSubscriptionMenu();
                }
            }
        });
        checkPlayServices();
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
        if(!isRunning){
            mViewPager.setCurrentItem(1);
            mViewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(0);
                }
            }, 100);
            isRunning = true;
        }
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
            case R.id.reel:
                mViewPager.setCurrentItem(0);
                drawerLayout.closeDrawers();
                return true;
            case R.id.settings:
                //viewPager.setCurrentItem(2);
                drawerLayout.closeDrawers();
                return true;
            case R.id.calendar:{
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            }
           //case R.id.feedback:

           //    drawerLayout.closeDrawers();
           //    return true;
           //case R.id.help:
           //    drawerLayout.closeDrawers();
           //    return true;
            case R.id.organizations:
                Intent intentCatalog = new Intent(MainActivity.this, OrganizationCatalogActivity.class);
                startActivity(intentCatalog);
                return true;
            //case R.id.add_event:
            //    Intent intentEvent = new Intent(MainActivity.this, AddEventActivity.class);
            //    startActivity(intentEvent);
            //    return true;
            //case R.id.sync:
            //    Log.w("BUTTON_SYNC", "clicked");
            //    EvendateSyncAdapter.syncImmediately(this);
            //    return true;
            case R.id.nav_add_account:
                Intent authIntent = new Intent(this, AuthActivity.class);
                startActivity(authIntent);
                drawerLayout.closeDrawers();
                return true;
            case R.id.nav_account_management:
                Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.content_authority));
                intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.account_type));
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            default:
                if(!mAccountToggle.isChecked()){
                    Intent detailIntent = new Intent(this, OrganizationDetailActivity.class);
                    detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                            .buildUpon().appendPath(Long.toString(menuItem.getItemId())).build());
                    startActivity(detailIntent);
                }
                else{
                    String account_name = String.valueOf(menuItem.getTitle());
                    SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, account_name);
                    ed.apply();
                    setAccountInfo();
                    updateAccountMenu();
                }
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
                        null,
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
            if(mIconObserver == null){
                IconUpdaterHandler.init(this);
                mIconObserver  = new IconObserver(new IconUpdaterHandler(), getExternalCacheDir().toString() + "/" + EvendateContract.PATH_ORGANIZATION_LOGOS);
                mIconObserver.startWatching();
            }
            mSubscriptionCursor.moveToFirst();
            while(!mSubscriptionCursor.isAfterLast()){
                MenuItem menuItem = mOrganizationMenu.add(0, mSubscriptionCursor.getInt(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID)), 0,
                        mSubscriptionCursor.getString(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME)));
                mNavigationView.setItemIconTintList(null);
                try {
                    final ParcelFileDescriptor fileDescriptor = getContentResolver()
                            .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                    .appendPath("images").appendPath("organizations").appendPath("logos")
                                    .appendPath(mSubscriptionCursor.getString(mSubscriptionCursor
                                            .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID))).build(), "r");
                    //TODo перенести в xml
                    if(fileDescriptor == null)
                        //заглушка на случай отсутствия картинки
                        menuItem.setIcon(R.drawable.place);
                    else {
                        menuItem.setIcon(new BitmapDrawable(getResources(), BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor())));
                        //add to monitoring
                        mIconObserver.addId(mSubscriptionCursor.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID));
                        fileDescriptor.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                mSubscriptionCursor.moveToNext();
            }
        }
    }
    private void updateAccountMenu(){
        Menu navigationDrawerMenu = mNavigationView.getMenu();
        if(mAccountMenu != null){
            mAccountMenu.clear();
        }
        mNavigationView.setItemIconTintList(null);

        AccountManager accountManager =
                (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);

        SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
        String account_name = sPref.getString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, null);

        Account[] accounts = accountManager.getAccountsByType(getString(R.string.account_type));
        if (account_name == null)
            return;
        if(accounts.length == 0)
            return;
        mAccountMenu = navigationDrawerMenu.addSubMenu(R.id.nav_accounts, 0, 0, null);
        for(Account account : accounts){
            if(!account.name.equals(account_name)){
                MenuItem menuItem = mAccountMenu.add(0, 0, 0, account.name);
            }
        }
    }
    private void setAccountInfo(){
        TextView email = (TextView)findViewById(R.id.email);
        SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
        String account_name = sPref.getString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, null);
        email.setText(account_name);
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
                //case 0: {
                //    return new CalendarFragment();
                //}
                case 0: {
                    return ReelFragment.newInstance(ReelFragment.TypeFormat.feed.nativeInt, true);
                }
                case 1: {
                    // we need only favorite events in this fragment
                    return ReelFragment.newInstance(ReelFragment.TypeFormat.favorites.nativeInt, true);
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
                //case 0:
                //    return getString(R.string.calendar);
                case 0:
                    return getString(R.string.feed);
                case 1:
                    return getString(R.string.favorite);
                default:
                    return null;
            }
        }
    }
    class IconObserver extends FileObserver{
        Handler mHandler;
        ArrayList<Integer> ids;
        public IconObserver(Handler h, String path) {
            super(path);
            mHandler = h;
            ids = new ArrayList<>();
        }

        public void addId(int id){
            ids.add(id);
        }
        @Override
        public void onEvent(int event, String path) {
            switch (event){
                case CREATE:
                case MODIFY:{
                    if(ids.lastIndexOf(Integer.parseInt(Utils.getFileNameWithoutExtension(path))) != -1)
                        //TODO эта зараза не работает. Почему хрен знает
                        mHandler.sendEmptyMessageDelayed(IconUpdaterHandler.UPDATE_ICON, 500);
                }
            }
        }
    }

    static class IconUpdaterHandler extends Handler{
        public static final int UPDATE_ICON = 0;
        private static MainActivity mainActivity;

        public static void init(MainActivity activity){
            mainActivity = activity;
        }
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_ICON:
                    if(mainActivity != null)
                        mainActivity.updateSubscriptionMenu();
                    break;
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
