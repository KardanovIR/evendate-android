package ru.evendate.android.ui.orgdetail;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.network.Response;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.feed.ReelFragment;
import ru.evendate.android.ui.feed.ReelPresenter;
import ru.evendate.android.ui.users.UserListActivity;
import ru.evendate.android.ui.users.UserListFragment;
import ru.evendate.android.ui.utils.UsersFormatter;
import ru.evendate.android.views.LoadStateView;
import ru.evendate.android.views.UserFavoritedCard;

import static ru.evendate.android.ui.feed.ReelFragment.ReelType.ORGANIZATION;
import static ru.evendate.android.ui.feed.ReelFragment.ReelType.ORGANIZATION_PAST;
import static ru.evendate.android.ui.utils.UiUtils.revealView;

public class OrganizationDetailActivity extends BaseActivity implements LoadStateView.OnReloadListener {

    public static final String URI_KEY = "uri";
    final int TITLE_SHIFTED_BY_BUTTON = 2;
    private final String LOG_TAG = "OrganizationActivity";
    @BindView(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.org_toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.org_title) TextView mOrgTitle;
    @BindView(R.id.organization_subscribe_button) ToggleButton mSubscribeButton;
    @BindView(R.id.organization_image_container) View mImageContainer;
    @BindView(R.id.organization_image) ImageView mBackgroundView;
    @BindView(R.id.organization_image_foreground) ImageView mForegroundView;
    @BindView(R.id.organization_icon) CircleImageView mIconView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tabs) TabLayout mTabs;
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private final Target backgroundTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mLoadStateView.hideProgress();
            mBackgroundView.setImageBitmap(bitmap);
            tintImageShadow();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mBackgroundView.setImageDrawable(errorDrawable);
            revealView(mAppBarLayout);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };
    private ObjectAnimator mTitleAppearAnimation;
    private ObjectAnimator mTitleDisappearAnimation;
    private boolean loaded = false;
    private int organizationId = -1;
    private Uri mUri;
    private OrganizationPagerAdapter mPagerAdapter;
    private OrganizationDetail mOrganization;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);

        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        mUri = intent.getData();
        organizationId = Integer.parseInt(mUri.getLastPathSegment());
        new Statistics(this).sendOrganizationView(organizationId);

        initInterface();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(URI_KEY, mUri.toString());
    }

    private void initInterface() {
        ButterKnife.bind(this);

        initToolbar();
        initTransitions();
        initDrawer();

        mPagerAdapter = new OrganizationPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mPagerAdapter);

        mTabs.setupWithViewPager(mViewPager);
        setupPagerStat();
        mLoadStateView.setOnReloadListener(this);
        mLoadStateView.showProgress();

        mAppBarLayout.setVisibility(View.INVISIBLE);
        mViewPager.setVisibility(View.INVISIBLE);
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbarTitle.setAlpha(0f);

        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int actionBarHeight = (int)styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        mCollapsingToolbar.setScrimVisibleHeightTrigger(actionBarHeight * TITLE_SHIFTED_BY_BUTTON);
        mCollapsingToolbar.setScrimAnimationDuration(200);

        mAppBarLayout.addOnOffsetChangedListener((AppBarLayout appBarLayout, int verticalOffset) -> {
            if (mOrganization == null)
                return;
            if (mAppBarLayout.getTotalScrollRange() - Math.abs(verticalOffset) <= mToolbar.getHeight() * TITLE_SHIFTED_BY_BUTTON) {
                animateAppearToolbar();
            } else {
                animateDisappearToolbar();
            }
        });
    }

    private void initTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
            getWindow().setExitTransition(new Slide(Gravity.TOP));
        }
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new DrawerWrapper.NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    private void animateAppearToolbar() {
        //mToolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
        if (mTitleDisappearAnimation != null && mTitleDisappearAnimation.isRunning())
            mTitleDisappearAnimation.cancel();
        if (mTitleAppearAnimation == null || !mTitleAppearAnimation.isRunning()) {
            mTitleAppearAnimation = ObjectAnimator.ofFloat(mToolbarTitle, "alpha",
                    mToolbarTitle.getAlpha(), 1f);
            mTitleAppearAnimation.setDuration(200);
            mTitleAppearAnimation.start();
            if (Build.VERSION.SDK_INT >= 21) {
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                        getResources().getDisplayMetrics());
                mAppBarLayout.setElevation(px);
            }
        }
    }

    private void animateDisappearToolbar() {
        //mToolbar.setBackgroundColor(Color.TRANSPARENT);
        if (mTitleAppearAnimation != null && mTitleAppearAnimation.isRunning())
            mTitleAppearAnimation.cancel();
        if (mTitleDisappearAnimation == null || !mTitleDisappearAnimation.isRunning()) {
            mTitleDisappearAnimation = ObjectAnimator.ofFloat(mToolbarTitle, "alpha",
                    mToolbarTitle.getAlpha(), 0f);
            mTitleDisappearAnimation.setDuration(200);
            mTitleDisappearAnimation.start();
            if (Build.VERSION.SDK_INT >= 21)
                mAppBarLayout.setElevation(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.org_detail_menu, menu);
        return true;
    }

    /**
     * setup screen names of fragments for statistic screen tracking
     */
    private void setupPagerStat() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Tracker tracker = EvendateApplication.getTracker();
                tracker.setScreenName("Organization Screen ~" +
                        mPagerAdapter.getPageLabel(position));
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onUpPressed();
                return true;
            case R.id.action_info:
                OrganizationInfo fragment = OrganizationInfo.newInstance(
                        mOrganization.getBrandColor(), mOrganization.getBrandColorAccent());
                fragment.setOrganization(mOrganization);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_content, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.start();
        if (!loaded) {
            loadOrg();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.dispose();
        Picasso.with(this).cancelRequest(mBackgroundView);
    }

    private void loadOrg() {
        mDisposable = new DataRepository(this)
                .getOrg(EvendateAccountManager.peekToken(this), organizationId)
                .subscribe(result -> onLoaded(new ArrayList<>(result.getData())), this::onError);
    }


    private void onLoaded(ArrayList<OrganizationFull> organizations) {
        loaded = true;
        mOrganization = organizations.get(0);
        Picasso.with(this)
                .load(mOrganization.getBackgroundMediumUrl())
                .error(R.drawable.default_background)
                .noFade()
                .into(backgroundTarget);
        Picasso.with(this)
                .load(mOrganization.getLogoMediumUrl())
                .error(R.mipmap.ic_launcher)
                .into(mIconView);
        mSubscribeButton.setChecked(mOrganization.isSubscribed());
        mOrgTitle.setText(mOrganization.getName());
        mToolbarTitle.setText(mOrganization.getShortName());
        tintByBrandColor();
    }

    private void tintByBrandColor() {
        //todo handle illegal argument (bad color string)
        TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        if (mOrganization.getBrandColor() != null && !mOrganization.getBrandColor().isEmpty()) {
            int brandColor = Color.parseColor(mOrganization.getBrandColor());
            mAppBarLayout.setBackgroundColor(brandColor);
            mCollapsingToolbar.setContentScrimColor(brandColor);
            tintStatusBar(brandColor);
        }
        if (mOrganization.getBrandColorAccent() != null && !mOrganization.getBrandColorAccent().isEmpty()) {
            int brandColorAccent = Color.parseColor(mOrganization.getBrandColorAccent());
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, new ColorDrawable(brandColorAccent));
            stateListDrawable.addState(new int[]{android.R.attr.state_checked}, new ColorDrawable(Color.WHITE));
            mSubscribeButton.setBackgroundDrawable(stateListDrawable);
            mTabs.setSelectedTabIndicatorColor(brandColorAccent);
        }
    }

    private void tintStatusBar(int color) {

        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        int colorDark = Color.argb(150, red, green, blue);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(colorDark);
        }
    }

    private void tintImageShadow() {
        int color;
        if (mOrganization.getBrandColor() != null) {
            color = Color.parseColor(mOrganization.getBrandColor());
        } else {
            color = ContextCompat.getColor(getBaseContext(), R.color.primary);
        }

        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        int colorShadow = Color.argb(255, red, green, blue);
        int colorShadow2 = Color.argb(150, red, green, blue);
        int colorShadowEnd = Color.argb(0, red, green, blue);
        GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{colorShadow, colorShadow2, colorShadowEnd});
        mForegroundView.setImageDrawable(shadow);
        revealView(mAppBarLayout);
        TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        mViewPager.setVisibility(View.VISIBLE);
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    public void onReload() {
        super.onReload();
        loadOrg();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.organization_subscribe_button)
    public void onSubscribeClick(View v) {
        int organizationId = mOrganization.getEntryId();
        String token = EvendateAccountManager.peekToken(this);

        if (token == null) {
            requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(this::subscribe);
        } else {
            subscribe(token);
        }
    }

    private void subscribe(String token) {
        Observable<Response> subOrganizationObservable;
        if (mOrganization.isSubscribed()) {
            subOrganizationObservable = new DataRepository(this).unSubscribeOrg(token, organizationId);
        } else {
            subOrganizationObservable = new DataRepository(this).subscribeOrg(token, organizationId);
        }
        subOrganizationObservable
                .subscribe(result -> {
                    if (result.isOk()) {
                        Log.i(LOG_TAG, "subscription applied");
                        Statistics googleStatistics = new Statistics(getBaseContext());
                        if (mOrganization.isSubscribed()) {
                            googleStatistics.sendOrganizationSubAction(organizationId);
                        } else {
                            googleStatistics.sendOrganizationUnsubAction(organizationId);
                        }
                    } else
                        Log.e(LOG_TAG, "Error with response with organization sub");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage()));

        mOrganization.changeSubscriptionState();

        if (mOrganization.isSubscribed()) {
            Snackbar.make(mCoordinatorLayout, R.string.organization_subscription_confirm, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mCoordinatorLayout, R.string.organization_subscription_remove_confirm, Snackbar.LENGTH_LONG).show();
        }
    }

    public static class OrganizationInfo extends Fragment {
        private static final String ORG_OBJ_KEY = "organization";
        private static final String BRAND_COLOR_KEY = "brand_color";
        private static final String BRAND_COLOR_ACCENT_KEY = "brand_color_accent";
        OrganizationDetail mOrganization;

        @BindView(R.id.toolbar) Toolbar mToolbar;
        @BindView(R.id.user_card) UserFavoritedCard mUserFavoritedCard;
        @BindView(R.id.organization_name) TextView mOrganizationTextView;
        @BindView(R.id.organization_description) TextView mDescriptionTextView;
        @BindView(R.id.organization_place_text) TextView mPlacePlaceTextView;
        @BindView(R.id.org_place_button) TextView mOrgPlaceButton;
        @BindView(R.id.org_link_button) TextView mOrgLinkButton;
        private Unbinder unbinder;
        int brandColor;
        int brandColorAccent;

        public static OrganizationInfo newInstance(@Nullable String brandColor, @Nullable String brandColorAccent) {
            OrganizationInfo fragment = new OrganizationInfo();
            fragment.brandColor = brandColor == null || brandColor.isEmpty() ? 0 : Color.parseColor(brandColor);
            fragment.brandColorAccent = brandColorAccent == null || brandColorAccent.isEmpty() ? 0 : Color.parseColor(brandColorAccent);
            return fragment;
        }

        public void setOrganization(OrganizationDetail organization) {
            mOrganization = organization;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                mOrganization = new Gson().fromJson(savedInstanceState.getString(ORG_OBJ_KEY),
                        OrganizationFull.class);
                brandColor = savedInstanceState.getInt(BRAND_COLOR_KEY);
                brandColorAccent = savedInstanceState.getInt(BRAND_COLOR_ACCENT_KEY);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_organization_info, container, false);
            unbinder = ButterKnife.bind(this, rootView);

            mToolbar.setNavigationIcon(R.drawable.ic_clear_white);
            mToolbar.setNavigationOnClickListener((View v) -> getActivity().onBackPressed());
            mToolbar.setTitle(mOrganization.getShortName());
            if (brandColor != 0) {
                mToolbar.setBackgroundColor(brandColor);
            }
            initUserFavoriteCard();
            setOrg();
            return rootView;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(ORG_OBJ_KEY, new Gson().toJson(mOrganization));
            outState.putInt(BRAND_COLOR_KEY, brandColor);
            outState.putInt(BRAND_COLOR_ACCENT_KEY, brandColorAccent);
        }

        private void initUserFavoriteCard() {
            mUserFavoritedCard.setOnAllButtonListener((View v) -> onUsersClicked());
        }

        private void setOrg() {
            mOrganizationTextView.setText(mOrganization.getName());
            mDescriptionTextView.setText(mOrganization.getDescription());
            mUserFavoritedCard.setTitle(UsersFormatter.formatUsers(getContext(), mOrganization));
            if (mOrganization.getSubscribedUsersList().size() == 0) {
                mUserFavoritedCard.setVisibility(View.GONE);
            }
            mUserFavoritedCard.setUsers(mOrganization.getSubscribedUsersList());
            mPlacePlaceTextView.setText(mOrganization.getDefaultAddress());
            tintBrandColor();
        }

        private void tintBrandColor() {
            if (brandColor == 0)
                return;
            mUserFavoritedCard.tintAllButton(brandColorAccent);
            mOrgPlaceButton.setTextColor(brandColorAccent);
            mOrgLinkButton.setTextColor(brandColorAccent);
        }

        /**
         * handle link button click and open organization page in browser
         */
        @SuppressWarnings("unused")
        @OnClick(R.id.org_link_card)
        public void onLinkClick() {
            new Statistics(getActivity()).sendOrganizationClickLinkAction(mOrganization.getEntryId());
            Intent openLink = new Intent(Intent.ACTION_VIEW);
            openLink.setData(Uri.parse(mOrganization.getSiteUrl()));
            startActivity(openLink);
        }

        /**
         * handle place button click and open google map
         */
        @SuppressWarnings("unused")
        @OnClick(R.id.org_place_card)
        public void onPlaceClicked() {
            new Statistics(getActivity()).sendOrgOpenMap(mOrganization.getEntryId());
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + mOrganization.getDefaultAddress());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }

        /**
         * handle all user click and open user list for organization
         */
        public void onUsersClicked() {
            Intent intent = new Intent(getContext(), UserListActivity.class);
            intent.setData(EvendateContract.EventEntry.getContentUri(mOrganization.getEntryId()));
            intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.ORGANIZATION.type());
            if (Build.VERSION.SDK_INT >= 21) {
                getActivity().startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            } else
                getActivity().startActivity(intent);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }

    private class OrganizationPagerAdapter extends FragmentPagerAdapter {
        private final int TAB_COUNT = 2;
        private final int FUTURE_TAB = 0;
        private final int PAST_TAB = 1;
        ReelFragment mFutureReelFragment;
        ReelFragment mPastReelFragment;
        private Context mContext;

        OrganizationPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            mContext = context;
            DataSource dataSource = new DataRepository(mContext);
            mFutureReelFragment = ReelFragment.newInstance(ORGANIZATION, organizationId, false);
            ReelPresenter.newInstance(dataSource, mFutureReelFragment, ORGANIZATION, organizationId);
            mPastReelFragment = ReelFragment.newInstance(ORGANIZATION_PAST, organizationId, false);
            ReelPresenter.newInstance(dataSource, mPastReelFragment, ORGANIZATION_PAST, organizationId);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case FUTURE_TAB: {
                    return mFutureReelFragment;
                }
                case PAST_TAB: {
                    return mPastReelFragment;
                }
                default:
                    throw new IllegalArgumentException("invalid page number");
            }
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case FUTURE_TAB:
                    return mContext.getString(R.string.tab_organization_future_events);
                case PAST_TAB:
                    return mContext.getString(R.string.tab_organization_past_events);
                default:
                    return null;
            }
        }

        /**
         * return strings for statistics
         */
        String getPageLabel(int position) {
            switch (position) {
                case FUTURE_TAB:
                    return mContext.getString(R.string.stat_page_organization_events);
                case PAST_TAB:
                    return mContext.getString(R.string.stat_page_organization_past_events);
                default:
                    return null;
            }
        }
    }
}
