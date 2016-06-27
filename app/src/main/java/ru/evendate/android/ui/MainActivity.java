package ru.evendate.android.ui;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.auth.AuthActivity;


public class MainActivity extends AppCompatActivity implements ReelFragment.OnRefreshListener {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private Fragment mFragment;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String TYPE = "type";
    public static final int REEL = 0;
    public static final int CALENDAR = 1;
    public static final int CATALOG = 2;
    private EvendateDrawer mDrawer;
    private final int REQUEST_AUTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        //just change that fucking home icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_menu_white);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.getDrawer().openDrawer();
            }
        });

        mDrawer = EvendateDrawer.newInstance(this);
        checkPlayServices();
        checkAccount();

        if (savedInstanceState != null) {
            switch (savedInstanceState.getInt(TYPE)) {
                case REEL:
                    mFragment = new MainPagerFragment();
                    ((MainPagerFragment)mFragment).setOnRefreshListener(this);
                    break;
                case CALENDAR:
                    mFragment = new CalendarFragment();
                    break;
                default:
                    mFragment = new MainPagerFragment();
                    ((MainPagerFragment)mFragment).setOnRefreshListener(this);
            }
        } else {
            mFragment = new MainPagerFragment();
            ((MainPagerFragment)mFragment).setOnRefreshListener(this);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();

        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new MainNavigationItemClickListener(this, mDrawer.getDrawer()));
        mDrawer.getDrawer().setSelection(EvendateDrawer.REEL_IDENTIFIER);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
     * check account exists and start auth if no
     */
    private boolean checkAccount() {
        Account account = EvendateAccountManager.getSyncAccount(this);
        if (account == null) {
            Intent authIntent = new Intent(this, AuthActivity.class);
            startActivityForResult(authIntent, REQUEST_AUTH);
            return false;
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTH) {
            if(resultCode == RESULT_CANCELED) {
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
            extends NavigationItemSelectedListener {
        private Context mContext;
        public MainNavigationItemClickListener(Context context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
                case EvendateDrawer.REEL_IDENTIFIER: {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if (!(mFragment instanceof MainPagerFragment)) {
                        mFragment = new MainPagerFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                    }
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(0f);
                }
                break;
                case R.id.settings:
                    break;
                case EvendateDrawer.CALENDAR_IDENTIFIER: {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if (!(mFragment instanceof CalendarFragment)) {
                        mFragment = new CalendarFragment();
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

    @Override
    public void onBackPressed() {
        if (mDrawer.getDrawer().isDrawerOpen()) {
            mDrawer.getDrawer().closeDrawer();
        }
        else {
            super.onBackPressed();
        }
    }
}
