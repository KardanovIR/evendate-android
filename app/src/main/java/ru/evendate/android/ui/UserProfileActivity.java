package ru.evendate.android.ui;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
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
    private final static String LOG_TAG = UserProfileActivity.class.getSimpleName();
    private Uri mUri;
    private int userId;
    public static final String URI = "uri";
    public static final String USER_ID = "user_id";
    private UserAdapter mUserAdapter;
    private UserLoader mLoader;

    @Bind(R.id.pager) ViewPager mViewPager;
    private UserPagerAdapter mUserPagerAdapter;
    @Bind(R.id.tabs) TabLayout mTabLayout;

    @Bind(R.id.user_avatar) ImageView mUserImageView;
    @Bind(R.id.avatar_container) View mUserImageContainer;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    DrawerWrapper mDrawer;

    public static final String INTENT_TYPE = "type";
    public static final String NOTIFICATION = "notification";

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

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);

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

        mLoader = new UserLoader(this, userId);
        mLoader.setLoaderListener(this);
        mUserAdapter = new UserAdapter();
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(this, mDrawer.getDrawer()));
        setupStat();
        initTransitions();
        mUserImageContainer.setVisibility(View.INVISIBLE);
    }

    private void initTransitions(){
        if(Build.VERSION.SDK_INT >= 21){
            getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
            getWindow().setExitTransition(new Slide(Gravity.TOP));
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mLoader.startLoading();
        mDrawer.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onUpPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO DRY
    private void onUpPressed(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if(taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getClass().getName())) {
            Log.i(LOG_TAG, "This is last activity in the stack");
            startActivity(NavUtils.getParentActivityIntent(this));
        }
        else{
            onBackPressed();
        }
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
                    .error(R.drawable.default_background)
                    .into(target);
        }
    }

    private void revealView(View view){
        if(view.getVisibility() == View.VISIBLE)
            return;
        view.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT < 21)
            return;
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator animation = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        animation.start();
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