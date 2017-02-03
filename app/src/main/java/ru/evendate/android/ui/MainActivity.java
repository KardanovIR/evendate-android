package ru.evendate.android.ui;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.auth.AuthActivity;
import ru.evendate.android.network.ServiceImpl;


public class MainActivity extends AppCompatActivity implements ReelFragment.OnRefreshListener, OnboardingDialog.OnOrgSelectedListener {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    private MainPagerFragment mFragment;
    private DrawerWrapper mDrawer;

    public static final int REQUEST_AUTH = 1;
    public static final String SHOW_ONBOARDING = "onboarding";
    private static boolean requestOnboarding = false;
    OnboardingDialog onboarding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTransitions();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbar();
        initDrawer();

        mFragment = new MainPagerFragment();
        mFragment.setOnRefreshListener(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();

        checkAccount();
        checkDeviceTokenSynced();

        if(getIntent() != null)
            handleIntent(getIntent());
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setNavigationOnClickListener((View v) -> mDrawer.getDrawer().openDrawer());
    }
    private void initTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Fade());
            getWindow().setReenterTransition(new Fade());
            getWindow().setExitTransition(new Fade());
        }
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new MainNavigationItemClickListener(this, mDrawer.getDrawer()));
        mDrawer.setListener(() -> {
            if(mDrawer.getSubs().size() == 0)
                showOnboarding();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchResultsActivity.class);
                startActivity(searchIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        requestOnboarding = intent.getBooleanExtra(SHOW_ONBOARDING, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO cause auth flow
        mDrawer.getDrawer().setSelection(DrawerWrapper.REEL_IDENTIFIER);
        mDrawer.start();
        if(requestOnboarding){
            forceShowOnboarding();
            requestOnboarding = false;
        }
    }

    /**
     * check account exists and start auth if no
     */
    private void checkAccount() {
        Account account = EvendateAccountManager.getSyncAccount(this);
        if (account == null) {
            Intent authIntent = new Intent(this, AuthActivity.class);

            //if(Build.VERSION.SDK_INT >= 21) {
                //startActivityForResult(authIntent, REQUEST_AUTH, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            //}else
                startActivityForResult(authIntent, REQUEST_AUTH);
        }
    }

    private void checkDeviceTokenSynced() {
        String token = EvendatePreferences.getDeviceToken(this);
        Log.d(MainActivity.class.getSimpleName(), "checking device token synced for: " + token);
        if (!EvendatePreferences.getDeviceTokenSynced(this)) {
            ServiceImpl.sendRegistrationToServer(this, token);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public void onRefresh() {
        mDrawer.update();
    }

    private void showOnboarding() {
        if (onboarding != null)
            return;
        onboarding = new OnboardingDialog();
        onboarding.setOnOrgSelectedListener(this);
        onboarding.show(getSupportFragmentManager(), "onboarding");
    }

    private void forceShowOnboarding() {
        if(getSupportFragmentManager().findFragmentByTag("onboarding") != null)
            return;
        onboarding = new OnboardingDialog();
        onboarding.setOnOrgSelectedListener(this);
        onboarding.show(getSupportFragmentManager(), "onboarding");
    }

    @Override
    public void onOrgSelected() {
        mFragment.refresh();
        mDrawer.update();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (onboarding != null)
            onboarding.dismissAllowingStateLoss();
    }

    /**
     * handle clicks on items of navigation drawer list in main activity
     */
    private class MainNavigationItemClickListener extends NavigationItemSelectedListener {

        MainNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
                case DrawerWrapper.REEL_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.getDrawer().isDrawerOpen()) {
            mDrawer.getDrawer().closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
