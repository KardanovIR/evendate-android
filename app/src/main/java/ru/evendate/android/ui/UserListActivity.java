package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UserListActivity extends AppCompatActivity {
    private UserListFragment mUserListFragment;
    private EvendateDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            int type = intent.getIntExtra(UserListFragment.TYPE, 0);
            int id = Integer.parseInt(uri.getLastPathSegment());
            if (type == UserListFragment.TypeFormat.event.nativeInt) {
                mUserListFragment = UserListFragment.newInstance(type, id);
            } else {
                mUserListFragment = UserListFragment.newInstance(type, id);
            }
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);
        fragmentManager.beginTransaction().replace(R.id.main_content, mUserListFragment).commit();
        mDrawer = EvendateDrawer.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(this, mDrawer.getDrawer()));
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
