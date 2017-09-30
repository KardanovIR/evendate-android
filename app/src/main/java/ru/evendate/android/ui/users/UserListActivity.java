package ru.evendate.android.ui.users;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;

public class UserListActivity extends BaseActivity {
    @BindView(R.id.toolbar) Toolbar mToolbar;
    private UserListFragment mUserListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);
        initToolbar();
        initDrawer();
        initTransitions();

        handleIntent(getIntent());
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new DrawerWrapper.NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    private void initTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
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
        new UserListPresenter(new DataRepository(this), mUserListFragment);
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

    @Override
    protected void onReload() {
        super.onReload();
        mUserListFragment.onReload();
    }
}
