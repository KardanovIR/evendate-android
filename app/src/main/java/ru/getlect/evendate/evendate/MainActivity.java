package ru.getlect.evendate.evendate;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

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
    public static final int NAV_DRAWER_SUBCRIPTIONS_ID = 0;


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
        getSupportLoaderManager().initLoader(NAV_DRAWER_SUBCRIPTIONS_ID, null,
                (LoaderManager.LoaderCallbacks)this);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        switch (menuItem.getItemId()) {
            //TODO fragments controlling
            case R.id.calendar:
                //viewPager.setCurrentItem(0);
                drawerLayout.closeDrawers();
                return true;
            case R.id.reel:
                //viewPager.setCurrentItem(1);
                //временно открывает окно
                Intent intent = new Intent(getApplicationContext(), ReelActivity.class);
                startActivity(intent);
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
            case NAV_DRAWER_SUBCRIPTIONS_ID:
                return new CursorLoader(
                        this,
                        EvendateContract.OrganizationEntry.CONTENT_URI,
                        new String[] {
                                EvendateContract.OrganizationEntry._ID,
                                EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
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
            case NAV_DRAWER_SUBCRIPTIONS_ID:
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
            case NAV_DRAWER_SUBCRIPTIONS_ID:
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
            getSupportLoaderManager().restartLoader(MainActivity.NAV_DRAWER_SUBCRIPTIONS_ID,
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
                //TODO set icon
                mOrganizationMenu.add(0, mSubscriptionCursor.getInt(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry._ID)), 0,
                        mSubscriptionCursor.getString(mSubscriptionCursor
                                .getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME)))
                        .setIcon(R.drawable.place);
            }
        }
    }
}
