package ru.evendate.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
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
import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.authorization.AuthActivity;
import ru.evendate.android.authorization.EvendateAuthenticator;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.MeLoader;
import ru.evendate.android.loaders.SubscriptionLoader;
import ru.evendate.android.models.Organization;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.sync.EvendateSyncAdapter;


public class MainActivity extends AppCompatActivity implements LoaderListener<ArrayList<Organization>>,
        ReelFragment.OnRefreshListener{
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView mNavigationView;
    private int checkedMenuItemId = R.id.reel;

    private SubscriptionsAdapter mSubscriptionAdapter;
    private AccountAdapter mAccountAdapter;
    private AccountsAdapter mAccountsAdapter;
    private SubscriptionLoader mSubscriptionLoader;
    private MeLoader mMeLoader;
    /**
     * false -> action menu
     * true -> account menu
     */
    private ToggleButton mAccountToggle;

    private Fragment mFragment;
    private Toolbar mToolbar;
    private AppBarLayout mAppBar;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final int INTRO_REQUEST = 1;
    private boolean mDestroyed = false;

    private AlertDialog mAlertDialog;
    private SharedPreferences mSharedPreferences = null;
    public final String APP_PREF = "evendate_pref";
    public final String FIRST_RUN = "first_run";
    public boolean isFirstRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubscriptionAdapter = new SubscriptionsAdapter(this);
        mSubscriptionLoader = new SubscriptionLoader(this);
        mMeLoader = new MeLoader(this);
        mSubscriptionLoader.setLoaderListener(this);
        mMeLoader.setLoaderListener(new LoaderListener<UserDetail>() {
            @Override
            public void onLoaded(UserDetail user) {
                mAccountAdapter.setAccountInfo(user);
            }

            @Override
            public void onError() {
                mAccountAdapter.setAccountInfoOffline();
            }
        });
        mAccountsAdapter = new AccountsAdapter(this);
        mAccountAdapter = new AccountAdapter(this);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name){
            //change menu in nav drawer to provide possibility to change selected item
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(!mAccountToggle.isChecked()){
                    setupActionMenu();
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
        mToolbar = toolbar;
        mAppBar = (AppBarLayout)findViewById(R.id.app_bar_layout);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new MainNavigationItemSelectedListener(this));


        mAccountToggle = (ToggleButton)mNavigationView.getHeaderView(0).findViewById(R.id.account_view_icon_button);
        RelativeLayout navHeader = (RelativeLayout)mNavigationView.getHeaderView(0);
        navHeader.setOnClickListener(new NavigationHeaderOnClickListener());
        mAccountToggle.setOnCheckedChangeListener(new ToggleAccountOnCheckedChangeListener());
        checkPlayServices();

        checkAccount();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = new MainPagerFragment();
        mSharedPreferences = getSharedPreferences(APP_PREF, MODE_PRIVATE);
        if (mSharedPreferences.getBoolean(FIRST_RUN, true)) {
            isFirstRun = true;
            mSharedPreferences.edit().putBoolean(FIRST_RUN, false).apply();
        }
        else
            fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isFirstRun){
            mSubscriptionLoader.getSubscriptions();
            mMeLoader.getData();
        }
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
        if(mAlertDialog != null)
            mAlertDialog.cancel();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
    public void onLoaded(ArrayList<Organization> subList) {
        if(isDestroyed())
            return;
        mSubscriptionAdapter.setSubscriptions(subList);
        if(!mAccountToggle.isChecked())
            setupActionMenu();
    }

    @Override
    public void onError() {
        if(isDestroyed())
            return;
        mAlertDialog = ErrorAlertDialogBuilder.newInstance(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSubscriptionLoader.getSubscriptions();
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
    }

    @Override
    public void onRefresh() {
        mSubscriptionLoader.getSubscriptions();
    }

    /**
     * handle clicks on items of navigation drawer list
     * it's maybe actions or accounts menu
     */
    private class MainNavigationItemSelectedListener
            implements NavigationView.OnNavigationItemSelectedListener{
        private Context mContext;

        public MainNavigationItemSelectedListener(Context context) {
            mContext = context;
        }

        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            menuItem.setChecked(true);
            switch (menuItem.getItemId()) {
                case R.id.reel:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof MainPagerFragment)){
                        mFragment = new MainPagerFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                        drawerLayout.closeDrawers();
                    }
                    checkedMenuItemId = menuItem.getItemId();
                    menuItem.setChecked(true);
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(0f);
                    return true;
                }
                case R.id.settings:
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.calendar:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof CalendarFragment)){
                        mFragment = new CalendarFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                        drawerLayout.closeDrawers();
                    }
                    checkedMenuItemId = menuItem.getItemId();
                    menuItem.setChecked(true);
                    // Converts 4 dip into its equivalent px
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(px);
                    return true;
                }
                case R.id.organizations:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof OrganizationCatalogFragment)){
                        mFragment = new OrganizationCatalogFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                        drawerLayout.closeDrawers();
                    }
                    checkedMenuItemId = menuItem.getItemId();
                    menuItem.setChecked(true);
                    // Converts 4 dip into its equivalent px
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(px);
                    return true;
                }
                case R.id.nav_add_account:
                    Intent authIntent = new Intent(mContext, AuthActivity.class);
                    startActivity(authIntent);
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.nav_account_management:
                    Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                    //TODO не робит(
                    intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.content_authority));
                    intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.account_type));
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    return true;
                default:
                    if(!mAccountToggle.isChecked()){
                        //open organization from subs
                        Intent detailIntent = new Intent(mContext, OrganizationDetailActivity.class);
                        detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                                .buildUpon().appendPath(Long.toString(menuItem.getItemId())).build());
                        startActivity(detailIntent);
                    }
                    else{
                        //change account
                        //TODO does it work? Need to remove this fucking code with direct operating on SharedPref
                        //String account_name = String.valueOf(menuItem.getTitle());
                        //SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, MODE_PRIVATE);
                        //SharedPreferences.Editor ed = sPref.edit();
                        //ed.putString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, account_name);
                        //ed.apply();
                        //setAccountInfo();
                        mAccountsAdapter.updateAccountMenu();
                    }
                    drawerLayout.closeDrawers();
                    return true;
            }
        }
    }

    /**
     * handle clicks on navigation drawer header
     */
    private class NavigationHeaderOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v instanceof RelativeLayout)
                if (mAccountToggle.isChecked()) {
                    setupActionMenu();
                } else {
                    setupAccountMenu();
                }
        }
    }
    private class ToggleAccountOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                setupAccountMenu();
            } else {
                setupActionMenu();
            }
        }
    }

    private void setupAccountMenu(){
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.drawer_accounts);
        mAccountsAdapter.updateAccountMenu();
        mAccountToggle.setChecked(true);
    }
    private void setupActionMenu(){
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.drawer_actions);
        mSubscriptionAdapter.updateSubscriptionMenu();
        mAccountToggle.setChecked(false);
    }
    /**
     * update menu with accounts and clear it if it's already existed
     */
    private class AccountsAdapter{
        private Context mContext;
        private SubMenu mAccountMenu;
        private Account[] mAccounts;
        private String AccountName;

        public AccountsAdapter(Context context) {
            mContext = context;
        }

        private void updateAccountMenu(){
            setAccounts();
            Menu navigationDrawerMenu = mNavigationView.getMenu();
            if(mAccountMenu != null){
                mAccountMenu.clear();
            }
            mNavigationView.setItemIconTintList(null);

            mAccountMenu = navigationDrawerMenu.addSubMenu(R.id.nav_accounts, 0, 0, R.string.accounts);

            if (AccountName == null)
                return;
            if(mAccounts.length == 0)
                return;
            for(Account account : mAccounts){
                if(!account.name.equals(AccountName)){
                    mAccountMenu.add(0, 0, 0, account.name);
                }
            }
        }
        private void setAccounts(){
            AccountManager accountManager =
                    (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);

            SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
            AccountName = sPref.getString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, null);
            mAccounts = accountManager.getAccountsByType(getString(R.string.account_type));
        }
    }
    /**
     * update menu with subs and clear it if it's already existed
     */
    private class SubscriptionsAdapter{
        private Context mContext;
        private SubMenu mOrganizationMenu;
        private ArrayList<Organization> mSubscriptions;

        public SubscriptionsAdapter(Context context) {
            mContext = context;
        }
        public void setSubscriptions(ArrayList<Organization> subs){
            mSubscriptions = subs;
        }
        public void updateSubscriptionMenu(){
            Menu navigationDrawerMenu = mNavigationView.getMenu();
            if(mOrganizationMenu != null){
                mOrganizationMenu.clear();
            }
            mOrganizationMenu = navigationDrawerMenu
                    .addSubMenu(R.id.nav_organizations, 0, 0, R.string.subscriptions);
            mNavigationView.setItemIconTintList(null);
            if(mSubscriptions != null){
                for(Organization sub : mSubscriptions){
                    MenuItem menuItem = mOrganizationMenu.add(0, sub.getEntryId(), 0,
                            sub.getShortName());
                    menuItem.setVisible(false);
                    MenuIconTask menuIconTask = new MenuIconTask(mContext, menuItem);
                    menuIconTask.execute(sub.getLogoUrl());
                }
            }
            mNavigationView.getMenu().findItem(checkedMenuItemId).setChecked(true);
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
    }

    private class AccountAdapter{
        private Context mContext;
        RelativeLayout header;
        TextView email;
        ImageView avatar;
        TextView username;

        public AccountAdapter(Context context) {
            mContext = context;
        }
        private void init(){
            header = (RelativeLayout)mNavigationView.getHeaderView(0);
            email = (TextView)header.findViewById(R.id.email);
            avatar = (ImageView)header.findViewById(R.id.avatar);
            username = (TextView)header.findViewById(R.id.username);
        }
        public void setAccountInfo(UserDetail user){
            init();
            SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
            String account_name = sPref.getString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, null);
            email.setText(account_name);
            String name = user.getFirstName() + " " + user.getLastName();
            username.setText(name);

            Picasso.with(mContext)
                    .load(user.getAvatarUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(avatar);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(EvendateSyncAdapter.FIRST_NAME, user.getFirstName());
            ed.putString(EvendateSyncAdapter.AVATAR_URL, user.getAvatarUrl());
            ed.putString(EvendateSyncAdapter.LAST_NAME, user.getLastName());
            ed.apply();
        }
        public void setAccountInfoOffline(){
            init();

            SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, Context.MODE_PRIVATE);
            String account_name = sPref.getString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, null);
            String first_name = sPref.getString(EvendateSyncAdapter.FIRST_NAME, null);
            String last_name = sPref.getString(EvendateSyncAdapter.LAST_NAME, null);
            String name = first_name + " " + last_name;
            init();
            email.setText(account_name);
            username.setText(name);

            Picasso.with(mContext)
                    .load(sPref.getString(EvendateSyncAdapter.AVATAR_URL, null))
                    .error(R.mipmap.ic_launcher)
                    .into(avatar);
        }
    }
}
