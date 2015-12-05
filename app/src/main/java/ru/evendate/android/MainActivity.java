package ru.evendate.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import ru.evendate.android.authorization.AuthActivity;
import ru.evendate.android.authorization.EvendateAuthenticator;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.sync.EvendateSyncAdapter;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    final String LOG_TAG = MainActivity.class.getSimpleName();

    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView mNavigationView;

    private SubMenu mOrganizationMenu;
    private SubMenu mAccountMenu;
    private Cursor mSubscriptionCursor;

    /** Loader id that get subs data */
    public static final int NAV_DRAWER_SUBSCRIPTIONS_ID = 0;

    private ViewPager mViewPager;
    private MainPagerAdapter mMainPagerAdapter;

    private TabLayout mTabLayout;
    private ToggleButton mAccountToggle;

    private boolean isRunning = false;
    ProgressDialog mProgressDialog;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final int AUTH_REQUEST = 0;

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateSubscriptionMenu();
            setAccountInfo();
            if(mProgressDialog != null)
                mProgressDialog.dismiss();
        }
    };

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

        mAccountToggle = (ToggleButton)mNavigationView.getHeaderView(0).findViewById(R.id.account_view_icon_button);
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
        RelativeLayout navHeader = (RelativeLayout)mNavigationView.getHeaderView(0);
        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof RelativeLayout)
                    if (mAccountToggle.isChecked()) {
                        mNavigationView.getMenu().clear();
                        mNavigationView.inflateMenu(R.menu.drawer_accounts);
                        updateAccountMenu();
                        mAccountToggle.setChecked(false);
                    } else {
                        mNavigationView.getMenu().clear();
                        mNavigationView.inflateMenu(R.menu.drawer_actions);
                        updateSubscriptionMenu();
                        mAccountToggle.setChecked(true);
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
        registerReceiver(syncFinishedReceiver, new IntentFilter(EvendateSyncAdapter.SYNC_FINISHED));
        checkAccount();

        /**
         * solution for issue with view pager from support library
         * http://stackoverflow.com/questions/32323570/viewpager-title-doesnt-appear-until-i-swipe-it
         */
        //if(!isRunning){
        //    mViewPager.setCurrentItem(1);
        //    mViewPager.postDelayed(new Runnable() {
        //        @Override
        //        public void run() {
        //            mViewPager.setCurrentItem(0);
        //        }
        //    }, 100);
        //    isRunning = true;
        //}
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(syncFinishedReceiver);
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
        mOrganizationMenu = navigationDrawerMenu
                .addSubMenu(R.id.nav_organizations, 0, 0, R.string.subscriptions);
        mNavigationView.setItemIconTintList(null);
        if(mSubscriptionCursor != null){
            mSubscriptionCursor.moveToFirst();
            while(!mSubscriptionCursor.isAfterLast()){
                int id = mSubscriptionCursor.getInt(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID));
                MenuItem menuItem = mOrganizationMenu.add(0, id, 0,
                        mSubscriptionCursor.getString(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME)));
                menuItem.setVisible(false);
                MenuIconTask menuIconTask = new MenuIconTask(this, menuItem);
                menuIconTask.execute(mSubscriptionCursor.getString(mSubscriptionCursor
                        .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_LOGO_URL)));
                mSubscriptionCursor.moveToNext();
            }
        }
    }
    public class MenuIconTask extends AsyncTask<String, Void, Bitmap> {
        MenuItem mMenuItem;
        Context mContext;
        public MenuIconTask(Context context, MenuItem menuItem) {
            mMenuItem = menuItem;
            mContext = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try{
                return Picasso.with(mContext)
                        .load(params[0])
                        .get();
            }catch (IOException e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap == null)
                mMenuItem.setIcon(R.mipmap.ic_launcher).setVisible(true);
            else
                mMenuItem.setIcon(new BitmapDrawable(getResources(), bitmap)).setVisible(true);
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
        mAccountMenu = navigationDrawerMenu.addSubMenu(R.id.nav_accounts, 0, 0, R.string.accounts);
        for(Account account : accounts){
            if(!account.name.equals(account_name)){
                MenuItem menuItem = mAccountMenu.add(0, 0, 0, account.name);
            }
        }
    }
    private void setAccountInfo(){
        RelativeLayout header = (RelativeLayout)mNavigationView.getHeaderView(0);
        TextView email = (TextView)header.findViewById(R.id.email);
        ImageView avatar = (ImageView)header.findViewById(R.id.avatar);
        TextView username = (TextView)header.findViewById(R.id.username);
        SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
        String account_name = sPref.getString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, null);
        String first_name = sPref.getString(EvendateSyncAdapter.FIRST_NAME, null);
        String last_name = sPref.getString(EvendateSyncAdapter.LAST_NAME, null);
        if(first_name == null && last_name == null) {
            mProgressDialog = new ProgressDialog(this, R.style.Theme_FirstSyncDialog);
            mProgressDialog.setTitle(getString(R.string.progress_dialog_title));
            mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        email.setText(account_name);
        username.setText(first_name + " " + last_name);

        Picasso.with(this)
                .load(sPref.getString(EvendateSyncAdapter.AVATAR_URL, null))
                .error(R.mipmap.ic_launcher)
                .into(avatar);
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
                    return mContext.getString(R.string.feed);
                case 1:
                    return mContext.getString(R.string.favorite);
                default:
                    return null;
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
    private void checkAccount(){
        Account account = EvendateSyncAdapter.getSyncAccount(this);
        if(account == null){
            Intent authIntent = new Intent(this, AuthActivity.class);
            startActivityForResult(authIntent, AUTH_REQUEST);
        }
    }
    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTH_REQUEST) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }
}