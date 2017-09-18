package ru.evendate.android.ui.userdetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.views.LoadStateView;

import static ru.evendate.android.ui.utils.UiUtils.revealView;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserProfileActivity extends BaseActivity implements LoadStateView.OnReloadListener {
    private final static String LOG_TAG = UserProfileActivity.class.getSimpleName();
    public static final String URI = "uri";
    public static final String USER_ID = "user_id";
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(R.id.user_avatar) ImageView mUserImageView;
    @BindView(R.id.avatar_container) View mUserImageContainer;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private Uri mUri;
    private int userId;
    private UserAdapter mUserAdapter;
    private UserPagerAdapter mUserPagerAdapter;

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt(USER_ID, userId);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        userId = savedInstanceState.getInt(USER_ID);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        initToolbar();
        initDrawer();
        initTransitions();

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            userId = Integer.parseInt(intent.getData().getLastPathSegment());
            new Statistics(this).sendUserView(userId);
        }

        mUserAdapter = new UserAdapter();
        setupStat();
        mUserImageContainer.setVisibility(View.INVISIBLE);
        mLoadStateView.setOnReloadListener(this);

        loadUser();
        mLoadStateView.showProgress();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onUpPressed();
                return true;
            case R.id.action_user_link:
                if (mUserAdapter.getUser() != null) {
                    String url = mUserAdapter.getUser().getLink();
                    Intent linkIntent = new Intent(Intent.ACTION_VIEW);
                    linkIntent.setData(Uri.parse(url));
                    startActivity(linkIntent);
                    new Statistics(this).sendUserClickLinkAction(userId);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadUser() {
        new DataRepository(this).getUser(EvendateAccountManager.peekToken(this), userId)
                .subscribe(result -> {
                            if (!result.isOk())
                                mLoadStateView.showErrorHint();
                            else
                                onLoaded(result.getData());
                        }, this::onError,
                        mLoadStateView::hideProgress);
    }

    private void onLoaded(ArrayList<UserDetail> users) {
        mUserAdapter.setUser(users.get(0));
        mUserPagerAdapter = new UserPagerAdapter(getSupportFragmentManager(), this, mUserAdapter.getUser());
        mViewPager.setAdapter(mUserPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    public void onReload() {
        super.onReload();
        loadUser();
    }

    private void setupStat() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Tracker tracker = EvendateApplication.getTracker();
                tracker.setScreenName("User Profile Screen ~" +
                        mUserPagerAdapter.getPageLabel(position));
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private class UserAdapter {
        private UserDetail mUserDetail;

        public UserDetail getUser() {
            return mUserDetail;
        }

        public void setUser(UserDetail user) {
            mUserDetail = user;
            setUserInfo();
        }

        private void setUserInfo() {
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mUserImageView.setImageBitmap(bitmap);
                    revealView(mUserImageContainer);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            String userName = mUserDetail.getFirstName() + " " + mUserDetail.getLastName();
            mCollapsingToolbar.setTitle(userName);
            Picasso.with(getBaseContext())
                    .load(mUserDetail.getAvatarUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(target);
        }
    }

    private class UserPagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private UserDetail mUser;

        private UserPagerAdapter(FragmentManager fragmentManager, Context context, UserDetail user) {
            super(fragmentManager);
            mContext = context;
            mUser = user;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return UserSubscriptionsFragment.newInstance(mUser);
                }
                case 1: {
                    return UserActionsFragment.newInstance(mUser.getEntryId());
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
            switch (position) {
                case 0:
                    return mContext.getString(R.string.tab_user_subscriptions);
                case 1:
                    return mContext.getString(R.string.tab_user_actions);
                default:
                    return null;
            }
        }

        /**
         * return strings for statistics
         *
         * @param position int
         * @return String
         */
        String getPageLabel(int position) {
            switch (position) {
                case 0:
                    return mContext.getString(R.string.stat_page_subscriptions);
                case 1:
                    return mContext.getString(R.string.stat_page_actions);
                default:
                    return null;
            }
        }
    }
}