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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
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
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.adapters.UserPagerAdapter;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserProfileActivity extends AppCompatActivity {
    private final static String LOG_TAG = UserProfileActivity.class.getSimpleName();
    private Uri mUri;
    private int userId;
    public static final String URI = "uri";
    public static final String USER_ID = "user_id";
    private UserAdapter mUserAdapter;

    @Bind(R.id.pager) ViewPager mViewPager;
    private UserPagerAdapter mUserPagerAdapter;
    @Bind(R.id.tabs) TabLayout mTabLayout;

    @Bind(R.id.user_avatar) ImageView mUserImageView;
    @Bind(R.id.avatar_container) View mUserImageContainer;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
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

        initToolbar();
        initProgressBar();
        initDrawer();
        initTransitions();

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

        mUserAdapter = new UserAdapter();
        setupStat();
        mUserImageContainer.setVisibility(View.INVISIBLE);

        loadUser();
        mDrawer.start();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
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

    private void initProgressBar() {
        mProgressBar.getProgressDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return super.onCreateOptionsMenu(menu);

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
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO DRY
    private void onUpPressed() {
        ActivityManager activityManager = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getClass().getName())) {
            Log.i(LOG_TAG, "This is last activity in the stack");
            startActivity(NavUtils.getParentActivityIntent(this));
        } else {
            onBackPressed();
        }
    }

    public void loadUser() {
        ApiService apiService = ApiFactory.getService(this);
        Observable<ResponseArray<UserDetail>> organizationObservable =
                apiService.getUser(EvendateAccountManager.peekToken(this),
                        userId, UserDetail.FIELDS_LIST);

        organizationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isOk())
                        onLoaded(result.getData());
                    else
                        Log.e(LOG_TAG, "Error with response with user");
                }, error -> {
                    Log.e(LOG_TAG, error.getMessage());
                    onError();
                });
    }

    public void onLoaded(ArrayList<UserDetail> users) {
        mUserAdapter.setUser(users.get(0));
        mUserPagerAdapter = new UserPagerAdapter(getSupportFragmentManager(), this, mUserAdapter.getUser());
        mViewPager.setAdapter(mUserPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mProgressBar.setVisibility(View.GONE);
    }

    public void onError() {
        mProgressBar.setVisibility(View.GONE);
        AlertDialog alertDialog = ErrorAlertDialogBuilder.newInstance(this, (DialogInterface dialog, int which) -> {
            loadUser();
            mProgressBar.setVisibility(View.VISIBLE);
            dialog.dismiss();
        });
        alertDialog.show();
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
                    .error(R.mipmap.ic_launcher)
                    .into(target);
        }
    }

    private void revealView(View view) {
        if (view.getVisibility() == View.VISIBLE)
            return;
        view.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT < 21)
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
        mDrawer.cancel();
    }
}