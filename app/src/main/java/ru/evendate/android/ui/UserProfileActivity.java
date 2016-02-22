package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ru.evendate.android.R;
import ru.evendate.android.adapters.UserPagerAdapter;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.UserLoader;
import ru.evendate.android.models.UserDetail;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserProfileActivity extends AppCompatActivity implements LoaderListener<UserDetail> {
    private Uri mUri;
    private int userId;
    public static final String URI = "uri";
    UserAdapter mUserAdapter;
    UserLoader mLoader;

    private ViewPager mViewPager;
    private UserPagerAdapter mUserPagerAdapter;
    private TabLayout mTabLayout;

    private ImageView mUserImageView;
    private CollapsingToolbarLayout mCollapsingToolbar;

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
        if(intent != null){
            userId = Integer.parseInt(intent.getData().getLastPathSegment());
        }
        mCollapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        mUserImageView = (ImageView)findViewById(R.id.user_avatar);

        mLoader = new UserLoader(this);
        mLoader.setLoaderListener(this);
        mUserAdapter = new UserAdapter();
        mLoader.getData(userId);
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabLayout = (TabLayout)findViewById(R.id.tabs);
    }

    @Override
    public void onLoaded(UserDetail user) {
        mUserAdapter.setUser(user);
        mUserPagerAdapter = new UserPagerAdapter(getSupportFragmentManager(), this, user);
        mViewPager.setAdapter(mUserPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onError() {
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLoader.getData(userId);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private class UserAdapter{
        private UserDetail mUserDetail;

        public void setUser(UserDetail user) {
            mUserDetail = user;
            setUserInfo();
        }

        public UserDetail getUser() {
            return mUserDetail;
        }

        private void setUserInfo(){
            String userName = mUserDetail.getFirstName() + " " + mUserDetail.getLastName();
            mCollapsingToolbar.setTitle(userName);
            Picasso.with(getBaseContext())
                    .load(mUserDetail.getAvatarUrl())
                    .error(R.drawable.default_background)
                    .into(mUserImageView);
        }
    }
}