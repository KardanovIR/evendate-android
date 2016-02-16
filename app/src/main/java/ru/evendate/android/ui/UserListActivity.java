package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.SubscriptionLoader;
import ru.evendate.android.sync.models.OrganizationModel;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UserListActivity extends AppCompatActivity
        implements LoaderListener<ArrayList<OrganizationModel>> {
    UserListFragment mUserListFragment;
    private SubscriptionLoader mSubscriptionLoader;
    private EvendateDrawer mDrawer;
    private boolean mDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        if(intent != null){
            Uri uri = intent.getData();
            int type = intent.getIntExtra(UserListFragment.TYPE, 0);
            int id = Integer.parseInt(uri.getLastPathSegment());
            if(type == UserListFragment.TypeFormat.event.nativeInt){
                mUserListFragment = UserListFragment.newInstance(type, id);
            } else {
                mUserListFragment = UserListFragment.newInstance(type, id);
            }
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);

        mSubscriptionLoader = new SubscriptionLoader(this);
        mSubscriptionLoader.setSubscriptionLoaderListener(this);

        mDrawer = EvendateDrawer.newInstance(this);
        fragmentManager.beginTransaction().replace(R.id.main_content, mUserListFragment).commit();
        mSubscriptionLoader.getSubscriptions();
    }

    @Override
    public void onLoaded(ArrayList<OrganizationModel> subList) {
        if(isDestroyed())
            return;
        mDrawer.updateSubs(subList);
    }

    @Override
    public void onError() {
        if(isDestroyed())
            return;
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSubscriptionLoader.getSubscriptions();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Returns true if the final {@link #onDestroy()} call has been made
     * on the Activity, so this instance is now dead.
     * cause api 17 has not this method
     */
    @Override
    public boolean isDestroyed() {
        return mDestroyed;
    }

    @Override
    protected void onDestroy() {
        mDestroyed = true;
        super.onDestroy();
    }
}
