package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;

public class UserListActivity extends BaseActivity {
    private UserListFragment mUserListFragment;
    private DrawerWrapper mDrawer;
    @Bind(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);
        initToolbar();
        initDrawer();

        handleIntent(getIntent());
        mDrawer.start();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            Uri uri = intent.getData();
            int type = intent.getIntExtra(UserListFragment.TYPE, 0);
            int id;
            switch (UserListFragment.TypeFormat.getType(type)) {
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
                    mToolbar.setTitle(getString(R.string.title_activity_friends));
                    break;
            }
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, mUserListFragment).commit();
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

}
