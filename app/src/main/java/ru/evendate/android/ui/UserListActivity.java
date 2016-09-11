package ru.evendate.android.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.Bind;
import ru.evendate.android.R;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UserListActivity extends AppCompatActivity {
    private UserListFragment mUserListFragment;
    private DrawerWrapper mDrawer;
    @Bind(R.id.ll_feed_empty) LinearLayout mFeedEmptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            int type = intent.getIntExtra(UserListFragment.TYPE, 0);
            int id;
            switch (UserListFragment.TypeFormat.getType(type)){
                case EVENT:
                    id = Integer.parseInt(uri.getLastPathSegment());
                    mUserListFragment = UserListFragment.newInstance(type, id);
                    break;
                case ORGANIZATION:
                    id = Integer.parseInt(uri.getLastPathSegment());
                    mUserListFragment = UserListFragment.newInstance(type, id);
                    break;
                case FRIENDS:
                    mUserListFragment = UserListFragment.newFriendsInstance(type);
                    toolbar.setTitle(getString(R.string.title_activity_friends));
                    break;
            }
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        fragmentManager.beginTransaction().replace(R.id.main_content, mUserListFragment).commit();
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(this, mDrawer.getDrawer()));
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
        ActivityManager activityManager = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if(taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getClass().getName())) {
            startActivity(NavUtils.getParentActivityIntent(this));
        }
        else{
            onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mDrawer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mDrawer.cancel();
    }
}
