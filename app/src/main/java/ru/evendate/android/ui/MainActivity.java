package ru.evendate.android.ui;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import ru.evendate.android.R;
import ru.evendate.android.sync.EvendateSyncAdapter;


public class MainActivity extends AppCompatActivity implements ReelFragment.OnRefreshListener{
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private Fragment mFragment;
    private Toolbar mToolbar;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final int INTRO_REQUEST = 1;

    private SharedPreferences mSharedPreferences = null;
    public final String APP_PREF = "evendate_pref";
    public final String FIRST_RUN = "first_run";
    public boolean isFirstRun = false;
    public static final String TYPE = "type";
    public static final int REEL = 0;
    public static final int CALENDAR = 1;
    public static final int CATALOG = 2;
    private EvendateDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //just change that fucking home icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_menu_white);
        mToolbar = toolbar;

        //mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        //mNavigationView.setNavigationItemSelectedListener(new MainNavigationItemSelectedListener(this));


        checkPlayServices();

        checkAccount();
        if(savedInstanceState != null){
            switch (savedInstanceState.getInt(TYPE)){
                case REEL:
                    mFragment = new MainPagerFragment();
                    ((MainPagerFragment)mFragment).setOnRefreshListener(this);
                    break;
                case CALENDAR:
                    mFragment = new CalendarFragment();
                    break;
                case CATALOG:
                    mFragment = new OrganizationCatalogFragment();
                    break;
                default:
                    mFragment = new MainPagerFragment();
                    ((MainPagerFragment)mFragment).setOnRefreshListener(this);
            }
        }else{
            mFragment = new MainPagerFragment();
            ((MainPagerFragment)mFragment).setOnRefreshListener(this);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        mSharedPreferences = getSharedPreferences(APP_PREF, MODE_PRIVATE);
        if (mSharedPreferences.getBoolean(FIRST_RUN, true)) {
            isFirstRun = true;
            mSharedPreferences.edit().putBoolean(FIRST_RUN, false).apply();
        }
        else
            fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();

        mDrawer = EvendateDrawer.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new MainNavigationItemClickListener(this, mDrawer.getDrawer()));
        mDrawer.getDrawer().setSelection(EvendateDrawer.REEL_IDENTIFIER);
        mDrawer.start();
    }

    @Override
    protected void onDestroy() {
        mDrawer.cancel();
        super.onDestroy();
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

    /**
     * check account exists and start intro if no
     */
    private void checkAccount(){
        Account account = EvendateSyncAdapter.getSyncAccount(this);
        if(account == null){
            Intent introIntent = new Intent(this, EvendateIntro.class);
            startActivityForResult(introIntent, INTRO_REQUEST);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == INTRO_REQUEST){
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public void onRefresh() {
        mDrawer.update();
    }

    /**
     * handle clicks on items of navigation drawer list in main activity
     */
    private class MainNavigationItemClickListener
            extends NavigationItemSelectedListener{
        public MainNavigationItemClickListener(Context context, Drawer drawer) {
            super(context, drawer);
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()){
                case EvendateDrawer.REEL_IDENTIFIER:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof MainPagerFragment)){
                        mFragment = new MainPagerFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                    }
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(0f);
                }
                break;
                case R.id.settings:
                    break;
                case EvendateDrawer.CALENDAR_IDENTIFIER:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof CalendarFragment)){
                        mFragment = new CalendarFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                    }
                    // Converts 4 dip into its equivalent px
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(px);
                }
                break;
                case EvendateDrawer.ORGANIZATION_IDENTIFIER:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof OrganizationCatalogFragment)){
                        mFragment = new OrganizationCatalogFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                    }
                    // Converts 4 dip into its equivalent px
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(px);
                }
                break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            mDrawer.closeDrawer();
            return true;
        }
    }
}
