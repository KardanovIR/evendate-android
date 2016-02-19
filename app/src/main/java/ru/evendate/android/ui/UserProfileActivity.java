package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import ru.evendate.android.R;
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
    private RecyclerView mRecyclerView;
    SubscriptionsAdapter mAdapter;
    UserLoader mLoader;
    UserAdapter mUserAdapter;

    //ImageView mUserImageView;
    //TextView mUserNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);
        Intent intent = getIntent();
        if(intent != null){
            userId = Integer.parseInt(intent.getData().getLastPathSegment());
        }
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        //mUserImageView = (ImageView)findViewById(R.id.user_image);
        //mUserNameTextView = (TextView)findViewById(R.id.user_name);
        mAdapter = new SubscriptionsAdapter(this);
        mUserAdapter = new UserAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLoader = new UserLoader(this);
        mLoader.setLoaderListener(this);
        mLoader.getData(userId);
    }
    @Override
    public void onLoaded(UserDetail user) {
        mAdapter.setSubscriptionList(user.getSubscriptions());
        mUserAdapter.setUser(user);
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
            //prevent illegal state exception cause fragment not attached to
            String userName = mUserDetail.getLastName() + " " + mUserDetail.getFirstName();
            //mUserNameTextView.setText(userName);

            //Picasso.with(getBaseContext())
            //        .load(mUserDetail.getAvatarUrl())
            //        .error(R.drawable.default_background)
            //        .into(mUserImageView);
        }
    }
}