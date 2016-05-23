package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.adapters.UserPagerAdapter;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.UserLoader;
import ru.evendate.android.models.UserDetail;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserProfileActivity extends AppCompatActivity implements LoaderListener<ArrayList<UserDetail>> {
    private Uri mUri;
    private int userId;
    public static final String URI = "uri";
    public static final String USER_ID = "user_id";
    private UserAdapter mUserAdapter;
    private UserLoader mLoader;

    private ViewPager mViewPager;
    private UserPagerAdapter mUserPagerAdapter;
    private TabLayout mTabLayout;

    private ImageView mUserImageView;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private ProgressBar mProgressBar;
    EvendateDrawer mDrawer;

    public static final String INTENT_TYPE = "type";
    public static final String NOTIFICATION = "notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);
        //make status bar transparent
        //((AppBarLayout)findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new StatusBarColorChanger(this));

        Intent intent = getIntent();
        if (intent != null) {
            userId = Integer.parseInt(intent.getData().getLastPathSegment());
            Bundle intent_extras = getIntent().getExtras();
            Tracker tracker = EvendateApplication.getTracker();

            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.stat_category_user))
                    .setLabel(String.valueOf(userId));
            if (intent_extras != null && intent_extras.containsKey(INTENT_TYPE) &&
                    intent.getStringExtra(INTENT_TYPE).equals(NOTIFICATION)) {
                event.setAction(getString(R.string.stat_action_notification));
            } else {
                event.setAction(getString(R.string.stat_action_view));
            }
            tracker.send(event.build());
        }
        mCollapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        mUserImageView = (ImageView)findViewById(R.id.user_avatar);

        mLoader = new UserLoader(this, userId);
        mLoader.setLoaderListener(this);
        mUserAdapter = new UserAdapter();
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabLayout = (TabLayout)findViewById(R.id.tabs);

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        mDrawer = EvendateDrawer.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(this, mDrawer.getDrawer()));
        setupStat();
        mLoader.startLoading();
        mDrawer.start();
    }

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
    public void onLoaded(ArrayList<UserDetail> users) {
        mUserAdapter.setUser(users.get(0));
        mUserPagerAdapter = new UserPagerAdapter(getSupportFragmentManager(), this, mUserAdapter.getUser());
        mViewPager.setAdapter(mUserPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        mProgressBar.setVisibility(View.GONE);
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLoader.startLoading();
                mProgressBar.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class UserAdapter {
        private UserDetail mUserDetail;

        public void setUser(UserDetail user) {
            mUserDetail = user;
            setUserInfo();
        }

        public UserDetail getUser() {
            return mUserDetail;
        }

        private void setUserInfo() {
            String userName = mUserDetail.getFirstName() + " " + mUserDetail.getLastName();
            mCollapsingToolbar.setTitle(userName);
            Picasso.with(getBaseContext())
                    .load(mUserDetail.getAvatarUrl())
                    .error(R.drawable.default_background)
                    .into(mUserImageView);
        }
    }

    private void setupStat() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Tracker tracker = EvendateApplication.getTracker();
                tracker.setScreenName("User Profile Screen ~" +
                        mUserPagerAdapter.getPageLabel(position));
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoader.cancelLoad();
        mDrawer.cancel();
    }
}