package ru.evendate.android.ui.feed;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.network.ServiceImpl;
import ru.evendate.android.ui.AuthHandler;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.cities.CityActivity;
import ru.evendate.android.ui.search.SearchResultsActivity;

import static ru.evendate.android.ui.cities.CityActivity.KEY_PROMPT;


public class MainActivity extends BaseActivity implements ReelFragment.OnRefreshListener,
        OnboardingDialog.OnOrgSelectedListener, AuthHandler {

    private static final int REQUEST_SELECT_CITY = 2;
    public static final String SHOW_ONBOARDING = "onboarding";
    private static final String TAG_ONBOARDING = "tag_onboarding";
    private static boolean requestOnboarding = false;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    private OnboardingDialog onboarding;
    private MainPagerFragment mFragment;

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


        if (getIntent() != null)
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

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new MainNavigationItemClickListener(this, mDrawer.getDrawer()));
        mDrawer.setListener(() -> {
            if (mDrawer.getSubs().size() == 0 && !EvendateAccountManager.getOnboardingDone(this)
                    && EvendateAccountManager.getAccount(this) != null) {
                showOnboarding();
                EvendateAccountManager.setOnboardingDone(this);
            }
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

    private void handleIntent(Intent intent) {
        requestOnboarding = intent.getBooleanExtra(SHOW_ONBOARDING, false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkDeviceTokenSynced();
        if (!checkCity()) {
            startSelectCity();
            return;
        }

        if (requestOnboarding) {
            showOnboarding();
            requestOnboarding = false;
        }
        mDrawer.getDrawer().setSelection(DrawerWrapper.REEL_IDENTIFIER);
    }

    /**
     * check user city selected and start if no
     */
    private boolean checkCity() {
        return EvendatePreferences.newInstance(this).getUserCitySelected();
    }

    private void startSelectCity() {
        Intent cityIntent = new Intent(this, CityActivity.class);
        cityIntent.putExtra(KEY_PROMPT, true);
        startActivityForResult(cityIntent, REQUEST_SELECT_CITY);
    }

    private void checkDeviceTokenSynced() {
        String token = EvendatePreferences.getDeviceToken(this);
        Log.d(MainActivity.class.getSimpleName(), "checking device token synced for: " + token);
        if (!EvendatePreferences.isDeviceTokenSynced(this)) {
            ServiceImpl.sendRegistrationToServer(this, token);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_CITY) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public void onRefresh() {
        onReload();
    }

    @Override
    protected void onReload() {
        super.onReload();
        mFragment.reload();
    }

    private void showOnboarding() {
        onboarding = (OnboardingDialog)getSupportFragmentManager().findFragmentByTag(TAG_ONBOARDING);
        if (onboarding != null)
            return;
        onboarding = new OnboardingDialog();
        onboarding.setOnOrgSelectedListener(this);
        onboarding.show(getSupportFragmentManager(), TAG_ONBOARDING);
    }

    @Override
    public void onOrgSelected() {
        // todo change to observer, потому что нужно вызывать после запросов на подписку, а это тупо по клику кнопки ОК
        onReload();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (onboarding != null)
            onboarding.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.getDrawer().isDrawerOpen()) {
            mDrawer.getDrawer().closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * handle clicks on items of navigation drawer list in main activity
     */
    private class MainNavigationItemClickListener extends DrawerWrapper.NavigationItemSelectedListener {

        MainNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch ((int)drawerItem.getIdentifier()) {
                case DrawerWrapper.REEL_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }
}
