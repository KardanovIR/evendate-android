package ru.evendate.android.ui;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
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
import ru.evendate.android.authorization.AuthActivity;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Organization;
import ru.evendate.android.sync.EvendateSyncAdapter;


public class MainActivity extends AppCompatActivity implements ReelFragment.OnRefreshListener{

    final String LOG_TAG = MainActivity.class.getSimpleName();

    private Fragment mFragment;
    private Toolbar mToolbar;
    private AppBarLayout mAppBar;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final int INTRO_REQUEST = 1;
    private boolean mDestroyed = false;

    SharedPreferences mSharedPreferences = null;
    final String APP_PREF = "evendate_pref";
    final String FIRST_RUN = "first_run";
    boolean isFirstRun = false;
    public static final String TYPE = "type";
    public static final int REEL = 0;
    public static final int CALENDAR = 1;
    public static final int CATALOG = 2;
    EvendateDrawer mDrawer;


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
        mAppBar = (AppBarLayout)findViewById(R.id.app_bar_layout);

        //mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        //mNavigationView.setNavigationItemSelectedListener(new MainNavigationItemSelectedListener(this));


        checkPlayServices();

        checkAccount();
        if(savedInstanceState != null){
            switch (savedInstanceState.getInt(TYPE)){
                case REEL:
                    mFragment = new MainPagerFragment();
                    break;
                case CALENDAR:
                    mFragment = new CalendarFragment();
                    break;
                case CATALOG:
                    mFragment = new OrganizationCatalogFragment();
                    break;
                default:
                    mFragment = new MainPagerFragment();
            }
        }else{
            mFragment = new MainPagerFragment();
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
        mDrawer.getDrawer().setOnDrawerItemClickListener(new MainNavigationItemClickListener(this));
        mDrawer.getDrawer().setSelection(EvendateDrawer.REEL_IDENTIFIER);
    }

    /**
     * Returns true if the final {@link #onDestroy()} call has been made
     * on the Activity, so this instance is now dead.
     * cause api 17 has not this method
     */
    @Override
    public boolean isDestroyed() {
        return mDestroyed;
    }

    @Override
    protected void onDestroy() {
        mDestroyed = true;
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
     * handle clicks on items of navigation drawer list
     * it's maybe actions or accounts menu
     */
    private class MainNavigationItemClickListener
            implements Drawer.OnDrawerItemClickListener{
        private Context mContext;

        public MainNavigationItemClickListener(Context context) {
            mContext = context;
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
                case R.id.nav_add_account:
                    Intent authIntent = new Intent(mContext, AuthActivity.class);
                    startActivity(authIntent);
                    break;
                case R.id.nav_account_management:
                    Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                    //TODO не робит(
                    intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.content_authority));
                    intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.account_type));
                    startActivity(intent);
                    break;
                default:{
                        //open organization from subs
                        int id = ((Organization)drawerItem.getTag()).getEntryId();
                        Intent detailIntent = new Intent(mContext, OrganizationDetailActivity.class);
                        detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                                .buildUpon().appendPath(Long.toString(id)).build());
                        startActivity(detailIntent);
                    }
            }
            mDrawer.getDrawer().closeDrawer();
            return true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDrawer.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.start();
    }
}
