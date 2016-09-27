package ru.evendate.android.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.Statistics;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.UsersFormatter;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.NetworkRequests;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.views.LoadStateView;
import ru.evendate.android.views.UserFavoritedCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static ru.evendate.android.ui.ReelFragment.ReelType.ORGANIZATION;
import static ru.evendate.android.ui.ReelFragment.ReelType.ORGANIZATION_PAST;
import static ru.evendate.android.ui.UiUtils.revealView;

/**
 * Contain details of organization
 */
public class OrganizationDetailFragment extends Fragment implements LoadStateView.OnReloadListener {
    private final String LOG_TAG = "OrganizationFragment";

    private int organizationId = -1;
    public static final String URI = "uri";
    private Uri mUri;

    @Bind(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.organization_subscribe_button) ToggleButton mSubscribeButton;
    @Bind(R.id.organization_image_container) View mImageContainer;
    @Bind(R.id.organization_image) ImageView mBackgroundView;
    @Bind(R.id.organization_image_foreground) ImageView mForegroundView;
    @Bind(R.id.organization_icon) CircleImageView mIconView;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.tabs) TabLayout mTabs;
    @Bind(R.id.pager) ViewPager mViewPager;
    private OrganizationPagerAdapter mPagerAdapter;
    private DrawerWrapper mDrawer;
    private OrganizationDetail mOrganization;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;

    final Target backgroundTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mLoadStateView.hideProgress();
            mBackgroundView.setImageBitmap(bitmap);

            int color = ContextCompat.getColor(getActivity(), R.color.primary);

            int red = Color.red(color);
            int blue = Color.blue(color);
            int green = Color.green(color);

            int colorShadow = Color.argb(255, red, green, blue);
            int colorShadow2 = Color.argb(150, red, green, blue);
            int colorShadowEnd = Color.argb(0, red, green, blue);
            GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{colorShadow, colorShadow2, colorShadowEnd});
            mForegroundView.setImageDrawable(shadow);
            if (isAdded())
                revealView(mAppBarLayout);
            if (Build.VERSION.SDK_INT > 19)
                TransitionManager.beginDelayedTransition(mCoordinatorLayout);
            mViewPager.setVisibility(View.VISIBLE);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mBackgroundView.setImageDrawable(errorDrawable);
            if (isAdded())
                revealView(mAppBarLayout);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mUri = Uri.parse(args.getString(URI));
            organizationId = Integer.parseInt(mUri.getLastPathSegment());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        initToolbar();
        initTransitions();
        initDrawer();

        mAppBarLayout.addOnOffsetChangedListener((AppBarLayout appBarLayout, int verticalOffset) -> {
            if (mOrganization == null)
                return;
            if (mAppBarLayout.getTotalScrollRange() - Math.abs(verticalOffset) <= 4 &&
                    !mToolbar.getTitle().equals(mOrganization.getShortName())) {
                mCollapsingToolbar.setTitle(mOrganization.getShortName());
            } else if (!mToolbar.getTitle().equals(mOrganization.getName())) {
                mCollapsingToolbar.setTitle(mOrganization.getName());
            }
        });

        mPagerAdapter = new OrganizationPagerAdapter(getChildFragmentManager(), getActivity());
        mViewPager.setAdapter(mPagerAdapter);

        mTabs.setupWithViewPager(mViewPager);
        setupPagerStat();
        mLoadStateView.setOnReloadListener(this);

        mAppBarLayout.setVisibility(View.INVISIBLE);
        mViewPager.setVisibility(View.INVISIBLE);
        return rootView;
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        mToolbar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    }

    private void initTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
            getActivity().getWindow().setExitTransition(new Slide(Gravity.TOP));
        }
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(getActivity());
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(getActivity(), mDrawer.getDrawer()));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.org_detail_menu, menu);
    }

    /**
     * setup screen names of fragments for statistic screen tracking
     */
    private void setupPagerStat() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Tracker tracker = EvendateApplication.getTracker();
                tracker.setScreenName("Organization Screen ~" +
                        mPagerAdapter.getPageLabel(position));
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onUpPressed();
                return true;
            case R.id.action_info:
                OrganizationInfo dialog = new OrganizationInfo();
                dialog.setOrganization(mOrganization);
                dialog.show(getChildFragmentManager(), "dialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadOrg();
        mDrawer.start();
    }

    //TODO DRY
    private void onUpPressed() {
        ActivityManager activityManager = (ActivityManager)getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getActivity().getClass().getName())) {
            Log.i(LOG_TAG, "This is last activity in the stack");
            getActivity().startActivity(NavUtils.getParentActivityIntent(getActivity()));
        } else {
            getActivity().onBackPressed();
        }
    }

    private void loadOrg() {
        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<ResponseArray<OrganizationFull>> organizationObservable =
                apiService.getOrganization(EvendateAccountManager.peekToken(getActivity()),
                        organizationId, OrganizationDetail.FIELDS_LIST);
        organizationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> onLoaded(result.getData()),
                        this::onError);
    }


    public void onLoaded(ArrayList<OrganizationFull> organizations) {
        if (!isAdded())
            return;
        mOrganization = organizations.get(0);
        Picasso.with(getActivity())
                .load(mOrganization.getBackgroundMediumUrl())
                .error(R.drawable.default_background)
                .noFade()
                .into(backgroundTarget);
        Picasso.with(getActivity())
                .load(mOrganization.getLogoMediumUrl())
                .error(R.mipmap.ic_launcher)
                .into(mIconView);
        mSubscribeButton.setChecked(mOrganization.isSubscribed());

    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    public void onReload() {
        loadOrg();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.organization_subscribe_button)
    public void onSubscribeClick(View v) {
        NetworkRequests networkRequests = new NetworkRequests(getActivity());
        networkRequests.subscribeOrg(mOrganization, mCoordinatorLayout);
    }

    class OrganizationPagerAdapter extends FragmentPagerAdapter {
        private final int TAB_COUNT = 2;
        private final int FUTURE_TAB = 0;
        private final int PAST_TAB = 1;
        private Context mContext;
        ReelFragment mFutureReelFragment;
        ReelFragment mPastReelFragment;

        OrganizationPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            mContext = context;
            mFutureReelFragment = ReelFragment.newInstance(ORGANIZATION, organizationId, false);
            mPastReelFragment = ReelFragment.newInstance(ORGANIZATION_PAST, organizationId, false);
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

    public static class OrganizationInfo extends DialogFragment {
        OrganizationDetail mOrganization;
        @Bind(R.id.toolbar) Toolbar mToolbar;
        @Bind(R.id.user_card) UserFavoritedCard mUserFavoritedCard;
        @Bind(R.id.organization_name) TextView mOrganizationTextView;
        @Bind(R.id.organization_description) TextView mDescriptionTextView;
        @Bind(R.id.organization_place_text) TextView mPlacePlaceTextView;

        public void setOrganization(OrganizationDetail organization) {
            mOrganization = organization;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final FrameLayout root = new FrameLayout(getActivity());
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            Dialog dialog = new Dialog(getActivity(), R.style.AppTheme_FullScreenDialogOverlay);
            dialog.setContentView(root);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return dialog;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_organization_info, container, false);
            ButterKnife.bind(this, rootView);

            mToolbar.setNavigationIcon(R.drawable.ic_clear_white);
            mToolbar.setNavigationOnClickListener((View v) -> dismiss());
            initUserFavoriteCard();
            setOrg();
            return rootView;
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
            mToolbar.setTitle(getString(R.string.organization_dialog_info_title));
        }

        /**
         * handle link button click and open organization page in browser
         */
        @SuppressWarnings("unused")
        @OnClick(R.id.org_link_card)
        public void onLinkClick() {
            Statistics.init(getActivity());
            Statistics.sendOrgOpenSite(mOrganization.getEntryId());
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
            Statistics.init(getActivity());
            Statistics.sendOrgOpenMap(mOrganization.getEntryId());
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
            intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(mOrganization.getEntryId())).build());
            intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.ORGANIZATION.type());
            if (Build.VERSION.SDK_INT >= 21) {
                getActivity().startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            } else
                getActivity().startActivity(intent);
        }

    }
}
